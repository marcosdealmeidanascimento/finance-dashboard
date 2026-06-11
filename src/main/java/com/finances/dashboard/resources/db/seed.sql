INSERT INTO users (
    name,
    email,
    password,
    created_at,
    updated_at
)
SELECT
    'User ' || gs,
    'user' || gs || '@mail.com',
    '$2a$10$hashfake',
    NOW(),
    NOW()
FROM generate_series(1,100) gs;


INSERT INTO incomes (
    description,
    amount,
    received_date,
    user_id,
    created_at,
    updated_at,
    deleted_at
)
SELECT
    'Income ' || gs,
    ROUND((random() * 5000 + 100)::numeric, 2),
    DATE '2025-01-01' + ((random() * 730)::int),
    ((random() * 99)::int + 1),
    NOW(),
    NOW(),
    CASE
        WHEN random() < 0.05
        THEN NOW()
        ELSE NULL
    END
FROM generate_series(1,100000) gs;


INSERT INTO payments (
    description,
    method,
    status,
    amount,
    due_date,
    recurring,
    user_id,
    created_at,
    updated_at,
    deleted_at
)
SELECT
    'Payment ' || gs,

    (
        ARRAY[
            'PIX',
            'CREDIT_CARD',
            'DEBIT_CARD',
            'BANK_SLIP'
        ]
    )[floor(random()*4+1)],

    (
        ARRAY[
            'PENDING',
            'PAID',
            'FAILED',
            'CANCELLED'
        ]
    )[floor(random()*4+1)],

    ROUND((random() * 5000 + 50)::numeric, 2),

    DATE '2025-01-01' + ((random() * 730)::int),

    random() < 0.3,

    ((random() * 99)::int + 1),

    NOW(),
    NOW(),

    CASE
        WHEN random() < 0.05
        THEN NOW()
        ELSE NULL
    END

FROM generate_series(1,100000) gs;