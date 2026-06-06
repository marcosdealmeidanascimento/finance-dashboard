package com.finances.dashboard.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.finances.dashboard.enums.PaymentStatus;
import com.finances.dashboard.model.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Page<Payment> findByUser_Id(Long userId, Pageable pageable);

    Page<Payment> findByUser_IdAndStatus(Long userId, PaymentStatus status, Pageable pageable);

    List<Payment> findByStatusAndDeletedAtIsNull(PaymentStatus status);

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.user.id = ?1 AND p.deletedAt IS NULL AND p.dueDate BETWEEN ?2 AND ?3")
    BigDecimal sumByUser_IdAndDeletedAtIsNullAndDueDateBetween(Long userId, LocalDate startDate, LocalDate endDate);

}
