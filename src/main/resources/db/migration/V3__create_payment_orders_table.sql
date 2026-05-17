CREATE TABLE IF NOT EXISTS payment_orders (
    id              BIGSERIAL       PRIMARY KEY,
    order_id        VARCHAR(255)    NOT NULL,
    payment_id      VARCHAR(255)    NOT NULL,
    status          VARCHAR(50)     NOT NULL DEFAULT 'PROCESSED',
    processed_at    TIMESTAMPTZ,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_payment_status CHECK (
        status IN ('PROCESSED', 'REJECTED', 'REFUNDED')
    )
);

CREATE UNIQUE INDEX idx_payment_orders_payment_id ON payment_orders(payment_id);
CREATE INDEX idx_payment_orders_order_id ON payment_orders(order_id);
