package com.finances.dashboard.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.finances.dashboard.model.Charge;

public interface ChargeRepository extends JpaRepository<Charge, Long> {
    List<Charge> findByUser_Id(Long userId);
}
