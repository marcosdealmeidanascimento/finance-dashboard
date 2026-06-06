package com.finances.dashboard.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

public record IncomeUpdateRequest(
    String description,
    BigDecimal amount,
    LocalDate receivedDate
) {

}
