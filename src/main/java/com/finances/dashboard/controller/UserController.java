package com.finances.dashboard.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.finances.dashboard.dto.request.UserCreateRequest;
import com.finances.dashboard.dto.request.UserProfilePictureRequest;
import com.finances.dashboard.dto.request.UserUpdateRequest;
import com.finances.dashboard.dto.response.UserProfilePictureResponse;
import com.finances.dashboard.dto.response.UserResponse;
import com.finances.dashboard.mapper.UserMapper;
import com.finances.dashboard.service.UserService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

@RestController
@RequestMapping("api/users")
@SecurityRequirement(name = "bearerAuth")
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;

    public UserController(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(userMapper.toResponse(userService.getActive(userId)));
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserCreateRequest user) {
        return ResponseEntity.ok(userMapper.toResponse(userService.create(user)));
    }

    // The {id} routes only ever act on the caller's own account.
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest userRequest,
            @AuthenticationPrincipal Long userId) {
        if (!id.equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(userMapper.toResponse(userService.update(id, userRequest)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id, @AuthenticationPrincipal Long userId) {
        if (!id.equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        userService.deleteAccount(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/profile-picture")
    public ResponseEntity<UserProfilePictureResponse> getProfilePicture(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(new UserProfilePictureResponse(userService.getProfilePicture(userId)));
    }

    @PutMapping(value = "/profile-picture/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserProfilePictureResponse> uploadProfilePicture(@PathVariable Long id,
            @ModelAttribute UserProfilePictureRequest userRequest, @AuthenticationPrincipal Long userId) {
        if (!id.equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(new UserProfilePictureResponse(userService.updateProfilePicture(id, userRequest)));
    }

}
