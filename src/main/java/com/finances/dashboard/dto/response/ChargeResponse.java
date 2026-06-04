package com.finances.dashboard.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ChargeResponse(Long id, String description, BigDecimal amount, LocalDate dueDate, Boolean recurring, LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime deletedAt) {

}
