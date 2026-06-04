package com.finances.dashboard.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.finances.dashboard.enums.PaymentStatus;

public record PaymentResponse(Long id, String description, BigDecimal amount, LocalDate dueDate, Boolean recurring, String method, PaymentStatus status, LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime deletedAt) {

}
