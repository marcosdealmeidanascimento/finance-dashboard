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
import com.finances.dashboard.dto.response.ChargeResponse;
import com.finances.dashboard.mapper.ChargeMapper;
import com.finances.dashboard.model.Charge;
import com.finances.dashboard.model.Payment;
import com.finances.dashboard.model.User;
import com.finances.dashboard.service.ChargeService;
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
    private final ChargeMapper mapper;

    public ChargeController(ChargeService chargeService, PaymentService paymentService, UserService userService,
            ChargeMapper mapper) {
        this.chargeService = chargeService;
        this.paymentService = paymentService;
        this.userService = userService;
        this.mapper = mapper;
    }

    @GetMapping
    public ResponseEntity<List<ChargeResponse>> getCurrentUserCharges(
            Authentication authentication) {

        try {
            Long userId = (Long) authentication.getPrincipal();
            List<Charge> charges = chargeService.findByUserIdAndDeletedAtIsNull(userId);

            List<ChargeResponse> response = charges.stream()
                    .map(mapper::toResponse)
                    .toList();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChargeResponse> getChargeById(@PathVariable Long id) {
        try {
            Charge charge = chargeService.findById(id);
            return ResponseEntity.ok(mapper.toResponse(charge));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<ChargeResponse>> getAllChargesIncludingDeleted(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        try {
            List<Charge> charges = chargeService.findByUserId(userId);
            return ResponseEntity.ok(charges.stream().map(mapper::toResponse).toList());
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping
    public ResponseEntity<ChargeResponse> createCharge(@RequestBody ChargeCreateRequest charge, Authentication authentication) {
        try {
            User user = userService.findById((Long) authentication.getPrincipal());
            Charge createdCharge = chargeService.create(charge, user);
            paymentService.create(charge.description(), createdCharge);
            return ResponseEntity.ok(mapper.toResponse(createdCharge));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ChargeResponse> updateCharge(@PathVariable Long id, @RequestBody ChargeUpdateRequest charge) {
        try {
            Charge updatedCharge = chargeService.update(id, charge);
            return ResponseEntity.ok(mapper.toResponse(updatedCharge));
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
            Payment payment = paymentService.findByChargeId(id);
            if (payment != null) {
                paymentService.markAsCanceled(payment.getId());
                paymentService.softDelete(payment);
            }
            chargeService.softDelete(charge);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

}
