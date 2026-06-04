package com.finances.dashboard.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.finances.dashboard.model.Payment;
import com.finances.dashboard.service.JwtService;
import com.finances.dashboard.service.PaymentService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;

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

    @GetMapping("/me")
    public ResponseEntity<List<Payment>> getCurrentUserPayments(HttpServletRequest request) {
        try {
            String token = jwtService.extractToken(request);
            Long userId = jwtService.extractUserId(token);
            List<Payment> payments = paymentService.findByUserId(userId);
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
