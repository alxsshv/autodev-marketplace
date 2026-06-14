# AutoDev Marketplace — Памятка по миграциям Liquibase

**Для разработчиков**  
**Дата:** 2026-06-14  
**Версия документа:** 1.0

---

## Быстрый старт

### 1. Создание новой миграции

```bash
# Перейти в директорию сервиса
cd services/auth-service/src/main/resources/db/changelog/v1.0.0/

# Создать файл миграции (использовать формат 0X-description.sql)
touch 06-create-new-table.sql

# Редактировать файл
vim 06-create-new-table.sql
```

### 2. Структура SQL файла

```sql
--liquibase formatted sql
--changeset Aleksey Shvariov:14-06-2026-create-new-table

-- Your SQL DDL statements here

--rollback DROP TABLE auth.new_table;
```

**Обязательные секции:**
- `--liquibase formatted sql` — указание на Liquibase формат
- `--changeset` — уникальный идентификатор (автор:дата-описание)
- `--rollback` — SQL для отката миграции

### 3. Примеры

#### Создание таблицы
```sql
--liquibase formatted sql
--changeset Aleksey Shvariov:14-06-2026-create-users

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

#### Добавление индекса
```sql
--liquibase formatted sql
--changeset Aleksey Shvariov:14-06-2026-add-index

CREATE INDEX idx_users_email ON auth.users(email);

--rollback DROP INDEX idx_users_email;
```

#### Insert данных
```sql
--liquibase formatted sql
--changeset Aleksey Shvariov:14-06-2026-insert-admin

INSERT INTO auth.users (keycloak_user_id, email, enabled, created_at)
VALUES ('admin-uuid', 'admin@autodev.com', true, CURRENT_TIMESTAMP);

--rollback DELETE FROM auth.users WHERE email = 'admin@autodev.com';
```

---

## Часто используемые команды

### Локальное применение миграций

```bash
# Запустить Spring Boot сервис (миграции применятся автоматически)
cd services/auth-service/
gradle bootRun

# Или применить миграции вручную через Docker
docker exec -it autodev-postgres psql -U autodev -d autodev -c "\dt auth.*"
```

### Проверка применённых миграций

```bash
# Проверить список таблиц
docker exec -it autodev-postgres psql -U autodev -d autodev -c "\dt auth.*"

# Проверить статус миграций (если настроено)
docker exec -it autodev-postgres psql -U autodev -d autodev -c "SELECT * FROM databasechangelog;"
```

### Откат миграции (осторожно!)

```bash
# Откатить последнюю миграцию (только для разработки!)
docker exec -it autodev-postgres psql -U autodev -d autodev -c "ROLLBACK;"
```

---

## Соглашения

### Именование файлов

```
01-create-users.sql
02-create-oauth-providers.sql
03-create-roles-tables.sql
04-create-audit-logs.sql
05-insert-initial-admin-user.sql
```

**Правила:**
- Использовать ведущие нули: `01`, `02`, `03` (не `1`, `2`, `3`)
- Формат даты: `dd-mm-yyyy` (например, `03-06-2026`)
- Snake_case для описания: `create-table-users`, `add-index`

### Порядок применения

1. Liquibase читает `master.yaml`
2. `includeAll` находит все файлы в `v1.0.0/`
3. Файлы сортируются по имени (алфавитно)
4. Миграции применяются в алфавитном порядке

**Важно:** Не вставляйте новые миграции между существующими! Всегда добавляйте в конец.

---

## CI/CD

### GitLab CI/CD

```yaml
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
```

---

## Типичные ошибки

### ❌ Ошибка: Дублирование миграций

```bash
# НЕТ! Создание миграции с тем же именем
touch 01-create-users.sql  # уже существует!
```

**Решение:** Используйте следующий номер: `06-create-users.sql`

### ❌ Ошибка: Пропущенная секция rollback

```sql
--liquibase formatted sql
--changeset Aleksey Shvariov:14-06-2026-create-table

CREATE TABLE auth.users (...)

--rollback DROP TABLE auth.users;  -- ВСЕГДА добавлять!
```

**Решение:** Всегда добавлять `--rollback` секцию

### ❌ Ошибка: Неправильный порядок

```bash
# НЕТ! Вставка между существующими
touch 02-new-migration.sql  # должно быть 06!
```

**Решение:** Всегда проверяйте номер следующей миграции и добавляйте в конец

---

## Рекомендации

### ✅ Лучшие практики

1. **Коммитить SQL файлы в Git** — миграции — часть кода
2. **Не изменять примененные миграции** — создавать новые для исправлений
3. **Документировать изменения** — обновлять `data-model.md`
4. **Тестировать перед коммитом** — локально применять миграции
5. **Использовать семантическое версионирование** — `v1.0.0`, `v1.1.0`

### ✅ Примеры

```sql
-- Создание таблицы с внешним ключом
CREATE TABLE auth.oauth_providers (
    id                  BIGSERIAL      PRIMARY KEY,
    user_id             BIGINT         NOT NULL,
    provider            VARCHAR(50)    NOT NULL,
    provider_user_id    VARCHAR(255)   NOT NULL,
    access_token        VARCHAR(1000)  NOT NULL,
    refresh_token       VARCHAR(1000)  NULL,
    created_at          TIMESTAMP      NOT NULL   DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES auth.users(id) ON DELETE CASCADE
);

CREATE INDEX idx_oauth_providers_user ON auth.oauth_providers(user_id);
CREATE INDEX idx_oauth_providers_provider ON auth.oauth_providers(provider);

--rollback DROP TABLE auth.oauth_providers;
```

---

## Ссылки

- [Документация Liquibase](https://www.liquibase.org/)
- [docs/architecture/database-migrations.md](../database-migrations.md) — полное руководство
- [docs/architecture/data-model.md](../data-model.md) — модель данных
- [docs/architecture/roadmap.md](../roadmap.md) — план реализации

---

**Примечание:** Все миграции должны соответствовать архитектурным решениям, описанным в документации. Вопросы архитектурных решений направляйте архитектору проекта.
