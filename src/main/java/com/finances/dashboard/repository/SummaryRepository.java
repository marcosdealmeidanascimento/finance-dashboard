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
                u.name AS name,
                u.email AS email,

                COALESCE((
                    SELECT SUM(i.amount)
                    FROM incomes i
                    WHERE i.user_id = u.id
                      AND i.deleted_at IS NULL
                      AND i.received_date BETWEEN :startDate AND :endDate
                ), 0) AS totalIncome,

                COALESCE((
                    SELECT SUM(p.amount)
                    FROM payments p
                    WHERE p.user_id = u.id
                      AND p.deleted_at IS NULL
                      AND p.due_date BETWEEN :startDate AND :endDate
                ), 0) AS totalPayments

            FROM users u
            WHERE u.deleted_at IS NULL
            """, nativeQuery = true)
    List<UserSummaryProjection> findAllSummaries(
            LocalDate startDate,
            LocalDate endDate);
}
