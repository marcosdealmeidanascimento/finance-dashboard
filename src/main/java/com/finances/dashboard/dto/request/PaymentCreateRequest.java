package com.finances.dashboard.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PaymentCreateRequest(String description, BigDecimal amount, LocalDate dueDate, Boolean recurring, String method) {

}
