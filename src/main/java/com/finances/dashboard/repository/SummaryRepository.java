package com.finances.dashboard.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.finances.dashboard.dto.response.UserSummaryProjection;
import com.finances.dashboard.model.User;

@Repository
public interface SummaryRepository extends JpaRepository<User, Long> {

    @Query(value = """
                        SELECT
                u.id AS userId,
                u.name,
                u.email,
                COALESCE(i.total_income, 0) AS totalIncome,
                COALESCE(p.total_payment, 0) AS totalPayments
            FROM users u

            LEFT JOIN (
                SELECT
                    user_id,
                    SUM(amount) AS total_income
                FROM incomes
                WHERE deleted_at IS NULL
                  AND received_date BETWEEN :startDate AND :endDate
                GROUP BY user_id
            ) i ON i.user_id = u.id

            LEFT JOIN (
                SELECT
                    user_id,
                    SUM(amount) AS total_payment
                FROM payments
                WHERE deleted_at IS NULL
                  AND due_date BETWEEN :startDate AND :endDate
                GROUP BY user_id
            ) p ON p.user_id = u.id

            WHERE u.deleted_at IS NULL;
                        """, nativeQuery = true)
    List<UserSummaryProjection> findAllSummaries(
            LocalDate startDate,
            LocalDate endDate);
}
