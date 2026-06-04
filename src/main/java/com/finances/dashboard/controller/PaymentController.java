package com.finances.dashboard.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.finances.dashboard.dto.request.PaymentPaidRequest;
import com.finances.dashboard.enums.PaymentStatus;
import com.finances.dashboard.model.Payment;
import com.finances.dashboard.service.JwtService;
import com.finances.dashboard.service.PaymentService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("api/payment")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {
    private final PaymentService paymentService;
    private final JwtService jwtService;

    public PaymentController(PaymentService paymentService, JwtService jwtService) {
        this.paymentService = paymentService;
        this.jwtService = jwtService;
    }

    @GetMapping
    public ResponseEntity<List<Payment>> getCurrentUserPayments(Authentication authentication) {
        try {
            Long userId = (Long) authentication.getPrincipal();
            List<Payment> payments = paymentService.findByUserIdAndDeletedAtIsNull(userId);
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/paid")
    public ResponseEntity<List<Payment>> getPaidPayments(Authentication authentication) {
        try {
            Long userId = (Long) authentication.getPrincipal();
            List<Payment> payments = paymentService.findByUser_idAndStatus(userId, PaymentStatus.PAID);
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/cancelled")
    public ResponseEntity<List<Payment>> getCancelledPayments(Authentication authentication) {
        try {
            Long userId = (Long) authentication.getPrincipal();
            List<Payment> payments = paymentService.findByUser_idAndStatus(userId, PaymentStatus.CANCELLED);
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/mark-paid/{id}")
    public ResponseEntity<Payment> markPaymentPaid(@PathVariable Long id, @RequestBody PaymentPaidRequest paymentPaidRequest, Authentication authentication) {
        try {
            Long userId = (Long) authentication.getPrincipal();
            Payment payment = paymentService.findById(id);
            if (!payment.getUser().getId().equals(userId)) {
                return ResponseEntity.badRequest().build();
            }
            payment = paymentService.markAsPaid(id, paymentPaidRequest.method());
            return ResponseEntity.ok(payment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

}
