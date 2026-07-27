# products-service

## Назначение

Сервис **каталога товаров**: CRUD, активация и деактивация позиций. Список по умолчанию возвращает только **активные** товары (фильтр `isActive`, по умолчанию `true`).

- Порт: **8082**
- API: `/rest/products`
- БД: `product_db` (PostgreSQL, порт **5434**)

## Роль в процессе

Второй шаг после аккаунта. Товары добавляются в корзину и заказ; cart- и order-service проверяют существование и активность продукта через HTTP.

```
account → products → cart → order → payment → courier → delivery
              ↑
           (здесь)
```

## Зависимости

| Тип | Ресурс |
|-----|--------|
| БД | PostgreSQL (`product_db`) |
| HTTP-клиенты | — |
| Kafka | — |

## Структура

```
src/main/java/com/example/
├── Main.java
├── controller/          # REST-эндпоинты, DTO запросов
├── service/             # CRUD, set-active / set-inactive
├── repository/          # JPA-репозиторий
│   └── entity/          # ProductEntity (таблица products)
└── utils/               # MapStruct-маппер, фильтр по имени и isActive
```

## Запуск

Инструкция по окружению и запуску всего проекта: [README.md](../README.md), сценарий ручного тестирования: [scripts/local-dev.md](../scripts/local-dev.md).
