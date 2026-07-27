CREATE TABLE products (
    id               BIGSERIAL PRIMARY KEY,
    name             VARCHAR(255) NOT NULL,
    normalized_name  VARCHAR(255) NOT NULL UNIQUE,
    description      VARCHAR(255) NOT NULL,
    price            DOUBLE PRECISION NOT NULL,
    quantity         INTEGER NOT NULL,
    is_active        BOOLEAN NOT NULL,
    created_at       TIMESTAMP NOT NULL,
    updated_at       TIMESTAMP
);
