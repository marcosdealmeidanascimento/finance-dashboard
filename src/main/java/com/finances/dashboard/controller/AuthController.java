package com.finances.dashboard.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.finances.dashboard.dto.request.LoginRequest;
import com.finances.dashboard.dto.request.RefreshTokenRequest;
import com.finances.dashboard.dto.response.AuthResponse;
import com.finances.dashboard.model.User;
import com.finances.dashboard.service.AuthService;
import com.finances.dashboard.service.UserService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        try {
            return ResponseEntity.ok(authService.login(request));
        } catch (Exception e) {
            throw new RuntimeException("Login failed: " + e.getMessage());
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @RequestBody RefreshTokenRequest request) {
        try {
            return ResponseEntity.ok(authService.refresh(request.refreshToken()));
        } catch (Exception e) {
            throw new RuntimeException("Refresh token failed: " + e.getMessage());
        }
    }

    @PostMapping("/logout")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> logout(
            Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();

        User user = userService.findById(userId);

        authService.logout(user);

        return ResponseEntity.noContent().build();
    }
}
