CREATE TYPE COURSE_STATUS AS ENUM(
    'DRAFT',
    'PUBLISHED',
    'CLOSED'
);

CREATE TYPE ENROLLMENT_STATUS AS ENUM(
    'NEW',
    'REJECTED',
    'PENDING_PAYMENT',
    'ENROLLED',
    'PAYMENT_FAILED',
    'EXPIRED',
    'CANCELED',
    'REFUNDED'
);

CREATE TYPE PAYMENT_STATUS AS ENUM(
    'CREATED',
    'PENDING',
    'PAID',
    'FAILED',
    'EXPIRED',
    'REFUNDED'
);

CREATE TYPE ACCESS_STATUS AS ENUM(
    'ACTIVE',
    'REVOKED'
);

CREATE TYPE WEBHOOK_PROCESS_STATUS AS ENUM(
    'RECEIVED',
    'PROCESSED',
    'IGNORED',
    'ERROR'
);

CREATE TYPE OUTBOX_STATUS AS ENUM(
    'NEW',
    'SENT',
    'ERROR'
);

CREATE TYPE REFUND_STATUS AS ENUM(
    'REQUESTED',
    'APPROVED',
    'REJECTED',
    'SENT',
    'COMPLETED',
    'FAILED'
);

CREATE TABLE IF NOT EXISTS USERS(
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(32),
    full_name VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255),

    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT valid_email CHECK (
        email ~ '^[a-zA-Z0-9.!#$%&''*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$'
    )
);

