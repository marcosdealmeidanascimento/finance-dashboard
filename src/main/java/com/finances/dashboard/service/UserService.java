package com.finances.dashboard.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.finances.dashboard.dto.request.UserCreateRequest;
import com.finances.dashboard.dto.request.UserUpdateRequest;
import com.finances.dashboard.exception.BadRequestException;
import com.finances.dashboard.exception.ResourceNotFoundException;
import com.finances.dashboard.model.User;
import com.finances.dashboard.repository.UserRepository;

import jakarta.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UserService extends BaseService<User> {

    @Autowired
    private UserRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    private final Logger log = LoggerFactory.getLogger(UserService.class);

    @Override
    protected JpaRepository<User, Long> getRepository() {
        return repository;
    }

    public User create(UserCreateRequest userCreateRequest) {
        User user = new User();
        user.setName(userCreateRequest.name());
        if (repository.existsByEmail(userCreateRequest.email())) {
            throw new BadRequestException("Email already exists");
        }
        user.setEmail(userCreateRequest.email());
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
        User existingUser = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        if (user.name() != null)
            existingUser.setName(user.name());
        if (user.email() != null)
            existingUser.setEmail(user.email());
        if (user.password() != null && !user.password().isBlank()) {
            existingUser.setPassword(
                    passwordEncoder.encode(user.password()));
        }
        return repository.save(existingUser);
    }

}
