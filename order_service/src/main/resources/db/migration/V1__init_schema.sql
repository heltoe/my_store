CREATE TABLE orders (
    id          BIGSERIAL PRIMARY KEY,
    account_id  BIGINT NOT NULL,
    status      VARCHAR(255) NOT NULL,
    created_at  TIMESTAMP NOT NULL,
    updated_at  TIMESTAMP
);

CREATE TABLE order_items (
    id          BIGSERIAL PRIMARY KEY,
    order_id    BIGINT NOT NULL REFERENCES orders (id),
    price       DOUBLE PRECISION NOT NULL,
    quantity    INTEGER NOT NULL,
    product_id  BIGINT NOT NULL,
    created_at  TIMESTAMP NOT NULL,
    updated_at  TIMESTAMP
);
