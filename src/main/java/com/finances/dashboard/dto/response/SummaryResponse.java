package com.finances.dashboard.dto.response;

import java.math.BigDecimal;

public record SummaryResponse(BigDecimal totalIncome, BigDecimal totalPayments, BigDecimal balance) {

}
