# Техническое задание TS-008: Docker и инфраструктура

**Версия документа:** 1.0  
**Дата создания:** 2026-06-02  
**Автор:** Системный аналитик  
**Статус:** Готово к реализации  
**Приоритет:** Высокий  
**Оценка трудоемкости:** 2 дня

---

## 1. Цель проекта

Настроить Docker контейнеризацию для микросервиса Auth Service и интегрировать его в существующую инфраструктуру.

---

## 2. Функциональные требования

### FR-1. Docker контейнеризация

**FR-1.1.** Создать `Dockerfile` в `services/auth-service/`.

**FR-1.2.** Использовать официальный образ `openjdk:17-slim` как базовый.

**FR-1.3.** Скопировать собранный jar файл в контейнер.

**FR-1.4.** Создать не-root пользователя для запуска приложения.

**FR-1.5.** Открыть порт 8080 для входящих соединений.

### FR-2. Docker Compose

**FR-2.1.** Обновить `docker-compose.yaml` в корне проекта.

**FR-2.2.** Добавить сервис `auth-service` с:
- build: `./services/auth-service`
- порт на хосте: 8083
- переменные окружения для конфигурации

**FR-2.3.** Добавить зависимость от:
- `auth-database` (PostgreSQL)
- `redis` (кэш)
- `kafka` (брокер сообщений)
- `keycloak` (аутентификация)

**FR-2.4.** Добавить сервис `auth-database` (PostgreSQL) для хранения данных Auth Service.

**FR-2.5.** Добавить `auth-database-exporter` для сбора метрик.

**FR-2.6.** Подключить все сервисы к сети `app_network`.

**FR-2.7.** Настроить healthcheck для `auth-service`.

### FR-3. Переменные окружения

**FR-3.1.** Настроить переменные окружения:
- `SPRING_PROFILES_ACTIVE=docker`
- `SPRING_DATASOURCE_URL` — URL для подключения к auth-database
- `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` — учетные данные БД
- `SPRING_DATA_REDIS_HOST`, `SPRING_DATA_REDIS_PORT`, `SPRING_DATA_REDIS_PASSWORD` — настройки Redis
- `SPRING_KAFKA_BOOTSTRAP_SERVERS` — адрес Kafka
- `KEYCLOAK_ISSUER_URI` — URI Keycloak
- `CONSUL_HOST`, `CONSUL_PORT` — настройки Consul

---

## 3. Нефункциональные требования

**NFR-1.** Образ должен быть минимального размера (использовать slim версии образов).

**NFR-2.** Контейнер должен иметь healthcheck.

**NFR-3.** Конфигурация должна поддерживать масштабирование (множественные инстансы).

**NFR-4.** Все сервисы должны быть в одной Docker сети `app_network`.

---

## 4. Технические требования

### 4.1. Dockerfile

**Структура:**
- FROM: `openjdk:17-slim`
- WORKDIR: `/app`
- COPY: `build/libs/*.jar app.jar`
- USER: не-root пользователь
- EXPOSE: `8080`
- ENTRYPOINT: `["java", "-jar", "app.jar"]`

### 4.2. docker-compose.yaml

**Сервис auth-service:**
- image: build из `./services/auth-service`
- ports: `8083:8080`
- environment: переменные окружения
- depends_on: auth-database, redis, kafka, keycloak
- networks: `app_network`
- healthcheck: HTTP проверка `/actuator/health`

**Сервис auth-database:**
- image: `postgres:15`
- environment: POSTGRES_USER, POSTGRES_PASSWORD, POSTGRES_DB=auth
- ports: `5436:5432`
- volumes: `./local/auth_bd/data/:/var/lib/postgresql/data`
- networks: `app_network`
- healthcheck: `pg_isready`

### 4.3. Порты

| Сервис | Порт в контейнере | Порт на хосте |
|--------|-------------------|---------------|
| auth-service | 8080 | 8083 |
| auth-database | 5432 | 5436 |

---

## 5. Критерии приемки

**HC-1.** Dockerfile создан и оптимизирован.

**HC-2.** auth-service добавлен в docker-compose.yaml.

**HC-3.** auth-database настроен и имеет healthcheck.

**HC-4.** Все сервисы в одной сети.

**HC-5.** Порты правильно проброшены (8083:8080).

**HC-6.** Переменные окружения настроены корректно.

**HC-7.** Healthcheck настроен для auth-service.

---

## 6. Примечания для разработчика

- Разработчик может изменить структуру Dockerfile, если это улучшает безопасность или производительность.
- Можно использовать многоэтапную сборку для уменьшения размера образа.
- Проверить работу через `docker-compose up` и запросы к `/actuator/health`.

---

## 7. Ответственный

**Системный аналитик** — составил техническое задание  
**Дата составления:** 2026-06-02  
**Версия документа:** 1.0
