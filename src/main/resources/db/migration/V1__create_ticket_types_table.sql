CREATE TABLE IF NOT EXISTS ticket_types (
    id              BIGSERIAL       PRIMARY KEY,
    event_id        BIGINT          NOT NULL,
    name            VARCHAR(255)    NOT NULL,
    description     TEXT,
    price           NUMERIC(10, 2)  NOT NULL,
    quantity        INTEGER         NOT NULL,
    available_quantity INTEGER      NOT NULL,
    max_per_user    INTEGER         NOT NULL DEFAULT 1,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_available_quantity_non_negative CHECK (available_quantity >= 0),
    CONSTRAINT chk_quantity_positive CHECK (quantity >= 0),
    CONSTRAINT chk_price_positive CHECK (price >= 0)
);

CREATE INDEX idx_ticket_types_event_id ON ticket_types(event_id);
