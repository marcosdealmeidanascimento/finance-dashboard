package com.finances.dashboard.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.finances.dashboard.enums.PaymentStatus;
import com.finances.dashboard.model.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Page<Payment> findByUser_Id(Long userId, Pageable pageable);

    Page<Payment> findByUser_IdAndStatus(Long userId, PaymentStatus status, Pageable pageable);

    List<Payment> findByStatusAndDeletedAtIsNull(PaymentStatus status);
}