CREATE TABLE IF NOT EXISTS COURSES(
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,

    price_cents INTEGER NOT NULL CONSTRAINT non_negative_price CHECK (price_cents >= 0),
    currency CHAR(3) NOT NULL,

    capacity INTEGER CONSTRAINT positive_capacity CHECK (capacity IS NULL OR capacity > 0),

    status COURSE_STATUS NOT NULL DEFAULT 'DRAFT',
    starts_at TIMESTAMPTZ,
    enrollment_open BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS ENROLLMENTS(
    id BIGSERIAL PRIMARY KEY,

    user_id BIGINT NOT NULL REFERENCES USERS (id) ON DELETE RESTRICT,
    course_id BIGINT NOT NULL REFERENCES COURSES (id) ON DELETE RESTRICT,

    status ENROLLMENT_STATUS NOT NULL DEFAULT 'NEW',
    rejection_reason TEXT,

    payment_expires_at TIMESTAMPTZ,

    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT readable_rejection_reason CHECK (
        rejection_reason IS NULL OR char_length(rejection_reason) <= 500
    )
);

CREATE TABLE IF NOT EXISTS PAYMENTS(
    id BIGSERIAL PRIMARY KEY,

    enrollment_id BIGINT NOT NULL REFERENCES ENROLLMENTS (id) ON DELETE CASCADE,

    amount_cents INTEGER NOT NULL CONSTRAINT non_negative_amount CHECK (amount_cents >= 0),
    currency CHAR(3) NOT NULL,

    status PAYMENT_STATUS NOT NULL DEFAULT 'CREATED',

    provider VARCHAR(64) NOT NULL DEFAULT 'MINIBANK',
    provider_payment_id VARCHAR(128),
    payment_link TEXT,

    expires_at TIMESTAMPTZ,
    paid_at TIMESTAMPTZ,

    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT paid_at_only_when_paid CHECK (
        paid_at IS NULL OR status IN ('PAID', 'REFUNDED')
    )
);

CREATE TABLE IF NOT EXISTS COURSE_ACCESS(
    id BIGSERIAL PRIMARY KEY,

    user_id BIGINT NOT NULL REFERENCES USERS (id) ON DELETE RESTRICT,
    course_id BIGINT NOT NULL REFERENCES COURSES (id) ON DELETE RESTRICT,

    status ACCESS_STATUS NOT NULL DEFAULT 'ACTIVE',
    granted_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    revoked_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT revoked_at_when_revoked CHECK (
        (status = 'REVOKED' AND revoked_at IS NOT NULL) OR
        (status = 'ACTIVE' AND revoked_at IS NULL)
    )
);

CREATE TABLE IF NOT EXISTS REFUNDS(
    id BIGSERIAL PRIMARY KEY,

    payment_id BIGINT NOT NULL REFERENCES PAYMENTS (id) ON DELETE RESTRICT,

    amount_cents INTEGER NOT NULL CONSTRAINT positive_refund_amount CHECK (amount_cents > 0),
    status REFUND_STATUS NOT NULL DEFAULT 'REQUESTED',
    reason TEXT,

    provider_refund_id VARCHAR(128),

    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    completed_at TIMESTAMPTZ,

    CONSTRAINT readable_refund_reason CHECK (
        reason IS NULL OR char_length(reason) <= 500
    )
);

CREATE TABLE IF NOT EXISTS WEBHOOK_EVENTS(
    id BIGSERIAL PRIMARY KEY,

    provider VARCHAR(64) NOT NULL,
    event_id VARCHAR(128),

    provider_payment_id VARCHAR(128) NOT NULL,
    event_type VARCHAR(64) NOT NULL,
    payload JSONB NOT NULL,

    process_status WEBHOOK_PROCESS_STATUS NOT NULL DEFAULT 'RECEIVED',
    error_message TEXT,

    received_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    processed_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS OUTBOX_MESSAGES(
    id BIGSERIAL PRIMARY KEY,

    aggregate_type VARCHAR(64) NOT NULL,
    aggregate_id BIGINT NOT NULL,
    event_type VARCHAR(64) NOT NULL,
    payload JSONB NOT NULL,

    status OUTBOX_STATUS NOT NULL DEFAULT 'NEW',
    attempts INTEGER NOT NULL DEFAULT 0,
    last_error TEXT,

    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT non_negative_attempts CHECK (attempts >= 0)
);


CREATE UNIQUE INDEX IF NOT EXISTS IDX_USERS_EMAIL ON USERS USING btree (lower(email));
CREATE UNIQUE INDEX IF NOT EXISTS IDX_USERS_PHONE ON USERS USING btree (phone) WHERE phone IS NOT NULL;

CREATE INDEX IF NOT EXISTS IDX_COURSES_STATUS ON COURSES USING btree (status);
CREATE INDEX IF NOT EXISTS IDX_COURSES_ENROLLMENT_OPEN ON COURSES USING btree (enrollment_open);

CREATE INDEX IF NOT EXISTS IDX_ENROLLMENTS_USER_ID ON ENROLLMENTS USING btree (user_id);
CREATE INDEX IF NOT EXISTS IDX_ENROLLMENTS_COURSE_ID ON ENROLLMENTS USING btree (course_id);
CREATE INDEX IF NOT EXISTS IDX_ENROLLMENTS_STATUS ON ENROLLMENTS USING btree (status);

CREATE UNIQUE INDEX IF NOT EXISTS IDX_ACTIVE_ENROLLMENT_PER_USER_COURSE
ON ENROLLMENTS USING btree (user_id, course_id)
WHERE status IN ('NEW', 'PENDING_PAYMENT', 'ENROLLED');

CREATE INDEX IF NOT EXISTS IDX_PAYMENTS_ENROLLMENT_ID ON PAYMENTS USING btree (enrollment_id);
CREATE INDEX IF NOT EXISTS IDX_PAYMENTS_STATUS ON PAYMENTS USING btree (status);

CREATE UNIQUE INDEX IF NOT EXISTS IDX_PAYMENTS_PROVIDER_PAYMENT_ID
ON PAYMENTS USING btree (provider_payment_id)
WHERE provider_payment_id IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS IDX_ACTIVE_PAYMENT_PER_ENROLLMENT
ON PAYMENTS USING btree (enrollment_id)
WHERE status IN ('CREATED', 'PENDING');

CREATE UNIQUE INDEX IF NOT EXISTS IDX_ACCESS_ACTIVE_USER_COURSE
ON COURSE_ACCESS USING btree (user_id, course_id)
WHERE status = 'ACTIVE';

CREATE INDEX IF NOT EXISTS IDX_REFUNDS_PAYMENT_ID ON REFUNDS USING btree (payment_id);
CREATE INDEX IF NOT EXISTS IDX_REFUNDS_STATUS ON REFUNDS USING btree (status);

CREATE UNIQUE INDEX IF NOT EXISTS IDX_REFUNDS_PROVIDER_REFUND_ID
ON REFUNDS USING btree (provider_refund_id)
WHERE provider_refund_id IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS IDX_WEBHOOK_PROVIDER_EVENT_ID
ON WEBHOOK_EVENTS USING btree (provider, event_id)
WHERE event_id IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS IDX_WEBHOOK_DEDUPE_FALLBACK
ON WEBHOOK_EVENTS USING btree (provider, provider_payment_id, event_type);

CREATE INDEX IF NOT EXISTS IDX_WEBHOOK_PROCESS_STATUS
ON WEBHOOK_EVENTS USING btree (process_status, received_at);

CREATE INDEX IF NOT EXISTS IDX_OUTBOX_STATUS
ON OUTBOX_MESSAGES USING btree (status, created_at);