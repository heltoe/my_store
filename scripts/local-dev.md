# Локальный полный стенд (7 микросервисов)

## 1. Инфраструктура

```bash
cp .env.example .env
# отредактируйте POSTGRES_PASSWORD

docker compose -f docker-compose-databases.yml up -d
docker compose -f docker-compose-kafka.yml up -d
```

Проверка БД: `docker compose -f docker-compose-databases.yml ps`

## 2. Env сервисов

```bash
for s in account_service products_service cart_service order_service payment_service courier_service delivery_service; do
  cp "$s/.env.example" "$s/.env"
done
```

В каждом `{service}/.env` задайте `POSTGRES_PASSWORD` (тот же, что в корневом `.env`).

## 3. Запуск приложений

Из **корня** репозитория:

```bash
./scripts/start-all-services.sh
```

Логи: `.local-dev/logs/`. Остановка: `./scripts/stop-all-services.sh`.

Один сервис вручную:

```bash
bash mvnw -pl order_service spring-boot:run
```

Подождите ~30 с, пока все сервисы поднимутся.

## 4. Happy-path (Swagger)

| Шаг | Сервис | Порт | Действие |
|-----|--------|------|----------|
| 1 | account | 8081 | `POST /rest/accounts` — создать покупателя |
| 2 | products | 8082 | `POST /rest/products` — создать товар, `PATCH .../activate` |
| 3 | cart | 8083 | `POST /rest/carts` + добавить позицию |
| 4 | order | 8084 | `POST /rest/orders` — заказ из корзины, статус `ACCEPTED` |
| 5 | payment | 8085 | `POST /rest/payments` + `PATCH .../success` |
| 6 | order | 8084 | `GET /rest/orders/{id}` — статус `PAID` (через Kafka) |
| 7 | courier | 8086 | `POST /rest/couriers` |
| 8 | delivery | 8087 | `POST /rest/deliveries`, `GET .../courier/{id}/on-the-way` |

Swagger UI: `http://localhost:808X/swagger-ui.html` (X = 1…7).

## 5. Kafka UI

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
