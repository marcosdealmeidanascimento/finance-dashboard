package com.finances.dashboard.mapper;

import org.springframework.stereotype.Component;

import com.finances.dashboard.dto.response.PaymentResponse;
import com.finances.dashboard.model.Payment;

@Component
public class PaymentMapper {

    private final ChargeMapper chargeMapper;

    public PaymentMapper(ChargeMapper chargeMapper) {
        this.chargeMapper = chargeMapper;
    }

    public PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getDescription(),
                payment.getCharge().getAmount(),
                payment.getCharge().getDueDate(),
                payment.getCharge().getRecurring(),
                payment.getMethod(),
                payment.getStatus(),
                chargeMapper.toResponse(payment.getCharge()),
                payment.getCreatedAt(),
                payment.getUpdatedAt(),
                payment.getDeletedAt()
        );
    }
}