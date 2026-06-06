package com.finances.dashboard.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.finances.dashboard.dto.request.LoginRequest;
import com.finances.dashboard.dto.response.AuthResponse;
import com.finances.dashboard.model.RefreshToken;
import com.finances.dashboard.model.User;
import com.finances.dashboard.repository.RefreshTokenRepository;
import com.finances.dashboard.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("User not found with email: " + request.email()));
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }
        String accessToken = jwtService.generateToken(user);
        String refreshTokenValue = jwtService.generateRefreshToken();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(refreshTokenValue);
        refreshToken.setExpiryAt(
                Date.from(
                        Instant.now().plus(30, ChronoUnit.DAYS)));
        refreshTokenRepository.save(refreshToken);

        return new AuthResponse(accessToken, refreshTokenValue);

    }

    public AuthResponse refresh(String refreshToken) {
        RefreshToken refreshTokenEntity = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));
        if (refreshTokenEntity.getExpiryAt().before(new Date())) {
            throw new RuntimeException("Refresh token expired");
        }
        String accessToken = jwtService.generateToken(refreshTokenEntity.getUser());
        return new AuthResponse(accessToken, refreshToken);
    }

    @Transactional
    public void logout(User user) {
        refreshTokenRepository.deleteByUser(user);
    }

}
