# delivery-service

## Назначение

Сервис **доставок заказов**: создание и обновление, привязка к заказу и курьеру. При create/patch проверяются существование **заказа** и **курьера** через HTTP. Эндпоинт `on-the-way` используется courier-service перед деактивацией курьера.

- Порт: **8087**
- API: `/rest/deliveries`
- БД: `delivery_db` (PostgreSQL, порт **5439**)

## Роль в процессе

Финальный шаг цепочки магазина. Связывает оплаченный заказ с курьером и отслеживает статус доставки.

```
account → products → cart → order → payment → courier → delivery
                                                             ↑
                                                          (здесь)
```

## Зависимости

| Тип | Ресурс |
|-----|--------|
| БД | PostgreSQL (`delivery_db`) |
| HTTP-клиенты | order-service (**8084**), courier-service (**8086**) |
| Kafka | — |

## Структура

```
src/main/java/com/example/
├── Main.java
├── config/              # WebClient для order и courier
├── controller/          # REST-эндпоинты, on-the-way, DTO доставки
├── service/             # CRUD, валидация order/courier
├── repository/
│   └── entity/          # DeliveryEntity
└── utils/               # MapStruct-маппер, JPA-фильтр списка
```

## Запуск

Инструкция по окружению и запуску всего проекта: [README.md](../README.md), сценарий ручного тестирования: [scripts/local-dev.md](../scripts/local-dev.md).
