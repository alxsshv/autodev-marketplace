-- Скрипт инициализации PostgreSQL для создания схем микросервисов
-- Выполняется автоматически при первом запуске контейнера

-- Создание схем для всех микросервисов

CREATE SCHEMA IF NOT EXISTS catalog;
CREATE SCHEMA IF NOT EXISTS orders;
CREATE SCHEMA IF NOT EXISTS payment;
CREATE SCHEMA IF NOT EXISTS listings;
CREATE SCHEMA IF NOT EXISTS platform;
CREATE SCHEMA IF NOT EXISTS communication;
CREATE SCHEMA IF NOT EXISTS notification;

-- Предоставление прав пользователю services_db_user на все схемы
-- Обратите внимание: замените services_db_user на фактическое имя пользователя из переменных окружения
--
-- GRANT ALL PRIVILEGES ON SCHEMA catalog TO ${SERVICES_DB_USER};

--
-- -- Предоставление прав на все таблицы в схемах (для новых и существующих)
-- GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA catalog TO ${SERVICES_DB_USER};

--
-- -- Предоставление прав на все последовательности в схемах
-- GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA catalog TO ${SERVICES_DB_USER};

--
-- -- Для новых таблиц, создаваемых в будущем
-- ALTER DEFAULT PRIVILEGES IN SCHEMA catalog GRANT ALL ON TABLES TO ${SERVICES_DB_USER};
-- ALTER DEFAULT PRIVILEGES IN SCHEMA catalog GRANT ALL ON SEQUENCES TO ${SERVICES_DB_USER};

--

