package com.finances.dashboard.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

// PUT replaces the whole payment, so every field is required (same rules as creation).
public record PaymentUpdateRequest(
        @NotBlank(message = "Informe a descrição.") @Size(max = 255, message = "A descrição deve ter no máximo 255 caracteres.") String description,
        @NotNull(message = "Informe o valor.") @Positive(message = "O valor deve ser maior que zero.") @Digits(integer = 13, fraction = 2, message = "Valor inválido.") BigDecimal amount,
        @NotNull(message = "Informe a data de vencimento.") LocalDate dueDate,
        @NotNull(message = "Informe se o pagamento é recorrente.") Boolean recurring,
        @NotBlank(message = "Informe a forma de pagamento.") @Size(max = 50, message = "A forma de pagamento deve ter no máximo 50 caracteres.") String method) {

}
