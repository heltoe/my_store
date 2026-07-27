# Локальный полный стенд (7 микросервисов + gateway)

## Вариант 1: Docker full stack

```bash
cp .env.example .env
# отредактируйте POSTGRES_PASSWORD

docker compose up -d --build
```

Дождитесь healthy-статуса всех сервисов (~2–3 мин):

```bash
docker compose ps
curl http://localhost:8080/actuator/health
```

Happy-path ниже выполняйте через **gateway :8080** (Swagger на отдельных портах в Docker недоступен с хоста — только gateway).

Остановка: `docker compose down`

---

## Вариант 2: Maven + infra-only Docker

### 1. Инфраструктура

```bash
cp .env.example .env
# отредактируйте POSTGRES_PASSWORD

docker compose -f docker-compose-databases.yml up -d
docker compose -f docker-compose-kafka.yml up -d
```

### 2. Env сервисов

```bash
for s in account_service products_service cart_service order_service payment_service courier_service delivery_service; do
  cp "$s/.env.example" "$s/.env"
done
```

В каждом `{service}/.env` задайте `POSTGRES_PASSWORD` (тот же, что в корневом `.env`).

### 3. Запуск приложений

Из **корня** репозитория:

```bash
./scripts/start-all-services.sh
./mvnw -pl api_gateway spring-boot:run
```

Логи: `.local-dev/logs/`. Остановка: `./scripts/stop-all-services.sh`.

---

## Happy-path (через API Gateway :8080)

Базовый URL: `http://localhost:8080`

| Шаг | Метод и путь | Действие |
|-----|--------------|----------|
| 1 | `POST /rest/accounts` | Создать покупателя |
| 2 | `POST /rest/products` + `PATCH /rest/products/{id}/activate` | Создать и активировать товар |
| 3 | `POST /rest/carts` + добавить позицию | Корзина с товаром |
| 4 | `POST /rest/orders` | Заказ из корзины, статус `ACCEPTED` |
| 5 | `POST /rest/payments` + `PATCH /rest/payments/{id}/success` | Оплата |
| 6 | `GET /rest/orders/{id}` | Статус `PAID` (через Kafka) |
| 7 | `POST /rest/couriers` | Создать курьера |
| 8 | `POST /rest/deliveries` | Назначить доставку |

Пример smoke (account):

```bash
curl -s http://localhost:8080/rest/accounts | head
curl -s -X POST http://localhost:8080/rest/accounts \
  -H 'Content-Type: application/json' \
  -d '{"phoneNumber":"+79991234567","firstName":"Ivan","lastName":"Petrov"}'
```

### Swagger (локальный Maven-режим)

| Сервис | Swagger UI |
|--------|------------|
| account | http://localhost:8081/swagger-ui.html |
| products | http://localhost:8082/swagger-ui.html |
| … | … |
| delivery | http://localhost:8087/swagger-ui.html |

## Kafka UI

http://localhost:9091 — топик `payment.succeeded` после успешной оплаты.

## Маппинг БД

| Сервис | POSTGRES_PORT | POSTGRES_DB |
|--------|---------------|-------------|
| account | 5433 | account_db |
| products | 5434 | product_db |
| cart | 5435 | cart_db |
| order | 5436 | order_db |
| payment | 5437 | payment_db |
| courier | 5438 | courier_db |
| delivery | 5439 | delivery_db |

## Маршруты gateway

| Predicate | Backend (Docker) |
|-----------|------------------|
| `/rest/accounts/**` | account-service:8081 |
| `/rest/products/**` | products-service:8082 |
| `/rest/carts/**` | cart-service:8083 |
| `/rest/orders/**` | order-service:8084 |
| `/rest/payments/**` | payment-service:8085 |
| `/rest/couriers/**` | courier-service:8086 |
| `/rest/deliveries/**` | delivery-service:8087 |
