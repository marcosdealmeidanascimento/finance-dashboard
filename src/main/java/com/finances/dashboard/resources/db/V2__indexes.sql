-- Index
CREATE INDEX idx_payment_user
ON payments(user_id)
WHERE deleted_at IS NULL;


CREATE INDEX idx_income_user
ON incomes(user_id)
WHERE deleted_at IS NULL;


CREATE INDEX idx_payment_user_status
ON payments(user_id, status)
WHERE deleted_at IS NULL;


CREATE INDEX idx_income_summary
ON incomes(received_date, user_id)
WHERE deleted_at IS NULL;


CREATE INDEX idx_payment_summary
ON payments(due_date, user_id)
WHERE deleted_at IS NULL;