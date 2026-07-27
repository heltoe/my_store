# Локальный полный стенд (7 микросервисов + gateway)

## Сервисы и порты

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

---

## Вариант 1: Docker full stack (рекомендуется)

```bash
cp .env.example .env
# задайте POSTGRES_PASSWORD
```

Первый запуск — сборка образов (5–10 мин), дальше быстрее:

```bash
DOCKER_BUILDKIT=1 docker compose build
docker compose up -d
```

После изменений в `pom.xml` или Dockerfile пересоберите образы:

```bash
DOCKER_BUILDKIT=1 docker compose build
docker compose up -d --force-recreate
```

Дождитесь `healthy` у всех сервисов (~2–3 мин после сборки):

```bash
docker compose ps
curl http://localhost:8080/actuator/health
```

Happy-path ниже выполняйте через **gateway :8080**. Swagger на отдельных портах в Docker с хоста недоступен — только gateway.

| Что | URL |
|-----|-----|
| CRUD через gateway | http://localhost:8080/rest/accounts … /rest/deliveries |
| Health gateway | http://localhost:8080/actuator/health |
| Kafka UI | http://localhost:9091 |

Остановка: `docker compose down`

---

## Вариант 2: Maven + infra-only Docker

### 1. Инфраструктура

```bash
cp .env.example .env
# задайте POSTGRES_PASSWORD (тот же будет в .env сервисов)

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

Дополнительно (уже есть в `.env.example`, при необходимости поправьте):

| Сервис | Переменные |
|--------|------------|
| cart, order | `services.account`, `services.products` |
| payment, delivery | `services.order` |
| courier | `services.delivery` |
| delivery | `services.courier` |
| order, payment | `KAFKA_BOOTSTRAP_SERVERS=localhost:9092` |

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
| cart | http://localhost:8083/swagger-ui.html |
| order | http://localhost:8084/swagger-ui.html |
| payment | http://localhost:8085/swagger-ui.html |
| courier | http://localhost:8086/swagger-ui.html |
| delivery | http://localhost:8087/swagger-ui.html |

## Kafka UI

http://localhost:9091 — топик `payment.succeeded` после успешной оплаты.

## Маршруты gateway

| Predicate | Backend (Docker) | Backend (Maven) |
|-----------|------------------|-----------------|
| `/rest/accounts/**` | account-service:8081 | localhost:8081 |
| `/rest/products/**` | products-service:8082 | localhost:8082 |
| `/rest/carts/**` | cart-service:8083 | localhost:8083 |
| `/rest/orders/**` | order-service:8084 | localhost:8084 |
| `/rest/payments/**` | payment-service:8085 | localhost:8085 |
| `/rest/couriers/**` | courier-service:8086 | localhost:8086 |
| `/rest/deliveries/**` | delivery-service:8087 | localhost:8087 |
