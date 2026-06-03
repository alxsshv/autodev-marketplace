# Техническое задание TS-002: Настройка базы данных

**Версия документа:** 1.0  
**Дата создания:** 2026-06-02  
**Автор:** Системный аналитик  
**Статус:** Готово к реализации  
**Приоритет:** Высокий  
**Оценка трудоемкости:** 3 дня

---

## 1. Цель проекта

Настроить базу данных PostgreSQL для хранения информации о пользователях в микросервисе Auth Service с использованием Liquibase для управления миграциями и Spring Data JPA для доступа к данным.

---

## 2. Функциональные требования

### FR-1. Миграции Liquibase

**FR-1.1.** Создать файл `db.changelog-master.yaml` в `src/main/resources/db/changelog/` как главный файл миграций.

**FR-1.2.** Создать файл `001-create-users-table.yaml` для создания таблицы `users`.

**FR-1.3.** Таблица `users` должна содержать следующие поля:
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

**FR-1.4.** Настроить автоматическое применение миграций при запуске приложения.

### FR-2. JPA сущность User

**FR-2.1.** Создать класс `User` в пакете `com.autodev.auth.entity`.

**FR-2.2.** Сущность должна быть аннотирована `@Entity` и `@Table(name = "users")`.

**FR-2.3.** Каждое поле должно иметь соответствующую аннотацию `@Column` с типом данных и ограничениями.

**FR-2.4.** Использовать `@Enumerated(EnumType.STRING)` для поля `role`.

**FR-2.5.** Добавить аннотации `@CreatedDate` и `@LastModifiedDate` для автоматического управления временем.

### FR-3. Репозиторий UserRepository

**FR-3.1.** Создать интерфейс `UserRepository` в пакете `com.autodev.auth.repository`.

**FR-3.2.** Наследовать от `JpaRepository<User, Long>`.

**FR-3.3.** Добавить методы:
- `findByKeycloakUserId(String keycloakUserId)` — найти пользователя по ID в Keycloak
- `findByEmail(String email)` — найти пользователя по email

---

## 3. Нефункциональные требования

**NFR-1.** Таблица `users` должна поддерживать рост до 1 000 000 пользователей.

**NFR-2.** Индексы должны быть настроены для полей `keycloak_user_id`, `email`, `role` и `enabled`.

**NFR-3.** Все ограничения базы данных (PK, UNIQUE, NOT NULL) должны быть соблюдены.

**NFR-4.** Операции с базой данных должны выполняться менее чем за 100 мс.

**NFR-5.** Структура таблицы должна быть совместима с PostgreSQL 15.

---

## 4. Технические требования

### 4.1. Зависимости

В `build.gradle.kts` должны быть добавлены:
- `spring-boot-starter-data-jpa`
- `org.postgresql:postgresql` (runtime)
- `org.liquibase:liquibase-core`
- `org.springframework.boot:spring-boot-starter-data-redis` (для кэширования)

### 4.2. Структура базы данных

**Таблица: users**

| Поле | Тип | Ограничения | Описание |
|------|-----|-------------|----------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Уникальный идентификатор |
| keycloak_user_id | VARCHAR(255) | UNIQUE, NOT NULL | ID пользователя в Keycloak |
| email | VARCHAR(255) | UNIQUE, NOT NULL | Email пользователя |
| first_name | VARCHAR(255) | NULL | Имя пользователя |
| last_name | VARCHAR(255) | NULL | Фамилия пользователя |
| phone | VARCHAR(50) | NULL | Телефон пользователя |
| role | VARCHAR(50) | NOT NULL | Роль пользователя |
| enabled | BOOLEAN | NOT NULL, DEFAULT TRUE | Флаг активности |
| created_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Дата создания |
| updated_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Дата обновления |

**Индексы:**
- PRIMARY KEY: id
- UNIQUE INDEX: keycloak_user_id
- UNIQUE INDEX: email
- INDEX: role
- INDEX: enabled

---

## 5. Критерии приемки

**HC-1.** Миграция `001-create-users-table.yaml` создана и содержит все необходимые поля.

**HC-2.** При запуске приложения миграции применяются автоматически без ошибок.

**HC-3.** Таблица `users` создается в базе данных PostgreSQL.

**HC-4.** Ограничения (PK, UNIQUE) созданы корректно.

**HC-5.** Индексы созданы корректно.

**HC-6.** JPA сущность `User` корректно отображается на таблицу `users`.

**HC-7.** `UserRepository` содержит все необходимые методы поиска.

**HC-8.** Проект компилируется без ошибок: `./gradlew build`.

---

## 6. Примечания для разработчика

- Разработчик должен сам решить, как организовать структуру пакетов внутри `entity/`, `repository/`, `config/`.
- Можно использовать любые валидные аннотации Lombok для сокращения boilerplate кода.
- Проверить корректность миграций через `./gradlew liquibaseStatus`.

---

## 7. Ответственный

**Системный аналитик** — составил техническое задание  
**Дата составления:** 2026-06-02  
**Версия документа:** 1.0
