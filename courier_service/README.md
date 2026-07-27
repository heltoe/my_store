# courier-service

## Назначение

Сервис **курьеров**: CRUD профилей, активация и деактивация. Перед деактивацией проверяется через delivery-service, что у курьера **нет доставки в пути**.

- Порт: **8086**
- API: `/rest/couriers`
- БД: `courier_db` (PostgreSQL, порт **5438**)

## Роль в процессе

Предпоследний шаг цепочки. Курьеры назначаются на доставки; delivery-service валидирует курьера при создании доставки.

```
account → products → cart → order → payment → courier → delivery
                                                  ↑
                                               (здесь)
```

## Зависимости

| Тип | Ресурс |
|-----|--------|
| БД | PostgreSQL (`courier_db`) |
| HTTP-клиенты | delivery-service (**8087**) — `GET /rest/deliveries/courier/{id}/on-the-way` |
| Kafka | — |

## Структура

```
src/main/java/com/example/
├── Main.java
├── config/              # WebClient для вызова delivery-service
├── controller/          # REST-эндпоинты, DTO курьера
├── service/             # CRUD, set-active / set-inactive с проверкой on-the-way
├── repository/
│   └── entity/          # CourierEntity
└── utils/               # MapStruct-маппер
```

## Запуск

Инструкция по окружению и запуску всего проекта: [README.md](../README.md), сценарий ручного тестирования: [scripts/local-dev.md](../scripts/local-dev.md).
