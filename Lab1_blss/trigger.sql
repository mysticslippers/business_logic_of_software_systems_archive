CREATE OR REPLACE FUNCTION set_updated_at()
    RETURNS TRIGGER AS
    $$
    BEGIN
        NEW.updated_at := now();
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

DROP TRIGGER IF EXISTS set_course_access_updated_at_trigger ON COURSE_ACCESS;
CREATE TRIGGER set_course_access_updated_at_trigger
BEFORE UPDATE ON COURSE_ACCESS
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

DROP TRIGGER IF EXISTS set_refunds_updated_at_trigger ON REFUNDS;
CREATE TRIGGER set_refunds_updated_at_trigger
BEFORE UPDATE ON REFUNDS
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

DROP TRIGGER IF EXISTS set_webhook_events_updated_at_trigger ON WEBHOOK_EVENTS;
CREATE TRIGGER set_webhook_events_updated_at_trigger
BEFORE UPDATE ON WEBHOOK_EVENTS
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

DROP TRIGGER IF EXISTS set_outbox_messages_updated_at_trigger ON OUTBOX_MESSAGES;
CREATE TRIGGER set_outbox_messages_updated_at_trigger
BEFORE UPDATE ON OUTBOX_MESSAGES
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();