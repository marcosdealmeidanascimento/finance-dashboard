explain ANALYZE
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
      AND received_date BETWEEN '2026-05-01' AND '2026-05-31'
    GROUP BY user_id
) i ON i.user_id = u.id

LEFT JOIN (
    SELECT
        user_id,
        SUM(amount) AS total_payment
    FROM payments
    WHERE deleted_at IS NULL
      AND due_date BETWEEN '2026-05-01' AND '2026-05-31'
    GROUP BY user_id
) p ON p.user_id = u.id

WHERE u.deleted_at IS NULL;
