package com.finances.dashboard.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.finances.dashboard.enums.PaymentStatus;
import com.finances.dashboard.model.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByUser_Id(Long userId);
    List<Payment> findByUser_IdAndDeletedAtIsNull(Long userId);
    Optional<Payment> findByCharge_Id(Long id);
    List<Payment> findByUser_idAndStatus(Long userId, PaymentStatus status);
}
