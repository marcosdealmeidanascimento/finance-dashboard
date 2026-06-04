package com.finances.dashboard.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ChargeCreateRequest(String description,
        BigDecimal amount,
        LocalDate dueDate,
        Boolean recurring) {
}
