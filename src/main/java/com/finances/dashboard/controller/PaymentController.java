package com.finances.dashboard.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
import com.finances.dashboard.service.PaymentService;
import com.finances.dashboard.service.UserService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

// Ownership and status rules live in PaymentService: every lookup is scoped to the authenticated user.
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
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(
                paymentService.findByUserAndStatus(userId, status, pageable).map(paymentMapper::toResponse));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable Long id, @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(paymentMapper.toResponse(paymentService.getOwned(id, userId)));
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody PaymentCreateRequest payment,
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(paymentMapper.toResponse(
                paymentService.create(payment, userService.getActive(userId))));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaymentResponse> updatePayment(@PathVariable Long id,
            @Valid @RequestBody PaymentUpdateRequest payment, @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(paymentMapper.toResponse(paymentService.update(id, userId, payment)));
    }

    @PostMapping("/mark-paid/{id}")
    public ResponseEntity<PaymentResponse> markPaymentPaid(@PathVariable Long id, @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(paymentMapper.toResponse(paymentService.markAsPaid(id, userId)));
    }

    @PostMapping("/mark-cancelled/{id}")
    public ResponseEntity<PaymentResponse> markPaymentCancelled(@PathVariable Long id,
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(paymentMapper.toResponse(paymentService.markAsCanceled(id, userId)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePayment(@PathVariable Long id, @AuthenticationPrincipal Long userId) {
        paymentService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }

}
