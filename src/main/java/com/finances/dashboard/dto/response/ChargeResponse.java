package com.finances.dashboard.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ChargeResponse(Long id, String description, BigDecimal amount, LocalDate dueDate, Boolean recurring) {

}
