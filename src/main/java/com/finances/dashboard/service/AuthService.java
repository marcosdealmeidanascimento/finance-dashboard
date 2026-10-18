package com.finances.dashboard.service;

import java.time.Clock;
import java.time.Duration;
import java.util.Date;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.finances.dashboard.dto.request.LoginRequest;
import com.finances.dashboard.dto.response.AuthResponse;
import com.finances.dashboard.exception.UnauthorizedException;
import com.finances.dashboard.model.RefreshToken;
import com.finances.dashboard.model.User;
import com.finances.dashboard.repository.RefreshTokenRepository;
import com.finances.dashboard.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class AuthService {
    private static final Duration REFRESH_TOKEN_TTL = Duration.ofDays(30);

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final LoginAttemptService loginAttemptService;
    private final Clock clock;

    public AuthService(UserRepository userRepository, RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder, JwtService jwtService, LoginAttemptService loginAttemptService,
            Clock clock) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.loginAttemptService = loginAttemptService;
        this.clock = clock;
    }

    @Transactional
    public AuthResponse login(LoginRequest request, String clientIp) {
        String email = UserService.normalizeEmail(request.email());
        String attemptKey = LoginAttemptService.key(email, clientIp);
        loginAttemptService.checkAllowed(attemptKey);

        // Same 401 and message for unknown email and wrong password, so the API doesn't reveal which emails exist.
        User user = userRepository.findActiveByEmail(email)
                .filter(u -> passwordEncoder.matches(request.password(), u.getPassword()))
                .orElse(null);
        if (user == null) {
            loginAttemptService.recordFailure(attemptKey);
            throw new UnauthorizedException("E-mail ou senha inválidos.");
        }
        loginAttemptService.recordSuccess(attemptKey);

        // One refresh token per user (unique user_id): a new login replaces the previous session's token.
        RefreshToken refreshToken = refreshTokenRepository.findByUser(user).orElseGet(RefreshToken::new);
        refreshToken.setUser(user);
        String refreshTokenValue = issue(refreshToken);

        return new AuthResponse(jwtService.generateToken(user), refreshTokenValue);
    }

    /**
     * Rotates the refresh token: every use returns a new one and the old value stops working,
     * so a leaked token is only good until the legitimate app refreshes again.
     */
    @Transactional
    public AuthResponse refresh(String refreshToken) {
        RefreshToken refreshTokenEntity = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new UnauthorizedException("Sessão inválida. Faça login novamente."));
        if (refreshTokenEntity.getExpiryAt().before(Date.from(clock.instant()))) {
            refreshTokenRepository.delete(refreshTokenEntity);
            throw new UnauthorizedException("Sessão expirada. Faça login novamente.");
        }
        User user = refreshTokenEntity.getUser();
        if (user.getDeletedAt() != null) {
            throw new UnauthorizedException("Sessão inválida. Faça login novamente.");
        }
        String newRefreshToken = issue(refreshTokenEntity);
        return new AuthResponse(jwtService.generateToken(user), newRefreshToken);
    }

    @Transactional
    public void logout(User user) {
        refreshTokenRepository.deleteByUser(user);
    }

    private String issue(RefreshToken refreshToken) {
        String value = jwtService.generateRefreshToken();
        refreshToken.setToken(value);
        refreshToken.setExpiryAt(Date.from(clock.instant().plus(REFRESH_TOKEN_TTL)));
        refreshTokenRepository.save(refreshToken);
        return value;
    }

}
