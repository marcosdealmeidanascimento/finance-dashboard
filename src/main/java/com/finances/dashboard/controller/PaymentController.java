package com.finances.dashboard.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.finances.dashboard.dto.request.PaymentCreateRequest;
import com.finances.dashboard.dto.request.PaymentUpdateRequest;
import com.finances.dashboard.dto.response.PaymentResponse;
import com.finances.dashboard.enums.PaymentStatus;
import com.finances.dashboard.mapper.PaymentMapper;
import com.finances.dashboard.model.Payment;
import com.finances.dashboard.model.User;
import com.finances.dashboard.service.PaymentService;
import com.finances.dashboard.service.UserService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("api/payment")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {
    private final PaymentService paymentService;
    private final UserService userService;
    private final PaymentMapper paymentMapper;

    public PaymentController(PaymentService paymentService, PaymentMapper paymentMapper, UserService userService) {
        this.paymentService = paymentService;
        this.paymentMapper = paymentMapper;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<Page<PaymentResponse>> getCurrentUserPayments(
            @RequestParam(required = false) PaymentStatus status,
            Pageable pageable,
            Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();

        Page<Payment> payments = paymentService.findByUserAndStatus(userId, status, pageable);

        return ResponseEntity.ok(
                payments.map(paymentMapper::toResponse));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable Long id) {
        try {
            Payment payment = paymentService.findById(id);
            return ResponseEntity.ok(paymentMapper.toResponse(payment));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(@RequestBody PaymentCreateRequest payment,
            Authentication authentication) {
        try {
            Long userId = (Long) authentication.getPrincipal();
            User user = userService.findById(userId);
            Payment createdPayment = paymentService.create(payment, user);
            return ResponseEntity.ok(paymentMapper.toResponse(createdPayment));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaymentResponse> updatePayment(@PathVariable Long id,
            @RequestBody PaymentUpdateRequest payment) {
        try {
            Payment updatedPayment = paymentService.update(id, payment);
            return ResponseEntity.ok(paymentMapper.toResponse(updatedPayment));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/mark-paid/{id}")
    public ResponseEntity<PaymentResponse> markPaymentPaid(@PathVariable Long id, Authentication authentication) {
        try {
            Long userId = (Long) authentication.getPrincipal();
            Payment payment = paymentService.findById(id);
            if (!payment.getUser().getId().equals(userId)) {
                return ResponseEntity.badRequest().build();
            }
            payment = paymentService.markAsPaid(id);
            return ResponseEntity.ok(paymentMapper.toResponse(payment));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/mark-cancelled/{id}")
    public ResponseEntity<PaymentResponse> markPaymentCancelled(@PathVariable Long id, Authentication authentication) {
        try {
            Long userId = (Long) authentication.getPrincipal();
            Payment payment = paymentService.findById(id);
            if (!payment.getUser().getId().equals(userId)) {
                return ResponseEntity.badRequest().build();
            }
            payment = paymentService.markAsCanceled(id);
            paymentService.softDelete(payment);
            return ResponseEntity.ok(paymentMapper.toResponse(payment));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePayment(@PathVariable Long id, Authentication authentication) {
        try {
            Long userId = (Long) authentication.getPrincipal();
            Payment payment = paymentService.findById(id);
            if (!payment.getUser().getId().equals(userId)) {
                return ResponseEntity.badRequest().build();
            }
            paymentService.softDelete(payment);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

}
