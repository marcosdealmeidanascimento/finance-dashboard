package com.finances.dashboard.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.finances.dashboard.dto.request.PaymentCreateRequest;
import com.finances.dashboard.dto.request.PaymentUpdateRequest;
import com.finances.dashboard.enums.PaymentStatus;
import com.finances.dashboard.exception.ConflictException;
import com.finances.dashboard.exception.ResourceNotFoundException;
import com.finances.dashboard.model.Payment;
import com.finances.dashboard.model.User;
import com.finances.dashboard.repository.PaymentRepository;

import jakarta.transaction.Transactional;

@Service
public class PaymentService {
    // Payments that can still be edited, paid or cancelled. PAID and CANCELLED are final.
    private static final Set<PaymentStatus> OPEN_STATUSES = EnumSet.of(PaymentStatus.PENDING, PaymentStatus.FAILED);

    private final PaymentRepository repository;

    public PaymentService(PaymentRepository repository) {
        this.repository = repository;
    }

    /** Active payment owned by the user; anything else (missing, deleted, someone else's) is a 404. */
    public Payment getOwned(Long id, Long userId) {
        return repository.findByIdAndUser_IdAndDeletedAtIsNull(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Pagamento não encontrado."));
    }

    public Page<Payment> findByUserAndStatus(
            Long userId,
            PaymentStatus status,
            Pageable pageable) {

        if (status != null) {
            return repository.findByUser_IdAndStatusAndDeletedAtIsNull(userId, status, pageable);
        }

        return repository.findByUser_IdAndDeletedAtIsNull(userId, pageable);
    }

    public BigDecimal sumByUser_IdAndDeletedAtIsNullAndDueDateBetween(Long userId, LocalDate startDate, LocalDate endDate) {
        return repository.sumByUser_IdAndDeletedAtIsNullAndDueDateBetween(userId, startDate, endDate);
    }

    public Payment create(PaymentCreateRequest paymentRequest, User user) {
        Payment payment = new Payment();
        payment.setDescription(paymentRequest.description().trim());
        payment.setAmount(paymentRequest.amount());
        payment.setDueDate(paymentRequest.dueDate());
        payment.setRecurring(paymentRequest.recurring());
        payment.setMethod(paymentRequest.method().trim());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setUser(user);
        return repository.save(payment);
    }

    @Transactional
    public Payment update(Long id, Long userId, PaymentUpdateRequest payment) {
        Payment existingPayment = getOwned(id, userId);
        requireOpen(existingPayment, "Só pagamentos pendentes ou com falha podem ser editados.");
        existingPayment.setDescription(payment.description().trim());
        existingPayment.setMethod(payment.method().trim());
        existingPayment.setAmount(payment.amount());
        existingPayment.setDueDate(payment.dueDate());
        existingPayment.setRecurring(payment.recurring());
        return repository.save(existingPayment);
    }

    @Transactional
    public Payment markAsPaid(Long id, Long userId) {
        Payment existingPayment = getOwned(id, userId);
        requireOpen(existingPayment, "Este pagamento já foi pago ou cancelado.");
        existingPayment.setStatus(PaymentStatus.PAID);
        if (Boolean.TRUE.equals(existingPayment.getRecurring())) {
            Payment newPayment = new Payment();
            newPayment.setDescription(existingPayment.getDescription());
            newPayment.setAmount(existingPayment.getAmount());
            newPayment.setDueDate(existingPayment.getDueDate().plusMonths(1));
            newPayment.setRecurring(true);
            newPayment.setMethod(existingPayment.getMethod());
            newPayment.setStatus(PaymentStatus.PENDING);
            newPayment.setUser(existingPayment.getUser());
            repository.save(newPayment);
        }
        return repository.save(existingPayment);
    }

    @Transactional
    public Payment markAsFailed(Long id, Long userId) {
        Payment existingPayment = getOwned(id, userId);
        if (existingPayment.getStatus() != PaymentStatus.PENDING) {
            throw new ConflictException("Só pagamentos pendentes podem ser marcados como falha.");
        }
        existingPayment.setStatus(PaymentStatus.FAILED);
        return repository.save(existingPayment);
    }

    @Transactional
    public Payment markAsCanceled(Long id, Long userId) {
        Payment existingPayment = getOwned(id, userId);
        requireOpen(existingPayment, "Este pagamento já foi pago ou cancelado.");
        existingPayment.setStatus(PaymentStatus.CANCELLED);
        return repository.save(existingPayment);
    }

    @Transactional
    public void delete(Long id, Long userId) {
        Payment existingPayment = getOwned(id, userId);
        if (existingPayment.getStatus() == PaymentStatus.PAID) {
            throw new ConflictException("Pagamentos já pagos não podem ser excluídos.");
        }
        existingPayment.softDelete();
        repository.save(existingPayment);
    }

    public List<Payment> listCloseToDueDatePayments() {
        List<Payment> payments = repository.findByStatusAndDeletedAtIsNull(PaymentStatus.PENDING).stream()
                .filter(p -> p.isCloseToDueDate()).toList();
        return payments;
    }

    private static void requireOpen(Payment payment, String message) {
        if (!OPEN_STATUSES.contains(payment.getStatus())) {
            throw new ConflictException(message);
        }
    }

}
