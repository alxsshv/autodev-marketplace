-- liquibase formatted sql
-- changeset Aleksey Shvariov:07-06-2026-insert-initial-roles

INSERT INTO auth.roles (name, description) VALUES
('BUYER', 'Покупатель автозапчастей'),
('SELLER', 'Продавец автозапчастей'),
('MODERATOR','Модератор платформы'),
('ADMIN','Администратор системы');

-- roolback DELETE FROM auth.roles WHERE name IN ('BUYER', 'SELLER', 'MODERATOR', 'ADMIN');