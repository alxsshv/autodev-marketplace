# Техническое задание TS-001: Создание проекта Auth Service

**Версия документа:** 1.0  
**Дата создания:** 2026-06-02  
**Автор:** Системный аналитик  
**Статус:** Готово к реализации  
**Приоритет:** Высокий  
**Оценка трудоемкости:** 2 дня

---

## 1. Цель проекта

Создать микросервис Auth Service для централизованной аутентификации и авторизации пользователей через интеграцию с Keycloak.

---

## 2. Функциональные требования

### FR-1. Структура проекта

**FR-1.1.** Создать директорию `services/auth-service` со стандартной структурой Spring Boot проекта.

**FR-1.2.** Внутри `services/auth-service` создать следующие директории:
- `src/main/java/com/autodev/auth/` — основной исходный код
- `src/main/resources/` — ресурсы (конфигурация, миграции)
- `src/test/java/com/autodev/auth/` — тесты
- `src/main/resources/db/changelog/` — файлы миграций Liquibase

### FR-2. Конфигурация сборки

**FR-2.1.** В `build.gradle.kts` настроить зависимости для:
- Spring Boot 3.4.5 с поддержкой Java 17
- Spring Cloud Consul для Service Discovery
- Spring Security с OAuth2 Resource Server
- Spring Data JPA для доступа к PostgreSQL
- Spring Data Redis для кэширования
- Spring Kafka для асинхронной коммуникации
- Liquibase для управления миграциями БД
- MapStruct для маппинга объектов
- Lombok для сокращения boilerplate кода
- Testcontainers для интеграционного тестирования

**FR-2.2.** Настроить плагины:
- `org.springframework.boot`
- `io.spring.dependency-management`
- `org.liquibase.gradle`

**FR-2.3.** Использовать версии библиотек из `gradle.properties` проекта:
- `springBootVersion=3.4.5`
- `springCloudVersion=2024.0.1`

### FR-3. Основной класс приложения

**FR-3.1.** Создать класс `AuthApplication` в пакете `com.autodev.auth`.

**FR-3.2.** Добавить аннотации:
- `@SpringBootApplication` — основная аннотация Spring Boot
- `@EnableDiscoveryClient` — включить регистрацию в Consul
- `@EnableCaching` — включить кэширование
- `@EnableKafka` — включить Kafka

### FR-4. Конфигурация приложения

**FR-4.1.** В `application.yml` настроить:
- Порт сервера: 8080
- Имя сервиса: `auth-service`
- Активный профиль по умолчанию: `local`

**FR-4.2.** Настроить подключение к базе данных PostgreSQL:
- Драйвер: `org.postgresql.Driver`
- URL, username и password из переменных окружения

**FR-4.3.** Настроить подключение к Redis:
- Host и port из переменных окружения
- Password из переменной окружения

**FR-4.4.** Настроить Kafka:
- Bootstrap servers из переменной окружения
- Конфигурация producer и consumer для JSON сериализации

**FR-4.5.** Настроить интеграцию с Keycloak:
- `issuer-uri` для валидации JWT токенов

**FR-4.6.** Настроить Consul:
- Host и port для Service Discovery
- Настройки health check и регистрации

**FR-4.7.** Настроить Actuator:
- Экспонировать эндпоинты: `health`, `info`, `metrics`, `env`, `configprops`

### FR-5. Профили окружения

**FR-5.1.** Создать профиль `docker` в `application-docker.yml` для работы в контейнере.

**FR-5.2.** В профиле `docker` указать:
- Правильные имена сервисов (auth-database, redis, kafka, consul)
- Использовать Docker сетевые имена вместо localhost

---

## 3. Нефункциональные требования

**NFR-1.** Время запуска приложения не должно превышать 60 секунд.

**NFR-2.** Все зависимости в `build.gradle.kts` должны быть совместимы между собой.

**NFR-3.** Проект должен компилироваться командой `./gradlew build` без ошибок.

**NFR-4.** Приложение должно запускаться командой `./gradlew bootRun` без ошибок.

**NFR-5.** Код должен соответствовать конвенциям именования Java проекта (UpperCamelCase для классов, lowerCamelCase для методов).

**NFR-6.** Все публичные классы и методы должны быть задокументированы через JavaDoc.

---

