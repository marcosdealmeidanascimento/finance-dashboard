package com.finances.dashboard.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.finances.dashboard.dto.request.PaymentCreateRequest;
import com.finances.dashboard.dto.request.PaymentUpdateRequest;
import com.finances.dashboard.enums.PaymentStatus;
import com.finances.dashboard.model.Payment;
import com.finances.dashboard.model.User;
import com.finances.dashboard.repository.PaymentRepository;

import jakarta.transaction.Transactional;

@Service
public class PaymentService extends BaseService<Payment> {
    @Autowired
    private PaymentRepository repository;

    @Override
    protected PaymentRepository getRepository() {
        return repository;
    }

    public List<Payment> findByUserId(Long userId) {
        return repository.findByUser_Id(userId);
    }

    public List<Payment> findByUserIdAndDeletedAtIsNull(Long userId) {
        return repository.findByUser_IdAndDeletedAtIsNull(userId);
    }

    public List<Payment> findByUser_idAndStatus(Long userId, PaymentStatus status) {
        return repository.findByUser_idAndStatus(userId, status);
    }

    public Payment create(PaymentCreateRequest paymentRequest, User user) {
        Payment payment = new Payment();
        payment.setDescription(paymentRequest.description());
        payment.setAmount(paymentRequest.amount());
        payment.setDueDate(paymentRequest.dueDate());
        payment.setRecurring(paymentRequest.recurring());
        payment.setMethod(paymentRequest.method());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setUser(user);
        return repository.save(payment);
    }

    @Transactional
    public Payment update(Long id, PaymentUpdateRequest payment) {
        Payment existingPayment = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + id));
        if (existingPayment.getDeletedAt() != null) {
            throw new RuntimeException("Payment is deleted and cannot be updated.");
        }
        if (payment.description() != null) existingPayment.setDescription(payment.description());
        if (payment.method() != null) existingPayment.setMethod(payment.method());
        if (payment.amount() != null) existingPayment.setAmount(payment.amount());
        if (payment.dueDate() != null) existingPayment.setDueDate(payment.dueDate());
        if (payment.recurring() != null) existingPayment.setRecurring(payment.recurring());
        return repository.save(existingPayment);
    }

    @Transactional
    public Payment markAsPaid(Long paymentId, String method) {
        Payment existingPayment = repository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + paymentId));
        existingPayment.setMethod(method);
        existingPayment.setStatus(PaymentStatus.PAID);
        return repository.save(existingPayment);
    }

    @Transactional
    public Payment markAsFailed(Long paymentId) {
        Payment existingPayment = repository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + paymentId));
        existingPayment.setStatus(PaymentStatus.FAILED);
        return repository.save(existingPayment);
    }

    @Transactional
    public Payment markAsCanceled(Long paymentId) {
        Payment existingPayment = repository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + paymentId));
        existingPayment.setStatus(PaymentStatus.CANCELLED);
        return repository.save(existingPayment);
    }

    public List<Payment> listCloseToDueDatePayments() {
        List<Payment> payments = repository.findByStatus(PaymentStatus.PENDING).stream().filter(p -> p.isCloseToDueDate()).toList();
        return payments;
    }

}
