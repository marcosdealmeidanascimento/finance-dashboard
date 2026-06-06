package com.finances.dashboard.mapper;

import org.springframework.stereotype.Component;

import com.finances.dashboard.dto.response.IncomeResponse;
import com.finances.dashboard.model.Income;

@Component
public class IncomeMapper {

    public IncomeResponse toResponse(Income income) {
        return new IncomeResponse(
                income.getId(),
                income.getDescription(),
                income.getAmount(),
                income.getReceivedDate());
    }
}
