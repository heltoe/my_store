# My Store

REST API интернет-магазина на Spring Boot в виде **7 микросервисов** + **API Gateway**. Покрывает цикл: аккаунт → каталог → корзина → заказ → оплата → курьер/доставка.

OpenAPI-описание: **My Store API**. Авторизация отсутствует — API открытый.

## Стек

- Java 17, Spring Boot 4.1, Maven (`./mvnw`)
- Spring Web MVC, Data JPA / Hibernate, **Flyway** (`ddl-auto=validate`)
- PostgreSQL 14 (database per service), Bean Validation, MapStruct, Lombok
- springdoc-openapi 3.0.2
- Kafka (событие `payment.succeeded` → заказ `PAID`)
- Spring Cloud Gateway 2025.1 (единая точка входа `:8080`)

## Домены и сервисы

| Сервис | Порт | API | БД (порт PG) |
|--------|------|-----|--------------|
| **api-gateway** | **8080** | прокси `/rest/**` | — |
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
- Docker (Postgres × 7, Kafka, приложения)
- Maven Wrapper уже в репозитории

## Быстрый старт

Подробный сценарий ручного тестирования: [scripts/local-dev.md](scripts/local-dev.md).

### Вариант A: Docker full stack (рекомендуется)

```bash
cp .env.example .env
# задайте POSTGRES_PASSWORD

docker compose up -d --build
```

Единая точка входа: **http://localhost:8080**

| Что | URL |
|-----|-----|
| CRUD через gateway | http://localhost:8080/rest/accounts … /rest/deliveries |
| Health gateway | http://localhost:8080/actuator/health |
| Kafka UI | http://localhost:9091 |

Остановка: `docker compose down`

> Для infra-only (только БД + Kafka без приложений) по-прежнему доступны `docker-compose-databases.yml` и `docker-compose-kafka.yml`.

### Вариант B: Локальная разработка (Maven)

#### 1. Переменные окружения

**Корень** — `.env` для Docker Compose:

```bash
cp .env.example .env
```

```env
POSTGRES_USER=postgres
POSTGRES_PASSWORD=<password>
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

**Каждый сервис** — `{service}/.env`:

```bash
for s in account_service products_service cart_service order_service payment_service courier_service delivery_service; do
  cp "$s/.env.example" "$s/.env"
done
```

URL соседних сервисов — в `common_lib/.../application-services.properties`, переопределяются через `{service}/.env` (`services.account`, `services.products`, …).

#### 2. Базы данных и Kafka

```bash
docker compose -f docker-compose-databases.yml up -d
docker compose -f docker-compose-kafka.yml up -d
```

#### 3. Запуск сервисов

```bash
./scripts/start-all-services.sh
./mvnw -pl api_gateway spring-boot:run   # gateway :8080
```

Swagger UI (напрямую): http://localhost:8081/swagger-ui.html … http://localhost:8087/swagger-ui.html

### Тесты

```bash
./mvnw test
```

## Kafka flow

`payment-service` публикует `payment.succeeded` → `order-service` переводит заказ в `PAID` (consumer после commit транзакции).

## Структура репозитория

```
my_store/
├── pom.xml
├── Dockerfile                  # multi-stage, ARG SERVICE=...
├── docker-compose.yml          # full stack
├── api_gateway/
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
