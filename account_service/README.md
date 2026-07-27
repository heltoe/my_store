# account-service

## Назначение

Сервис управляет **аккаунтами покупателей** интернет-магазина: создание, чтение, обновление и удаление профилей. При создании и обновлении проверяется **уникальность номера телефона**.

- Порт: **8081**
- API: `/rest/accounts`
- БД: `account_db` (PostgreSQL, порт **5433**)

## Роль в процессе

Первый шаг в цепочке магазина. Аккаунт создаётся до корзины и заказа; другие сервисы (cart, order) проверяют существование покупателя через HTTP.

```
account → products → cart → order → payment → courier → delivery
   ↑
 (здесь)
```

## Зависимости

| Тип | Ресурс |
|-----|--------|
| БД | PostgreSQL (`account_db`) |
| HTTP-клиенты | — |
| Kafka | — |

## Структура

```
src/main/java/com/example/
├── Main.java
├── controller/          # REST-эндпоинты, DTO запросов
├── service/             # бизнес-логика CRUD, проверка телефона
├── repository/          # JPA-репозиторий
│   └── entity/          # AccountEntity (таблица accounts)
└── utils/               # MapStruct-маппер, JPA-фильтр списка
```

## Запуск

Инструкция по окружению и запуску всего проекта: [README.md](../README.md), сценарий ручного тестирования: [scripts/local-dev.md](../scripts/local-dev.md).
