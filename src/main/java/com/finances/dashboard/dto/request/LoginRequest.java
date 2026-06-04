package com.finances.dashboard.dto.request;


public record LoginRequest(
    String email,
    String password
) {
}
