package com.finances.dashboard.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record IncomeResponse(
    Long id,
    String description,
    BigDecimal amount,
    LocalDate receivedDate
) {

}
