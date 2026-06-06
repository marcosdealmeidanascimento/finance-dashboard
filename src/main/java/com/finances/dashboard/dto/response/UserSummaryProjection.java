package com.finances.dashboard.dto.response;

import java.math.BigDecimal;

public record UserSummaryProjection(
        Long userId,
        String name,
        String email,
        BigDecimal totalIncome,
        BigDecimal totalPayments) {
}
