# My Store

Монолитный REST API интернет-магазина на Spring Boot. Покрывает цикл: аккаунт → каталог → корзина → заказ → оплата → курьер/доставка.

OpenAPI-описание: **My Store API**. Авторизация отсутствует — API открытый.

## Стек

- Java 17, Spring Boot 4.1, Maven (`./mvnw`)
- Spring Web MVC, Data JPA / Hibernate (`ddl-auto=update`)
- PostgreSQL 14, Bean Validation, MapStruct, Lombok
- springdoc-openapi 3.0.2
- Тесты: JUnit 5, Mockito, Testcontainers

## Домены

| Домен    | Префикс API         | Суть                         |
|----------|---------------------|------------------------------|
| account  | `/rest/accounts`    | Покупатели                   |
| product  | `/rest/products`    | Каталог, activate/deactivate |
| cart     | `/rest/carts`       | Корзина, позиции             |
| order    | `/rest/orders`      | Заказы и смена статуса       |
| payment  | `/rest/payments`    | Платежи, success/failure     |
| courier  | `/rest/couriers`    | Курьеры                      |
| delivery | `/rest/deliveries`  | Доставка                     |

Слои: Controller → Service → Repository + DTO/MapStruct. Общие ошибки обрабатывает `GlobalErrorHandler`, фильтрация — через JPA Specification.

## Требования

- JDK 17+
- Docker (для Postgres)
- Maven Wrapper уже в репозитории

## Запуск

### 1. Переменные окружения

**Postgres** — файл `.env` в корне проекта:

```env
POSTGRES_HOST=localhost
POSTGRES_USER=postgres
POSTGRES_PASSWORD=<password>
POSTGRES_DATABASE=my_store
POSTGRES_LOCAL_PORT=5433
POSTGRES_DOCKER_PORT=5432
```

**URL микросервисов** — общий файл `common_lib/.env` (шаблон: `common_lib/.env.example`):

```bash
cp common_lib/.env.example common_lib/.env
```

```env
services.account.url=http://localhost:8081
services.order.url=http://localhost:8084
# ... остальные services.*.url
```

Для Docker переопределите hostnames, например `services.order.url=http://order-service:8084`.

Микросервисы подключают конфиг через `spring.config.import`:
`classpath:application-services.properties` → `common_lib/.env` → корневой `.env`.

### 2. База данных

```bash
docker compose -f docker-compose-pg.yml up -d
```

`docker-compose-pg.yml` поднимает Postgres 14; порт на хосте задаётся через `POSTGRES_LOCAL_PORT`.

### 3. Приложение

```bash
./mvnw spring-boot:run
```

| Что            | URL                                       |
|----------------|-------------------------------------------|
| Приложение     | http://localhost:8080                     |
| Swagger UI     | http://localhost:8080/swagger-ui.html     |
| OpenAPI JSON   | http://localhost:8080/v3/api-docs         |

### 4. Тесты

```bash
./mvnw test
```

## API

Полный список эндпоинтов — в [Swagger UI](http://localhost:8080/swagger-ui.html).

Типичные операции: CRUD по доменам, смена статуса заказа, activate/deactivate продукта, success/failure платежа. Списки поддерживают пагинацию через Spring `Pageable`.

## Структура

```
src/main/java/com/example/my_store/
├── account/          # покупатели
├── product/          # каталог
├── cart/             # корзина
├── order/            # заказы
├── payment/          # платежи
├── courier/          # курьеры
├── delivery/         # доставка
└── utils/            # ошибки, OpenAPI, аудит
```

В каждом домене: `controller`, `service`, `repository` (+ DTO и мапперы).


## Микросервисы

Проект содержит 7 микросервисов + монолит. Порты по умолчанию:

| Сервис | Порт | API |
|--------|------|-----|
| account-service | 8081 | `/rest/accounts` |
| products-service | 8082 | `/rest/products` |
| cart-service | 8083 | `/rest/carts` |
| order-service | 8084 | `/rest/orders` |
| payment-service | 8085 | `/rest/payments` |
| courier-service | 8086 | `/rest/couriers` |
| delivery-service | 8087 | `/rest/deliveries` |
| monolith | 8080 | все домены |

Запуск отдельного сервиса (из **корня** проекта):

```bash
./mvnw -pl payment_service spring-boot:run
```

## Kafka

```bash
docker compose -f docker-compose-kafka.yml up -d
```

С UI:

```bash
docker compose -f docker-compose-kafka.yml up -d kafka kafka-ui
```

Kafka UI: http://localhost:9091

Переменная окружения (опционально): `KAFKA_BOOTSTRAP_SERVERS=localhost:9092`

Flow: `payment-service` публикует `payment.succeeded` → `order-service` переводит заказ в `PAID`.
