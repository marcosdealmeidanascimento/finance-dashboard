package com.finances.dashboard.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.finances.dashboard.dto.request.ChargeCreateRequest;
import com.finances.dashboard.dto.request.ChargeUpdateRequest;
import com.finances.dashboard.model.Charge;
import com.finances.dashboard.model.User;
import com.finances.dashboard.service.ChargeService;
import com.finances.dashboard.service.JwtService;
import com.finances.dashboard.service.PaymentService;
import com.finances.dashboard.service.UserService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("api/charges")
@SecurityRequirement(name = "bearerAuth")
public class ChargeController {
    private final ChargeService chargeService;
    private final PaymentService paymentService;
    private final UserService userService;
    private final JwtService jwtService;

    public ChargeController(ChargeService chargeService, PaymentService paymentService, UserService userService, JwtService jwtService) {
        this.chargeService = chargeService;
        this.paymentService = paymentService;
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @GetMapping("/me")
    public ResponseEntity<List<Charge>> getCurrentUserCharges(Authentication authentication) {
        try {
            Long userId = (Long) authentication.getPrincipal();
            List<Charge> charges = chargeService.findByUserIdAndDeletedAtIsNull(userId);
            return ResponseEntity.ok(charges);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Charge> getChargeById(@PathVariable Long id) {
        try {
            Charge charge = chargeService.findById(id);
            return ResponseEntity.ok(charge);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<Charge>> getAllChargesIncludingDeleted(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        try {
            List<Charge> charges = chargeService.findByUserId(userId);
            return ResponseEntity.ok(charges);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping
    public ResponseEntity<Charge> createCharge(@RequestBody ChargeCreateRequest charge, Authentication authentication) {
        try {
            User user = userService.findById((Long) authentication.getPrincipal());
            Charge createdCharge = chargeService.create(charge, user);
            paymentService.create(charge.description(), createdCharge);
            return ResponseEntity.ok(createdCharge);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Charge> updateCharge(@PathVariable Long id, @RequestBody ChargeUpdateRequest charge) {
        try {
            Charge updatedCharge = chargeService.update(id, charge);
            return ResponseEntity.ok(updatedCharge);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCharge(@PathVariable Long id) {
        try {
            Charge charge = chargeService.findById(id);
            if (charge == null) {
                return ResponseEntity.notFound().build();
            }
            chargeService.softDelete(charge);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

}
