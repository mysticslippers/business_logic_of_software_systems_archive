CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS
$$
BEGIN
    NEW.updated_at := CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$
LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS set_merchants_updated_at_trigger ON MERCHANTS;
CREATE TRIGGER set_merchants_updated_at_trigger
BEFORE UPDATE ON MERCHANTS
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

DROP TRIGGER IF EXISTS set_provider_payments_updated_at_trigger ON PROVIDER_PAYMENTS;
CREATE TRIGGER set_provider_payments_updated_at_trigger
BEFORE UPDATE ON PROVIDER_PAYMENTS
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

DROP TRIGGER IF EXISTS set_provider_webhook_outbox_updated_at_trigger ON PROVIDER_WEBHOOK_OUTBOX;
CREATE TRIGGER set_provider_webhook_outbox_updated_at_trigger
BEFORE UPDATE ON PROVIDER_WEBHOOK_OUTBOX
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

CREATE OR REPLACE FUNCTION set_default_provider_payment_url()
RETURNS TRIGGER AS
$$
BEGIN
    IF NEW.payment_url IS NULL OR NEW.payment_url = '' THEN
        UPDATE PROVIDER_PAYMENTS
        SET payment_url = 'https://mini-bank.local/pay/' || NEW.id::TEXT,
            updated_at  = CURRENT_TIMESTAMP
        WHERE id = NEW.id;
    END IF;

    RETURN NULL;
END;
$$
LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS set_default_provider_payment_url_trigger ON PROVIDER_PAYMENTS;
CREATE TRIGGER set_default_provider_payment_url_trigger
AFTER INSERT ON PROVIDER_PAYMENTS
FOR EACH ROW
EXECUTE FUNCTION set_default_provider_payment_url();


CREATE OR REPLACE FUNCTION enqueue_webhook_on_provider_payment_status_change()
RETURNS TRIGGER AS
$$
DECLARE
    v_event PROVIDER_WEBHOOK_EVENT;
BEGIN
    IF (TG_OP = 'UPDATE') AND (NEW.status IS DISTINCT FROM OLD.status) THEN

        IF (NEW.status = 'PAID') THEN
            v_event := 'PAYMENT_PAID';
        ELSIF (NEW.status = 'FAILED') THEN
            v_event := 'PAYMENT_FAILED';
        ELSIF (NEW.status = 'EXPIRED') THEN
            v_event := 'PAYMENT_EXPIRED';
        ELSE
            RETURN NULL;
        END IF;

        INSERT INTO PROVIDER_WEBHOOK_OUTBOX(payment_id, event_type, target_url, payload)
        VALUES (
            NEW.id,
            v_event,
            'https://merchant.local/webhook',
            jsonb_build_object(
                'providerPaymentId', NEW.id,
                'merchantPaymentRef', NEW.merchant_payment_ref,
                'status', NEW.status,
                'amountCents', NEW.amount_cents,
                'currency', NEW.currency
            )
        );
    END IF;

    RETURN NULL;
END;
$$
LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS enqueue_webhook_on_provider_payment_status_change_trigger ON PROVIDER_PAYMENTS;
CREATE TRIGGER enqueue_webhook_on_provider_payment_status_change_trigger
AFTER UPDATE ON PROVIDER_PAYMENTS
FOR EACH ROW
EXECUTE FUNCTION enqueue_webhook_on_provider_payment_status_change();