## 4. Технические требования

### 4.1. Технологический стек

**Backend:**
- Java 17 (LTS)
- Spring Boot 3.4.5
- Spring Cloud 2024.0.1

**Базы данных:**
- PostgreSQL 15 — основная БД для хранения пользователей
- Redis 7 — кэширование данных
- Liquibase 4.27.0 — миграции БД

**Инфраструктура:**
- Consul 1.15.3 — Service Discovery
- Kafka 4.2.0 — асинхронная коммуникация

### 4.2. Структура проекта

```
services/auth-service/
├── src/
│   ├── main/
│   │   ├── java/com/autodev/auth/
│   │   │   ├── AuthApplication.java
│   │   │   ├── config/           # Конфигурационные классы
│   │   │   ├── controller/       # REST контроллеры
│   │   │   ├── dto/              # DTO объекты
│   │   │   ├── entity/           # JPA сущности
│   │   │   ├── repository/       # Репозитории
│   │   │   ├── service/          # Сервисы
│   │   │   └── event/            # Классы событий для Kafka
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-docker.yml
│   │       └── db/changelog/     # Liquibase миграции
│   └── test/
│       └── java/com/autodev/auth/  # Тесты
├── build.gradle.kts
├── Dockerfile
└── README.md
```

### 4.3. Структура базы данных

Таблица `users` (создается через Liquibase):
- `id` — BIGINT, PRIMARY KEY, AUTO_INCREMENT
- `keycloak_user_id` — VARCHAR(255), UNIQUE, NOT NULL
- `email` — VARCHAR(255), UNIQUE, NOT NULL
- `first_name` — VARCHAR(255), NULL
- `last_name` — VARCHAR(255), NULL
- `phone` — VARCHAR(50), NULL
- `role` — VARCHAR(50), NOT NULL
- `enabled` — BOOLEAN, NOT NULL, DEFAULT TRUE
- `created_at` — TIMESTAMP, NOT NULL, DEFAULT CURRENT_TIMESTAMP
- `updated_at` — TIMESTAMP, NOT NULL, DEFAULT CURRENT_TIMESTAMP

---

## 5. API эндпоинты (предварительная спецификация)

| Метод | Путь | Описание | Роли |
|-------|------|----------|------|
| GET | `/api/v1/users/{id}` | Получить пользователя по ID | ADMIN, BUYER, SELLER, MODERATOR |
| GET | `/api/v1/users/email/{email}` | Получить пользователя по email | ADMIN, BUYER, SELLER, MODERATOR |
| GET | `/api/v1/users/` | Получить всех пользователей | ADMIN |
| POST | `/api/v1/users/` | Создать нового пользователя | ADMIN |
| PUT | `/api/v1/users/{id}` | Обновить данные пользователя | ADMIN |

---

## 6. Критерии приемки

**HC-1.** Проект создан в директории `services/auth-service` со стандартной структурой Spring Boot.

**HC-2.** В `build.gradle.kts` перечислены все необходимые зависимости с корректными версиями из `gradle.properties`.

**HC-3.** Основной класс `AuthApplication` содержит все обязательные аннотации: `@SpringBootApplication`, `@EnableDiscoveryClient`, `@EnableCaching`, `@EnableKafka`.

**HC-4.** Конфигурация `application.yml` настроена для локальной разработки с поддержкой всех необходимых сервисов.

**HC-5.** Конфигурация `application-docker.yml` настроена для работы в Docker контейнере.

**HC-6.** Проект компилируется без ошибок: `./gradlew build`.

**HC-7.** Приложение запускается без ошибок: `./gradlew bootRun` (порт 8080).

**HC-8.** Директория `src/main/resources/db/changelog/` создана для файлов миграций Liquibase.

---

## 7. Примечания для разработчика

- Структура пакетов и классов может быть изменена разработчиком по усмотрению, если это улучшает читаемость и поддерживаемость кода.
- Использование Lombok, MapStruct и других библиотек для сокращения boilerplate кода приветствуется.
- Разработчик должен самостоятельно выбрать паттерны проектирования и структуру кода, соответствующие best practices Spring Boot.

---

## 8. Ответственный

**Системный аналитик** — составил техническое задание  
**Дата составления:** 2026-06-02  
**Версия документа:** 1.0
