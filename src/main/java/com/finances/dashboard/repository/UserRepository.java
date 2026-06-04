package com.finances.dashboard.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.finances.dashboard.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
}
