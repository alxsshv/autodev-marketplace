# AutoDev Marketplace — Liquibase миграции

**Версия документа:** 1.0  
**Дата создания:** 2026-06-14  
**Последнее обновление:** 2026-06-14

---

## Обзор

Документ описывает подход к управлению миграциями базы данных в AutoDev Marketplace с использованием Liquibase.

---

## Подход к версионированию

### Структура миграций

Для MVP используется структура, где **каждый сервис имеет свою директорию миграций** с версией `v1.0.0`. Это соответствует паттерну **"Database per Service"** в микросервисной архитектуре.

```
services/
├── auth-service/
│   └── src/main/resources/db/changelog/
│       ├── master.yaml             # Главный файл миграции для auth-service (YAML формат)
│       └── v1.0.0/                 # Версия миграций для MVP
│           ├── 01-create-users.sql
│           ├── 02-create-oauth-providers.sql
│           ├── 03-create-roles-tables.sql
│           └── ...
└── platform-service/
    └── src/main/resources/db/changelog/
        ├── master.yaml             # Главный файл миграции для platform-service (YAML формат)
        └── v1.0.0/                 # Версия миграций для MVP
            ├── 01-create-user-profiles.sql
            └── ...
```

### Почему такая структура?

1. **Соответствие паттерну "Database per Service"** — каждый сервис управляет своей схемой БД
2. **Независимость сервисов** — изменения в одной БД не влияют на другие сервисы
3. **Простота разработки** — разработчики могут работать над миграциями своего сервиса
4. **Масштабируемость** — при добавлении новых сервисов создается новая директория миграций

### Версионирование

- **MVP:** Все миграции объединены в версию `v1.0.0` для каждого сервиса
- **Будущее:** При добавлении новых функций создаются версии `v1.1.0`, `v1.2.0` и т.д.
- **Breaking changes:** Для мажорных изменений схемы создаются версии `v2.0.0`, `v3.0.0`

---

## Структура master changelog файла

Каждый сервис должен иметь `master.yaml` (или `master.xml`), который объединяет все миграции.

### Пример для auth-service (YAML формат)

```yaml
# services/auth-service/src/main/resources/db/changelog/master.yaml
databaseChangeLog:
  - includeAll:
      path: v1.0.0/
      relativeToChangelogFile: true
```

### Пример для platform-service (YAML формат)

```yaml
# services/platform-service/src/main/resources/db/changelog/master.yaml
databaseChangeLog:
  - includeAll:
      path: v1.0.0/
      relativeToChangelogFile: true
```

**Преимущества YAML перед XML:**
- Более читаем и компактный
- Меньше шума в Git diff
- Легче редактировать вручную
- Поддерживается Liquibase с версии 3.6+

---

## Структура SQL файлов миграции

### Формат имени файла

```
<номер>-<дата>-<описание>.sql
```

- **`номер`** — порядковый номер применения (для алфавитной сортировки)
- **`дата`** — дата создания миграции в формате `dd-mm-yyyy`
- **`описание`** — краткое описание изменений (snake_case)

### Примеры

```
01-create-users.sql
02-create-oauth-providers.sql
03-create-roles-tables.sql
04-create-audit-logs.sql
05-insert-initial-admin-user.sql
```

### Пример SQL файла

```sql
--liquibase formatted sql
--changeset Aleksey Shvariov:03-06-2026-create-table-users

CREATE TABLE auth.users (
    id                  BIGSERIAL      PRIMARY KEY,
    keycloak_user_id    VARCHAR(255)   NOT NULL   UNIQUE,
    email               VARCHAR(255)   NOT NULL   UNIQUE,
    enabled             BOOLEAN        NOT NULL   DEFAULT TRUE,
    created_at          TIMESTAMP      NOT NULL   DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_auth_users_keycloak ON auth.users(keycloak_user_id);

--rollback DROP TABLE auth.users;
```

**Обязательные секции:**
- `--liquibase formatted sql` — указывает, что это Liquibase файл
- `--changeset` — уникальный идентификатор миграции (автор:дата-описание)
- `--rollback` — SQL для отката миграции (обязательно!)

---

## Конфигурация в Spring Boot

### application.yml для auth-service

```yaml
spring:
  liquibase:
    enabled: true                    # Включить Liquibase
    change-log: classpath:/db/changelog/master.xml  # Путь к master changelog
    context: v1.0.0                  # Контекст миграций (опционально)
```

### application.yml для platform-service

```yaml
spring:
  liquibase:
    enabled: true                    # Включить Liquibase
    change-log: classpath:/db/changelog/master.xml  # Путь к master changelog
    context: v1.0.0                  # Контекст миграций (опционально)
```

### Application класс (автоматическое применение)

```java
package com.autodev.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication
public class AuthApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }
}
```

**Примечание:** Spring Boot автоматически применит миграции при старте сервиса, если `spring.liquibase.enabled=true`.

---

## Порядок применения миграций

### Алгоритм

1. Liquibase читает `master.yaml` (или `master.xml`)
2. `includeAll` находит все файлы в директории `v1.0.0/`
3. Файлы сортируются по имени (алфавитно)
4. Миграции применяются в алфавитном порядке

### Пример порядка для auth-service

```
1. 01-create-users.sql
2. 02-create-oauth-providers.sql
3. 03-create-roles-tables.sql
4. 04-create-audit-logs.sql
5. 05-insert-initial-admin-user.sql
...
```

