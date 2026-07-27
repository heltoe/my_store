CREATE TABLE accounts (
    id              BIGSERIAL PRIMARY KEY,
    phone_number    VARCHAR(255) NOT NULL UNIQUE,
    first_name      VARCHAR(255) NOT NULL,
    second_name     VARCHAR(255),
    last_name       VARCHAR(255) NOT NULL,
    created_at      TIMESTAMP NOT NULL,
    updated_at      TIMESTAMP
);
