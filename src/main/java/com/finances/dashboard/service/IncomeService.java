package com.finances.dashboard.service;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.finances.dashboard.dto.request.IncomeCreateRequest;
import com.finances.dashboard.dto.request.IncomeUpdateRequest;
import com.finances.dashboard.exception.ResourceNotFoundException;
import com.finances.dashboard.model.Income;
import com.finances.dashboard.model.User;
import com.finances.dashboard.repository.IncomeRepository;

import jakarta.transaction.Transactional;

@Service
public class IncomeService {
    private final IncomeRepository repository;

    public IncomeService(IncomeRepository repository) {
        this.repository = repository;
    }

    /** Active income owned by the user; anything else (missing, deleted, someone else's) is a 404. */
    public Income getOwned(Long id, Long userId) {
        return repository.findByIdAndUser_IdAndDeletedAtIsNull(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Receita não encontrada."));
    }

    public Page<Income> findByUser_Id(Long userId, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return repository.findByUser_IdAndDeletedAtIsNullAndReceivedDateBetween(userId, startDate, endDate, pageable);
    }

    public BigDecimal sumByUser_IdAndDeletedAtIsNullAndReceivedDateBetween(Long userId, LocalDate startDate, LocalDate endDate) {
        return repository.sumByUser_IdAndDeletedAtIsNullAndReceivedDateBetween(userId, startDate, endDate);
    }

    public Income create(IncomeCreateRequest incomeRequest, User user) {
        Income income = new Income();
        income.setDescription(incomeRequest.description().trim());
        income.setAmount(incomeRequest.amount());
        income.setReceivedDate(incomeRequest.receivedDate());
        income.setUser(user);
        return repository.save(income);
    }

    @Transactional
    public Income update(Long id, Long userId, IncomeUpdateRequest incomeRequest) {
        Income existingIncome = getOwned(id, userId);
        existingIncome.setDescription(incomeRequest.description().trim());
        existingIncome.setAmount(incomeRequest.amount());
        existingIncome.setReceivedDate(incomeRequest.receivedDate());
        return repository.save(existingIncome);
    }

    @Transactional
    public void delete(Long id, Long userId) {
        Income existingIncome = getOwned(id, userId);
        existingIncome.softDelete();
        repository.save(existingIncome);
    }

}
