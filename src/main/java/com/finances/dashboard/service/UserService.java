package com.finances.dashboard.service;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.finances.dashboard.dto.request.UserCreateRequest;
import com.finances.dashboard.dto.request.UserProfilePictureRequest;
import com.finances.dashboard.dto.request.UserUpdateRequest;
import com.finances.dashboard.exception.BadRequestException;
import com.finances.dashboard.exception.ConflictException;
import com.finances.dashboard.exception.ResourceNotFoundException;
import com.finances.dashboard.model.User;
import com.finances.dashboard.repository.RefreshTokenRepository;
import com.finances.dashboard.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final FileStorageService fileStorageService;
    private final RefreshTokenRepository refreshTokenRepository;

    public UserService(UserRepository repository, PasswordEncoder passwordEncoder, EmailService emailService,
            FileStorageService fileStorageService, RefreshTokenRepository refreshTokenRepository) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.fileStorageService = fileStorageService;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    /** Emails are stored trimmed and lowercase so uniqueness and login are case-insensitive. */
    public static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    public User getActive(Long id) {
        return repository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));
    }

    public User create(UserCreateRequest userCreateRequest) {
        String email = normalizeEmail(userCreateRequest.email());
        if (repository.existsActiveByEmail(email)) {
            throw new ConflictException("Este e-mail já está em uso.");
        }
        User user = new User();
        user.setName(userCreateRequest.name().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(userCreateRequest.password()));
        User createdUser = repository.save(user);
        try {
            emailService.sendWelcomeEmail(createdUser);
        } catch (Exception e) {
            log.error("Failed to send welcome email", e);
        }
        return createdUser;
    }

    @Transactional
    public User update(Long id, UserUpdateRequest user) {
        User existingUser = getActive(id);
        existingUser.setName(user.name().trim());
        String email = normalizeEmail(user.email());
        if (!email.equals(existingUser.getEmail())) {
            if (repository.existsActiveByEmail(email)) {
                throw new ConflictException("Este e-mail já está em uso.");
            }
            existingUser.setEmail(email);
        }
        if (user.password() != null && !user.password().isBlank()) {
            existingUser.setPassword(
                    passwordEncoder.encode(user.password()));
        }
        return repository.save(existingUser);
    }

    @Transactional
    public String updateProfilePicture(Long id, UserProfilePictureRequest userRequest) {
        User existingUser = getActive(id);
        MultipartFile file = userRequest.file();
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Envie uma imagem.");
        }
        if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
            throw new BadRequestException("O arquivo precisa ser uma imagem.");
        }

        String fileKey = fileStorageService.uploadFile(file);
        existingUser.setProfilePictureUrl(fileKey);
        return fileStorageService.generatePresignedUrl(fileKey);
    }

    @Transactional
    public void deleteAccount(Long id) {
        User user = getActive(id);
        user.softDelete();
        repository.save(user);
        // Revokes the session so the account can't keep refreshing access tokens.
        refreshTokenRepository.deleteByUser(user);
    }

    public String getProfilePicture(Long id) {
        User user = getActive(id);
        if (user.getProfilePictureUrl() == null || user.getProfilePictureUrl().isBlank()) {
            return null;
        }
        return fileStorageService.generatePresignedUrl(user.getProfilePictureUrl());
    }

}
