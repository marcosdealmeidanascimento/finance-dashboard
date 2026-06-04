package com.finances.dashboard.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.finances.dashboard.dto.request.UserCreateRequest;
import com.finances.dashboard.dto.request.UserUpdateRequest;
import com.finances.dashboard.dto.response.UserResponse;
import com.finances.dashboard.model.User;
import com.finances.dashboard.service.JwtService;
import com.finances.dashboard.service.UserService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("api/users")
@SecurityRequirement(name = "bearerAuth")
public class UserController {
    private final UserService userService;
    private final JwtService jwtService;

    public UserController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        try {
            Long userId = (Long) authentication.getPrincipal();
            User user = userService.findById(userId);
            return ResponseEntity.ok(new UserResponse(user.getId(), user.getName(), user.getEmail()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        try {
            List<User> users = userService.findAll();
            return ResponseEntity.ok(users.stream()
                    .map(user -> new UserResponse(user.getId(), user.getName(), user.getEmail())).toList());
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody UserCreateRequest user) {
        try {
            User createdUser = userService.create(user);
            return ResponseEntity
                    .ok(new UserResponse(createdUser.getId(), createdUser.getName(), createdUser.getEmail()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @RequestBody UserUpdateRequest user,
            Authentication authentication) {
        Long currentUserId = (Long) authentication.getPrincipal();
        if (!id.equals(currentUserId)) {
            return ResponseEntity.badRequest().build();
        }
        try {
            User updatedUser = userService.update(id, user);
            return ResponseEntity
                    .ok(new UserResponse(updatedUser.getId(), updatedUser.getName(), updatedUser.getEmail()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        try {
            User user = userService.findById(id);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }
            userService.softDelete(user);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

}
