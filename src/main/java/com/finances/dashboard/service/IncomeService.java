package com.finances.dashboard.service;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.finances.dashboard.dto.request.IncomeCreateRequest;
import com.finances.dashboard.dto.request.IncomeUpdateRequest;
import com.finances.dashboard.model.Income;
import com.finances.dashboard.model.User;
import com.finances.dashboard.repository.IncomeRepository;

@Service
public class IncomeService extends BaseService<Income> {
    @Autowired
    private IncomeRepository repository;

    @Override
    public IncomeRepository getRepository() {
        return repository;
    }

    public Page<Income> findByUser_Id(Long userId, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return repository.findByUser_IdAndDeletedAtIsNullAndReceivedDateBetween(userId, startDate, endDate, pageable);
    }

    public BigDecimal sumByUser_IdAndDeletedAtIsNullAndReceivedDateBetween(Long userId, LocalDate startDate, LocalDate endDate) {
        return repository.sumByUser_IdAndDeletedAtIsNullAndReceivedDateBetween(userId, startDate, endDate);
    }

    public Income create(IncomeCreateRequest incomeRequest, User user) {
        Income income = new Income();
        income.setDescription(incomeRequest.description());
        income.setAmount(incomeRequest.amount());
        income.setReceivedDate(incomeRequest.receivedDate());
        income.setUser(user);
        return repository.save(income);
    }

    public Income update(Long id, IncomeUpdateRequest incomeRequest) {
        Income existingIncome = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Income not found with id: " + id));
        if (existingIncome.getDeletedAt() != null) {
            throw new RuntimeException("Income is deleted and cannot be updated.");
        }
        if (incomeRequest.description() != null)
            existingIncome.setDescription(incomeRequest.description());
        if (incomeRequest.amount() != null)
            existingIncome.setAmount(incomeRequest.amount());
        if (incomeRequest.receivedDate() != null)
            existingIncome.setReceivedDate(incomeRequest.receivedDate());
        return repository.save(existingIncome);
    }

}
