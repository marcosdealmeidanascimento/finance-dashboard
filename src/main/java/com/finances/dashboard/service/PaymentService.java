package com.finances.dashboard.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.finances.dashboard.enums.PaymentStatus;
import com.finances.dashboard.model.Charge;
import com.finances.dashboard.model.Payment;
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

    public Payment findByChargeId(Long chargeId) {
        return repository.findByCharge_Id(chargeId).orElse(null);
    }

    public List<Payment> findByUser_idAndStatus(Long userId, PaymentStatus status) {
        return repository.findByUser_idAndStatus(userId, status);
    }

    public Payment create(String description, Charge charge) {
        Payment payment = new Payment();
        payment.setStatus(PaymentStatus.PENDING);
        payment.setDescription(description);
        payment.setCharge(charge);
        payment.setUser(charge.getUser());
        return repository.save(payment);
    }

    @Transactional
    public Payment update(Payment payment) {
        Payment existingPayment = repository.findById(payment.getId())
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + payment.getId()));
        existingPayment.setDescription(payment.getDescription());
        existingPayment.setMethod(payment.getMethod());
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
