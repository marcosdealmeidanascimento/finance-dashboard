package com.finances.dashboard.repository;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.finances.dashboard.model.Income;

public interface IncomeRepository extends JpaRepository<Income, Long>{
    Page<Income> findByUser_IdAndDeletedAtIsNullAndReceivedDateBetween(Long userId, LocalDate startDate, LocalDate endDate, Pageable pageable);
}