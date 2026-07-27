CREATE TABLE carts (
    id          BIGSERIAL PRIMARY KEY,
    account_id  BIGINT NOT NULL,
    created_at  TIMESTAMP NOT NULL,
    updated_at  TIMESTAMP
);

CREATE TABLE cart_items (
    id          BIGSERIAL PRIMARY KEY,
    cart_id     BIGINT NOT NULL REFERENCES carts (id),
    product_id  BIGINT NOT NULL,
    quantity    INTEGER,
    created_at  TIMESTAMP NOT NULL,
    updated_at  TIMESTAMP
);
