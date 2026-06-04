package com.finances.dashboard.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.finances.dashboard.model.Charge;

public interface ChargeRepository extends JpaRepository<Charge, Long> {
}