### Важные правила

- **Всегда добавляйте новые миграции в конец директории** — не вставляйте между существующими
- **Используйте номера с ведущими нулями** — `01`, `02`, `03` (не `1`, `2`, `3`)
- **Используйте формат `dd-mm-yyyy`** — `03-06-2026` (не `06-03-2026` для избежания путаницы)

---

## Управление миграциями

### Создание новой миграции для v1.0.0

1. Создать файл в `services/{service-name}/src/main/resources/db/changelog/v1.0.0/`
2. Использовать формат: `0X-description.sql` (X — следующий номер)
3. Добавить секции `--liquibase formatted sql`, `--changeset`, `--rollback`

### Пример создания миграции

```bash
# Для auth-service
cd services/auth-service/src/main/resources/db/changelog/v1.0.0/
touch 06-create-new-table.sql

# Редактировать файл
vim 06-create-new-table.sql
```

```sql
--liquibase formatted sql
--changeset Aleksey Shvariov:14-06-2026-create-new-table

CREATE TABLE auth.new_table (
    id                  BIGSERIAL      PRIMARY KEY,
    name                VARCHAR(255)   NOT NULL,
    created_at          TIMESTAMP      NOT NULL   DEFAULT CURRENT_TIMESTAMP
);

--rollback DROP TABLE auth.new_table;
```

### Обновление master changelog

Если добавляется новая версия миграций (например, `v1.1.0`), обновить `master.yaml`:

```yaml
databaseChangeLog:
  - includeAll:
      path: v1.0.0/
      relativeToChangelogFile: true
  - includeAll:
      path: v1.1.0/
      relativeToChangelogFile: true
```

---

## CI/CD интеграция

### GitLab CI/CD пример

```yaml
# .gitlab-ci.yml

auth-service-migrate:
  stage: migrate
  image: liquibase/liquibase:4.27.0
  variables:
    LIQUIBASE_URL: "jdbc:postgresql://postgres:5432/autodev"
    LIQUIBASE_USERNAME: "autodev"
    LIQUIBASE_PASSWORD: ${DB_PASSWORD}
    LIQUIBASE_CHANGELOG: "services/auth-service/src/main/resources/db/changelog/master.yaml"
  script:
    - liquibase update --context=v1.0.0
  only:
    - develop
    - main

platform-service-migrate:
  stage: migrate
  image: liquibase/liquibase:4.27.0
  variables:
    LIQUIBASE_URL: "jdbc:postgresql://postgres:5432/autodev"
    LIQUIBASE_USERNAME: "autodev"
    LIQUIBASE_PASSWORD: ${DB_PASSWORD}
    LIQUIBASE_CHANGELOG: "services/platform-service/src/main/resources/db/changelog/master.yaml"
  script:
    - liquibase update --context=v1.0.0
  only:
    - develop
    - main
```

---

## Тестирование миграций

### Локальное тестирование

1. Запустить инфраструктуру:
```bash
docker-compose up -d postgres
```

2. Применить миграции:
```bash
cd services/auth-service/
gradle bootRun
```

3. Проверить таблицы в PostgreSQL:
```bash
docker exec -it autodev-postgres psql -U autodev -d autodev -c "\dt auth.*"
```

### Тесты в Spring Boot

```java
// src/test/java/com/autodev/auth/migration/MigrationTest.java
package com.autodev.auth.migration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MigrationTest {

    @Test
    void testMigrationRunSuccessfully() {
        // Spring Boot автоматически применит миграции при старте контекста
        // Если миграции не прошли, контекст не запустится
    }
}
```

---

## Роли и ответственности

### Для разработчиков

1. **Создание миграций** — создавать SQL файлы в правильной директории
2. **Тестирование** — тестировать миграции локально перед коммитом
3. **Обновление документации** — обновлять `data-model.md` при изменениях схемы

### Для DevOps

1. **Настройка CI/CD** — настраивать скрипты для применения миграций
2. **Мониторинг** — следить за статусом миграций в production
3. **Backups** — делать бэкапы перед применением миграций

---

## Рекомендации

### Лучшие практики

1. **Коммитить SQL файлы в Git** — миграции — часть кода
2. **Не изменять примененные миграции** — создавать новые для исправлений
3. **Использовать семантическое версионирование** — `v1.0.0`, `v1.1.0`
4. **Документировать изменения** — обновлять `data-model.md`
5. **Тестировать перед коммитом** — локально применять миграции

### Избегать

1. **Не удалять SQL файлы** — только создавать новые миграции для исправлений
2. **Не изменять master changelog** — только добавлять новые директории
3. **Не использовать-relative пути** — всегда использовать `relativeToChangelogFile="true"`
4. **Не игнорировать rollback** — всегда добавлять секцию `--rollback`

---

## Заключение

Данная документация описывает подход к управлению миграциями базы данных в AutoDev Marketplace.

**Ключевые моменты:**
- Каждый сервис имеет свою директорию миграций
- Для MVP используется версия `v1.0.0` для каждого сервиса
- Используется master changelog файл для объединения миграций
- Порядок применения — алфавитный по именам файлов
- Семантическое версионирование для будущих изменений

**Документация:**
- `data-model.md` — логическая и физическая модель данных
- `roadmap.md` — план реализации
- `glossary.md` — глоссарий терминов и соглашений
