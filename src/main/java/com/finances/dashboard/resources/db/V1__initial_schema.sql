CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    email VARCHAR(255),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    deleted_at TIMESTAMP WITHOUT TIME ZONE
);

ALTER TABLE users 
ADD COLUMN password VARCHAR(255);

ALTER TABLE users 
ADD CONSTRAINT uq_users_email UNIQUE (email);



CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    description VARCHAR(255),
    method VARCHAR(50),
    status VARCHAR(50),
    amount NUMERIC(15, 2),
    due_date DATE,
    recurring BOOLEAN,
    user_id BIGINT,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    deleted_at TIMESTAMP WITHOUT TIME ZONE
);


-- Garante o relacionamento @ManyToOne com Users
ALTER TABLE payments 
ADD CONSTRAINT fk_payments_user FOREIGN KEY (user_id) REFERENCES users(id);

-- Garante que o banco só aceite os status definidos no seu Enum Java
ALTER TABLE payments 
ADD CONSTRAINT chk_payments_status 
CHECK (status IN ('PENDING', 'PAID', 'FAILED', 'CANCELLED'));


CREATE TABLE incomes (
    id BIGSERIAL PRIMARY KEY,
    description VARCHAR(255),
    amount NUMERIC(15, 2),
    received_date DATE,
    user_id BIGINT,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    deleted_at TIMESTAMP WITHOUT TIME ZONE
);


ALTER TABLE incomes 
ADD CONSTRAINT fk_incomes_user 
FOREIGN KEY (user_id) REFERENCES users(id);



CREATE TABLE refresh_token (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(255) NOT NULL,
    user_id BIGINT,
    expiry_at TIMESTAMP WITHOUT TIME ZONE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    deleted_at TIMESTAMP WITHOUT TIME ZONE
);



-- Garante o relacionamento @OneToOne com Users (um refresh token por usuário)
ALTER TABLE refresh_token 
ADD CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) REFERENCES users(id),
ADD CONSTRAINT uq_refresh_token_user UNIQUE (user_id);

-- Garante a unicidade do token
ALTER TABLE refresh_token
ADD CONSTRAINT uq_refresh_token_token UNIQUE (token);