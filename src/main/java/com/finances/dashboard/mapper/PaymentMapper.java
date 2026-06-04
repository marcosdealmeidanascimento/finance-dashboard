package com.finances.dashboard.mapper;

import org.springframework.stereotype.Component;

import com.finances.dashboard.dto.response.PaymentResponse;
import com.finances.dashboard.model.Payment;

@Component
public class PaymentMapper {

    public PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getDescription(),
                payment.getAmount(),
                payment.getDueDate(),
                payment.getRecurring(),
                payment.getMethod(),
                payment.getStatus(),
                payment.getCreatedAt(),
                payment.getUpdatedAt(),
                payment.getDeletedAt()
        );
    }
}