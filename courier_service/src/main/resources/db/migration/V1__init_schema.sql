CREATE TABLE couriers (
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(255) NOT NULL,
    second_name   VARCHAR(255),
    last_name     VARCHAR(255) NOT NULL,
    phone_number  VARCHAR(255) NOT NULL UNIQUE,
    is_active     BOOLEAN NOT NULL,
    created_at    TIMESTAMP NOT NULL,
    updated_at    TIMESTAMP
);
