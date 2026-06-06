package com.finances.dashboard.mapper;

import org.springframework.stereotype.Component;

import com.finances.dashboard.dto.response.UserResponse;
import com.finances.dashboard.model.User;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail());
    }

}
