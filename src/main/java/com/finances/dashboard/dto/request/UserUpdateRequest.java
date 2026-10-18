package com.finances.dashboard.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
        @NotBlank(message = "Informe o nome.") @Size(max = 255, message = "O nome deve ter no máximo 255 caracteres.") String name,
        @NotBlank(message = "Informe o e-mail.") @Email(message = "E-mail inválido.") @Size(max = 255, message = "O e-mail deve ter no máximo 255 caracteres.") String email,
        // Optional: null keeps the current password.
        @Size(min = 6, max = 72, message = "A senha deve ter entre 6 e 72 caracteres.") String password) {

}
