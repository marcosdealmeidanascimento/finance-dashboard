package com.finances.dashboard.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.finances.dashboard.model.Charge;
import com.finances.dashboard.service.ChargeService;

@RestController
@RequestMapping("api/charges")
public class ChargeController {
    private final ChargeService chargeService;

    public ChargeController(ChargeService chargeService) {
        this.chargeService = chargeService;
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

    @GetMapping
    public ResponseEntity<List<Charge>> getAllCharges() {
        try {
            List<Charge> charges = chargeService.findAllActive();
            return ResponseEntity.ok(charges);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<Charge>> getAllChargesIncludingDeleted() {
        try {
            List<Charge> charges = chargeService.findAll();
            return ResponseEntity.ok(charges);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping
    public ResponseEntity<Charge> createCharge(@RequestBody Charge charge) {
        try {
            Charge createdCharge = chargeService.save(charge);
            return ResponseEntity.ok(createdCharge);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Charge> updateCharge(@PathVariable Long id, @RequestBody Charge charge) {
        charge.setId(id);
        try {
            Charge updatedCharge = chargeService.update(charge);
            return ResponseEntity.ok(updatedCharge);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Charge>> getChargesByUserId(@PathVariable Long userId) {
        try {
            List<Charge> charges = chargeService.findByUserId(userId);
            return ResponseEntity.ok(charges);
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
