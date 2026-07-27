# My Store

REST API интернет-магазина на Spring Boot в виде **7 микросервисов**. Покрывает цикл: аккаунт → каталог → корзина → заказ → оплата → курьер/доставка.

OpenAPI-описание: **My Store API**. Авторизация отсутствует — API открытый.

## Стек

- Java 17, Spring Boot 4.1, Maven (`./mvnw`)
- Spring Web MVC, Data JPA / Hibernate (`ddl-auto=update`)
- PostgreSQL 14 (database per service), Bean Validation, MapStruct, Lombok
- springdoc-openapi 3.0.2
- Kafka (событие `payment.succeeded` → заказ `PAID`)

## Домены и сервисы

| Сервис | Порт | API | БД (порт PG) |
|--------|------|-----|--------------|
| account-service | 8081 | `/rest/accounts` | account_db (5433) |
| products-service | 8082 | `/rest/products` | product_db (5434) |
| cart-service | 8083 | `/rest/carts` | cart_db (5435) |
| order-service | 8084 | `/rest/orders` | order_db (5436) |
| payment-service | 8085 | `/rest/payments` | payment_db (5437) |
| courier-service | 8086 | `/rest/couriers` | courier_db (5438) |
| delivery-service | 8087 | `/rest/deliveries` | delivery_db (5439) |

Слои: Controller → Service → Repository + DTO/MapStruct. Общие ошибки и URL сервисов — в `common_lib`.

## Требования

- JDK 17+
- Docker (Postgres × 7, Kafka)
- Maven Wrapper уже в репозитории

## Быстрый старт

Подробный сценарий ручного тестирования: [scripts/local-dev.md](scripts/local-dev.md).

### 1. Переменные окружения

**Корень** — `.env` для Docker Compose (учётные данные Postgres, Kafka):

```bash
cp .env.example .env
```

```env
POSTGRES_USER=postgres
POSTGRES_PASSWORD=<password>
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

**Каждый сервис** — свой `{service}/.env` (порт и имя БД, URL соседних сервисов):

```bash
for s in account_service products_service cart_service order_service payment_service courier_service delivery_service; do
  cp "$s/.env.example" "$s/.env"
done
```

URL микросervисов по умолчанию — в `common_lib/.../application-services.properties`, переопределяются через `{service}/.env` (`services.*.url`).

Конфиг подключается через `spring.config.import`:
`classpath:application-services.properties` → `{service}/.env`.

> Запускать сервисы нужно из **корня** проекта: пути `file:account_service/.env` и т.д. относительны корню репозитория.

### 2. Базы данных (7 отдельных Postgres)

```bash
docker compose -f docker-compose-databases.yml up -d
```

### 3. Kafka

```bash
docker compose -f docker-compose-kafka.yml up -d
```

Kafka UI (опционально): http://localhost:9091

### 4. Запуск сервисов

Один сервис:

```bash
./mvnw -pl payment_service spring-boot:run
```

Все сразу:

```bash
./scripts/start-all-services.sh
./scripts/stop-all-services.sh
```

| Что | URL |
|-----|-----|
| Swagger UI | http://localhost:8081/swagger-ui.html … http://localhost:8087/swagger-ui.html |
| OpenAPI JSON | http://localhost:8081/v3/api-docs … |

### 5. Тесты

```bash
./mvnw test
```

## Kafka flow

`payment-service` публикует `payment.succeeded` → `order-service` переводит заказ в `PAID` (consumer после commit транзакции).

## Структура репозитория

```
my_store/
├── pom.xml
├── common_lib/
├── account_service/
├── products_service/
├── cart_service/
├── order_service/
├── payment_service/
├── courier_service/
├── delivery_service/
├── docker-compose-databases.yml
├── docker-compose-kafka.yml
└── scripts/
```
