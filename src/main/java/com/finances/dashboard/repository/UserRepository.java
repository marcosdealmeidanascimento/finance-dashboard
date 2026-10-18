package com.finances.dashboard.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.finances.dashboard.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    // Emails are unique (case-insensitive) among active users only: a soft-deleted account frees its email.
    // lower() matches the uq_users_email_active index from V4.
    @Query("SELECT u FROM User u WHERE lower(u.email) = lower(:email) AND u.deletedAt IS NULL")
    Optional<User> findActiveByEmail(@Param("email") String email);

    @Query("SELECT COUNT(u) > 0 FROM User u WHERE lower(u.email) = lower(:email) AND u.deletedAt IS NULL")
    boolean existsActiveByEmail(@Param("email") String email);

    Optional<User> findByIdAndDeletedAtIsNull(Long id);

    boolean existsByIdAndDeletedAtIsNull(Long id);
}
