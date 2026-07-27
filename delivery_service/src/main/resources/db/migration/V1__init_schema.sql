CREATE TABLE deliveries (
    id              BIGSERIAL PRIMARY KEY,
    order_id        BIGINT NOT NULL,
    courier_id      BIGINT NOT NULL,
    delivery_date   TIMESTAMP NOT NULL,
    delivery_place  VARCHAR(255) NOT NULL,
    description     VARCHAR(255),
    lat             DOUBLE PRECISION,
    lon             DOUBLE PRECISION,
    created_at      TIMESTAMP NOT NULL,
    updated_at      TIMESTAMP
);
