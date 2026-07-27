# cart-service

## Назначение

Сервис **корзины покупателя**: одна корзина на аккаунт, позиции с количеством. При создании корзины и добавлении позиций проверяется существование **аккаунта** и **товара** через HTTP.

- Порт: **8083**
- API: `/rest/carts`
- БД: `cart_db` (PostgreSQL, порт **5435**)

## Роль в процессе

Между каталогом и заказом. Покупатель наполняет корзину до оформления заказа в order-service.

```
account → products → cart → order → payment → courier → delivery
                       ↑
                    (здесь)
```

## Зависимости

| Тип | Ресурс |
|-----|--------|
| БД | PostgreSQL (`cart_db`) |
| HTTP-клиенты | account-service (**8081**), products-service (**8082**) |
| Kafka | — |

## Структура

```
src/main/java/com/example/
├── Main.java
├── config/              # WebClient для вызовов соседних сервисов
├── controller/          # REST-эндпоинты, DTO корзины и позиций
├── service/             # CRUD корзины и позиций, валидация account/product
├── repository/
│   ├── cart/
│   │   └── entity/      # CartEntity
│   └── cart_item/
│       └── entity/      # CartItemEntity
└── utils/               # MapStruct-мапперы
```

## Запуск

Инструкция по окружению и запуску всего проекта: [README.md](../README.md), сценарий ручного тестирования: [scripts/local-dev.md](../scripts/local-dev.md).
