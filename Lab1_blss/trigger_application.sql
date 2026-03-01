CREATE OR REPLACE FUNCTION set_updated_at()
    RETURNS TRIGGER AS
$$
BEGIN
    NEW.updated_at := CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$
LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS set_users_updated_at_trigger ON USERS;
CREATE TRIGGER set_users_updated_at_trigger
BEFORE UPDATE ON USERS
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

DROP TRIGGER IF EXISTS set_courses_updated_at_trigger ON COURSES;
CREATE TRIGGER set_courses_updated_at_trigger
BEFORE UPDATE ON COURSES
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

DROP TRIGGER IF EXISTS set_enrollments_updated_at_trigger ON ENROLLMENTS;
CREATE TRIGGER set_enrollments_updated_at_trigger
BEFORE UPDATE ON ENROLLMENTS
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

DROP TRIGGER IF EXISTS set_payments_updated_at_trigger ON PAYMENTS;
CREATE TRIGGER set_payments_updated_at_trigger
BEFORE UPDATE ON PAYMENTS
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

CREATE OR REPLACE FUNCTION set_default_enrollment_status()
    RETURNS TRIGGER AS
$$
BEGIN
    IF NEW.status IS NULL THEN
        NEW.status := 'PENDING_PAYMENT';
    END IF;
    RETURN NEW;
END;
$$
LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS set_default_enrollment_status_trigger ON ENROLLMENTS;
CREATE TRIGGER set_default_enrollment_status_trigger
BEFORE INSERT ON ENROLLMENTS
FOR EACH ROW
EXECUTE FUNCTION set_default_enrollment_status();

CREATE OR REPLACE FUNCTION set_default_payment_status()
    RETURNS TRIGGER AS
$$
BEGIN
    IF NEW.status IS NULL THEN
        NEW.status := 'NEW';
    END IF;
    RETURN NEW;
END;
$$
LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS set_default_payment_status_trigger ON PAYMENTS;
CREATE TRIGGER set_default_payment_status_trigger
BEFORE INSERT ON PAYMENTS
FOR EACH ROW
EXECUTE FUNCTION set_default_payment_status();

CREATE OR REPLACE FUNCTION sync_enrollment_status_from_payment()
    RETURNS TRIGGER AS
$$
BEGIN
    IF (TG_OP = 'UPDATE') AND (NEW.status IS DISTINCT FROM OLD.status) THEN

        IF (NEW.status = 'PAID') THEN
            UPDATE ENROLLMENTS
            SET status = 'ACTIVE',
                reject_reason = NULL
            WHERE id = NEW.enrollment_id;

        ELSIF (NEW.status = 'FAILED') THEN
            UPDATE ENROLLMENTS
            SET status = 'REJECTED',
                reject_reason = 'PAYMENT_FAILED'
            WHERE id = NEW.enrollment_id;

        ELSIF (NEW.status = 'EXPIRED') THEN
            UPDATE ENROLLMENTS
            SET status = 'REJECTED',
                reject_reason = 'PAYMENT_EXPIRED'
            WHERE id = NEW.enrollment_id;

        END IF;
    END IF;

    RETURN NULL;
END;
$$
LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS sync_enrollment_status_from_payment_trigger ON PAYMENTS;
CREATE TRIGGER sync_enrollment_status_from_payment_trigger
AFTER UPDATE ON PAYMENTS
FOR EACH ROW
EXECUTE FUNCTION sync_enrollment_status_from_payment();