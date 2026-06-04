package com.finances.dashboard.mapper;

import org.springframework.stereotype.Component;

import com.finances.dashboard.dto.response.ChargeResponse;
import com.finances.dashboard.model.Charge;

@Component
public class ChargeMapper {

    public ChargeResponse toResponse(Charge charge) {
        return new ChargeResponse(
                charge.getId(),
                charge.getDescription(),
                charge.getAmount(),
                charge.getDueDate(),
                charge.getRecurring(),
                charge.getCreatedAt(),
                charge.getUpdatedAt(),
                charge.getDeletedAt()
        );
    }
}