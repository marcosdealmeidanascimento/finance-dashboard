package com.finances.dashboard.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import com.finances.dashboard.dto.request.ChargeCreateRequest;
import com.finances.dashboard.dto.request.ChargeUpdateRequest;
import com.finances.dashboard.model.Charge;
import com.finances.dashboard.model.User;
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

    public Charge create(ChargeCreateRequest chargeCreateRequest, User user) {
        Charge charge = new Charge();
        charge.setAmount(chargeCreateRequest.amount());
        charge.setDescription(chargeCreateRequest.description());
        charge.setDueDate(chargeCreateRequest.dueDate());
        charge.setRecurring(chargeCreateRequest.recurring());
        charge.setUser(user);
        return repository.save(charge);
    }

    @Transactional
    public Charge update(Long id, ChargeUpdateRequest charge) {
        Charge existingCharge = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Charge not found with id: " + id));
        if (charge.amount() != null) existingCharge.setAmount(charge.amount());
        if (charge.description() != null) existingCharge.setDescription(charge.description());
        if (charge.dueDate() != null) existingCharge.setDueDate(charge.dueDate());
        if (charge.recurring() != null) existingCharge.setRecurring(charge.recurring());
        return repository.save(existingCharge);
    }

    public List<Charge> findByUserId(Long userId) {
        return repository.findByUser_Id(userId);
    }

    public List<Charge> findByUserIdAndDeletedAtIsNull(Long userId) {
        return repository.findByUserIdAndDeletedAtIsNull(userId);
    }

}
