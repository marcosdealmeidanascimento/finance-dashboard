package com.finances.dashboard.repository;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.finances.dashboard.model.Income;

public interface IncomeRepository extends JpaRepository<Income, Long> {
    Page<Income> findByUser_IdAndDeletedAtIsNullAndReceivedDateBetween(Long userId, LocalDate startDate,
            LocalDate endDate, Pageable pageable);

    @Query("SELECT SUM(i.amount) FROM Income i WHERE i.user.id = ?1 AND i.deletedAt IS NULL AND i.receivedDate BETWEEN ?2 AND ?3")
    BigDecimal sumByUser_IdAndDeletedAtIsNullAndReceivedDateBetween(Long userId, LocalDate startDate,
            LocalDate endDate);
}