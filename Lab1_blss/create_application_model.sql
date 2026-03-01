CREATE TYPE ENROLLMENT_STATUS AS ENUM(
    'PENDING_PAYMENT',
    'ACTIVE',
    'REJECTED'
);

CREATE TYPE PAYMENT_STATUS AS ENUM(
    'NEW',
    'PAID',
    'FAILED',
    'EXPIRED'
);

CREATE TABLE IF NOT EXISTS USERS(
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    full_name VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT valid_email CHECK (email ~ '^[a-zA-Z0-9.!#$%&''*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$')
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_users_email ON USERS USING btree (email);

CREATE TABLE IF NOT EXISTS COURSES(
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    price_cents INT NOT NULL CONSTRAINT non_negative_price CHECK (price_cents >= 0),
    currency CHAR(3) NOT NULL DEFAULT 'RUB',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_courses_is_active ON COURSES USING btree (is_active);

CREATE TABLE IF NOT EXISTS ENROLLMENTS(
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES USERS (id) ON DELETE RESTRICT,
    course_id BIGINT REFERENCES COURSES (id) ON DELETE RESTRICT,
    status ENROLLMENT_STATUS NOT NULL,
    reject_reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_enrollments_user_id ON ENROLLMENTS USING btree (user_id);
CREATE INDEX IF NOT EXISTS idx_enrollments_course_id ON ENROLLMENTS USING btree (course_id);
CREATE INDEX IF NOT EXISTS idx_enrollments_status ON ENROLLMENTS USING btree (status);

CREATE UNIQUE INDEX IF NOT EXISTS idx_unique_open_enrollment
ON ENROLLMENTS USING btree (user_id, course_id)
WHERE status IN ('PENDING_PAYMENT', 'ACTIVE');

CREATE TABLE IF NOT EXISTS PAYMENTS(
    id BIGSERIAL PRIMARY KEY,
    enrollment_id BIGINT REFERENCES ENROLLMENTS (id) ON DELETE CASCADE,
    provider_payment_id VARCHAR(255) UNIQUE,
    amount_cents INT NOT NULL CONSTRAINT non_negative_amount CHECK (amount_cents >= 0),
    currency CHAR(3) NOT NULL DEFAULT 'RUB',
    status PAYMENT_STATUS NOT NULL,
    retry_count SMALLINT NOT NULL DEFAULT 0 CONSTRAINT non_negative_retry CHECK (retry_count >= 0),
    max_retries SMALLINT NOT NULL DEFAULT 3 CONSTRAINT non_negative_max_retry CHECK (max_retries >= 0),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_payment_per_enrollment UNIQUE (enrollment_id)
);

CREATE INDEX IF NOT EXISTS idx_payments_status ON PAYMENTS USING btree (status);
CREATE INDEX IF NOT EXISTS idx_payments_provider_payment_id ON PAYMENTS USING btree (provider_payment_id);