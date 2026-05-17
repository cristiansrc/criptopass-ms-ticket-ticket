CREATE TABLE IF NOT EXISTS tickets (
    id                  BIGSERIAL       PRIMARY KEY,
    event_id            BIGINT          NOT NULL,
    event_name          VARCHAR(255)    NOT NULL,
    event_start_date    TIMESTAMPTZ     NOT NULL,
    venue_name          VARCHAR(255),
    ticket_type_id      BIGINT          NOT NULL,
    ticket_type_name    VARCHAR(255)    NOT NULL,
    ticket_type_price   NUMERIC(10, 2)  NOT NULL,
    owner_id            BIGINT          NOT NULL,
    owner_email         VARCHAR(255)    NOT NULL,
    price               NUMERIC(10, 2)  NOT NULL,
    status              VARCHAR(50)     NOT NULL DEFAULT 'PENDING_PAYMENT',
    qr_code             VARCHAR(500),
    blockchain_token_id BIGINT,
    blockchain_tx_hash  VARCHAR(255),
    seat_number         VARCHAR(50),
    order_id            VARCHAR(255),
    purchased_at        TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    validated_at        TIMESTAMPTZ,
    validated_by        BIGINT,
    version             INTEGER         NOT NULL DEFAULT 0,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_ticket_status CHECK (
        status IN ('PENDING_PAYMENT', 'ACTIVE', 'TRANSFERRED', 'VALIDATED', 'REVOKED', 'EXPIRED')
    ),
    CONSTRAINT chk_price_non_negative CHECK (price >= 0)
);

CREATE INDEX idx_tickets_owner_id ON tickets(owner_id);
CREATE INDEX idx_tickets_event_id ON tickets(event_id);
CREATE INDEX idx_tickets_qr_code ON tickets(qr_code);
CREATE INDEX idx_tickets_status ON tickets(status);
CREATE INDEX idx_tickets_order_id ON tickets(order_id);
CREATE UNIQUE INDEX idx_tickets_qr_code_unique ON tickets(qr_code) WHERE qr_code IS NOT NULL;
