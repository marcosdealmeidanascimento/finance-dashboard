package com.finances.dashboard.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import com.finances.dashboard.model.Charge;
import com.finances.dashboard.repository.ChargeRepository;

import jakarta.transaction.Transactional;

@Service
public class ChargeService extends BaseService<Charge> {

    @Autowired
    private ChargeRepository repository;

    @Override
    protected JpaRepository<Charge, Long> getRepository() {
        return repository;
    }

    @Transactional
    public Charge update(Charge charge) {
        Charge existingCharge = repository.findById(charge.getId())
                .orElseThrow(() -> new RuntimeException("Charge not found with id: " + charge.getId()));
        existingCharge.setAmount(charge.getAmount());
        existingCharge.setDescription(charge.getDescription());
        existingCharge.setDueDate(charge.getDueDate());
        return repository.save(existingCharge);
    }

    public List<Charge> findByUserId(Long userId) {
        return repository.findAll().stream()
                .filter(charge -> charge.getUser() != null && charge.getUser().getId().equals(userId))
                .toList();
    }

}
