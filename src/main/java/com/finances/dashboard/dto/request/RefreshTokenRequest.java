package com.finances.dashboard.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(@NotBlank(message = "Informe o refresh token.") String refreshToken) {
}
