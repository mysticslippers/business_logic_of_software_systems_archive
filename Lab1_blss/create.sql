CREATE TABLE IF NOT EXISTS COURSES(
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    price_cents INTEGER NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT positive_course_price CHECK (price_cents >= 0),
    CONSTRAINT readable_course_title CHECK (char_length(title) <= 255)
    );

CREATE INDEX IF NOT EXISTS idx_courses_active ON COURSES USING btree (active);
CREATE INDEX IF NOT EXISTS idx_courses_title ON COURSES USING btree (title);


CREATE TABLE IF NOT EXISTS ENROLLMENTS(
    id UUID PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL,
    course_id BIGINT REFERENCES COURSES (id) ON DELETE RESTRICT,
    price_cents INTEGER NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT readable_user_id CHECK (char_length(user_id) <= 64),
    CONSTRAINT enrollment_has_course CHECK (course_id IS NOT NULL),
    CONSTRAINT positive_enrollment_price CHECK (price_cents >= 0),
    CONSTRAINT enrollment_status_check CHECK (status IN (
        'NEW','WAITING_PAYMENT','PAID','CANCELED','EXPIRED'
    ))
    );

CREATE INDEX IF NOT EXISTS idx_enrollments_user_id ON ENROLLMENTS USING btree (user_id);
CREATE INDEX IF NOT EXISTS idx_enrollments_course_id ON ENROLLMENTS USING btree (course_id);
CREATE INDEX IF NOT EXISTS idx_enrollments_status ON ENROLLMENTS USING btree (status);
CREATE INDEX IF NOT EXISTS idx_enrollments_price ON ENROLLMENTS USING btree (price_cents);

CREATE UNIQUE INDEX IF NOT EXISTS uq_enrollments_user_course_active
    ON ENROLLMENTS(user_id, course_id)
    WHERE status IN ('NEW', 'WAITING_PAYMENT');

CREATE UNIQUE INDEX IF NOT EXISTS uq_enrollments_id_price
    ON ENROLLMENTS USING btree (id, price_cents);


CREATE TABLE IF NOT EXISTS PAYMENTS(
    id UUID PRIMARY KEY,
    enrollment_id UUID NOT NULL,
    amount_cents INTEGER NOT NULL,
    status VARCHAR(32) NOT NULL,
    provider_payment_id VARCHAR(255) UNIQUE,
    provider_name VARCHAR(100),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT positive_payment_amount CHECK (amount_cents >= 0),
    CONSTRAINT readable_provider_name CHECK (provider_name IS NULL OR char_length(provider_name) <= 100),
    CONSTRAINT readable_provider_payment_id CHECK (provider_payment_id IS NULL OR char_length(provider_payment_id) <= 255),
    CONSTRAINT payment_status_check CHECK (status IN (
        'INIT','CONFIRMED','FAILED','TIMEOUT'
    )),

    CONSTRAINT fk_payments_enrollment
    FOREIGN KEY (enrollment_id)
    REFERENCES ENROLLMENTS (id)
    ON DELETE CASCADE,

    CONSTRAINT fk_payments_amount_matches_enrollment
    FOREIGN KEY (enrollment_id, amount_cents)
    REFERENCES ENROLLMENTS (id, price_cents)
    ON DELETE CASCADE
    );

CREATE UNIQUE INDEX IF NOT EXISTS uq_payments_enrollment_id ON PAYMENTS USING btree (enrollment_id);

CREATE UNIQUE INDEX IF NOT EXISTS idx_payments_provider_payment_id ON PAYMENTS USING btree (provider_payment_id);
CREATE INDEX IF NOT EXISTS idx_payments_status ON PAYMENTS USING btree (status);


CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_courses_updated_at ON COURSES;
CREATE TRIGGER trg_courses_updated_at
    BEFORE UPDATE ON COURSES
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

DROP TRIGGER IF EXISTS trg_enrollments_updated_at ON ENROLLMENTS;
CREATE TRIGGER trg_enrollments_updated_at
    BEFORE UPDATE ON ENROLLMENTS
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

DROP TRIGGER IF EXISTS trg_payments_updated_at ON PAYMENTS;
CREATE TRIGGER trg_payments_updated_at
    BEFORE UPDATE ON PAYMENTS
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();


CREATE OR REPLACE FUNCTION enforce_enrollment_paid_has_confirmed_payment()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.status = 'PAID' AND (OLD.status IS DISTINCT FROM NEW.status) THEN
        IF NOT EXISTS (
            SELECT 1
            FROM PAYMENTS p
            WHERE p.enrollment_id = NEW.id
              AND p.status = 'CONFIRMED'
        ) THEN
            RAISE EXCEPTION 'Cannot set ENROLLMENTS.status=PAID without CONFIRMED payment for enrollment_id=%', NEW.id;
END IF;
END IF;

RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_enrollment_paid_has_payment ON ENROLLMENTS;
CREATE TRIGGER trg_enrollment_paid_has_payment
    BEFORE UPDATE OF status ON ENROLLMENTS
    FOR EACH ROW EXECUTE FUNCTION enforce_enrollment_paid_has_confirmed_payment();


CREATE OR REPLACE FUNCTION enforce_payment_confirmed_enrollment_active()
RETURNS TRIGGER AS $$
DECLARE
e_status VARCHAR(32);
BEGIN
    IF NEW.status = 'CONFIRMED' AND (OLD.status IS DISTINCT FROM NEW.status) THEN
SELECT status INTO e_status
FROM ENROLLMENTS
WHERE id = NEW.enrollment_id;

IF e_status IN ('CANCELED', 'EXPIRED') THEN
            RAISE EXCEPTION 'Cannot CONFIRM payment for enrollment_id=% because enrollment status is %', NEW.enrollment_id, e_status;
END IF;
END IF;

RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_payment_confirmed_enrollment_active ON PAYMENTS;
CREATE TRIGGER trg_payment_confirmed_enrollment_active
    BEFORE UPDATE OF status ON PAYMENTS
    FOR EACH ROW EXECUTE FUNCTION enforce_payment_confirmed_enrollment_active();
