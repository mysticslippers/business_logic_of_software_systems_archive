CREATE TYPE PROVIDER_PAYMENT_STATUS AS ENUM (
    'NEW',
    'WAITING_FOR_USER',
    'PROCESSING',
    'PAID',
    'FAILED',
    'EXPIRED'
);

CREATE TYPE PROVIDER_WEBHOOK_STATUS AS ENUM (
    'NEW',
    'SENT',
    'FAILED'
);

CREATE TABLE IF NOT EXISTS MERCHANTS (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    api_key VARCHAR(255) NOT NULL UNIQUE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_merchants_api_key
    ON MERCHANTS USING btree (api_key);

CREATE TABLE IF NOT EXISTS PROVIDER_PAYMENTS (
    id BIGSERIAL PRIMARY KEY,

    merchant_id BIGINT REFERENCES MERCHANTS (id) ON DELETE RESTRICT,

    merchant_payment_ref VARCHAR(255) NOT NULL,

    amount_cents INT NOT NULL CONSTRAINT provider_non_negative_amount CHECK (amount_cents >= 0),
    currency CHAR(3) NOT NULL DEFAULT 'RUB',

    status PROVIDER_PAYMENT_STATUS NOT NULL DEFAULT 'NEW',

    retry_count SMALLINT NOT NULL DEFAULT 0 CONSTRAINT provider_non_negative_retry CHECK (retry_count >= 0),
    max_retries SMALLINT NOT NULL DEFAULT 3 CONSTRAINT provider_non_negative_max_retry CHECK (max_retries >= 0),

    payment_url TEXT NOT NULL,

    expires_at TIMESTAMP,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT provider_unique_payment_ref_per_merchant UNIQUE (merchant_id, merchant_payment_ref)
);

CREATE INDEX IF NOT EXISTS idx_provider_payments_merchant_id
    ON PROVIDER_PAYMENTS USING btree (merchant_id);

CREATE INDEX IF NOT EXISTS idx_provider_payments_status
    ON PROVIDER_PAYMENTS USING btree (status);

CREATE INDEX IF NOT EXISTS idx_provider_payments_expires_at
    ON PROVIDER_PAYMENTS USING btree (expires_at);

CREATE TYPE PROVIDER_ATTEMPT_STATUS AS ENUM (
    'STARTED',
    'SUCCESS',
    'ERROR',
    'TIMEOUT'
);

CREATE TABLE IF NOT EXISTS PROVIDER_PAYMENT_ATTEMPTS (
    id BIGSERIAL PRIMARY KEY,
    payment_id BIGINT REFERENCES PROVIDER_PAYMENTS (id) ON DELETE CASCADE,

    attempt_no SMALLINT NOT NULL CONSTRAINT provider_attempt_no_positive CHECK (attempt_no >= 1),
    status PROVIDER_ATTEMPT_STATUS NOT NULL,

    error_code VARCHAR(64),
    error_message TEXT,

    started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    finished_at TIMESTAMP,

    CONSTRAINT provider_unique_attempt_no UNIQUE (payment_id, attempt_no)
);

CREATE INDEX IF NOT EXISTS idx_provider_payment_attempts_payment_id
    ON PROVIDER_PAYMENT_ATTEMPTS USING btree (payment_id);

CREATE TYPE PROVIDER_WEBHOOK_EVENT AS ENUM (
    'PAYMENT_PAID',
    'PAYMENT_FAILED',
    'PAYMENT_EXPIRED'
);

CREATE TABLE IF NOT EXISTS PROVIDER_WEBHOOK_OUTBOX (
    id BIGSERIAL PRIMARY KEY,
    payment_id BIGINT REFERENCES PROVIDER_PAYMENTS (id) ON DELETE CASCADE,

    event_type PROVIDER_WEBHOOK_EVENT NOT NULL,
    target_url TEXT NOT NULL,

    payload JSONB NOT NULL DEFAULT '{}'::jsonb,

    status PROVIDER_WEBHOOK_STATUS NOT NULL DEFAULT 'NEW',
    attempts SMALLINT NOT NULL DEFAULT 0 CONSTRAINT provider_webhook_attempts_non_negative CHECK (attempts >= 0),

    last_error TEXT,
    last_attempt_at TIMESTAMP,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_provider_webhook_outbox_status
    ON PROVIDER_WEBHOOK_OUTBOX USING btree (status);

CREATE INDEX IF NOT EXISTS idx_provider_webhook_outbox_payment_id
    ON PROVIDER_WEBHOOK_OUTBOX USING btree (payment_id);