# Техническое задание TS-006: Интеграция с Keycloak

**Версия документа:** 1.0  
**Дата создания:** 2026-06-02  
**Автор:** Системный аналитик  
**Статус:** Готово к реализации  
**Приоритет:** Высокий  
**Оценка трудоемкости:** 3 дня

---

## 1. Цель проекта

Настроить интеграцию с Keycloak для централизованной аутентификации и авторизации через OAuth2/OpenID Connect протоколы.

---

## 2. Функциональные требования

### FR-1. Интеграция с Keycloak

**FR-1.1.** Настроить Spring Security как OAuth2 Resource Server.

**FR-1.2.** Настроить валидацию JWT токенов через Keycloak.

**FR-1.3.** Настроить проверку ролей пользователей из токена.

### FR-2. Конфигурация Security

**FR-2.1.** Создать класс `SecurityConfig` в пакете `com.autodev.auth.config`.

**FR-2.2.** Настроить `SecurityFilterChain` с `@EnableWebSecurity`.

**FR-2.3.** Настроить авторизацию по ролям:
- `/api/v1/users/` — доступ только для ADMIN
- `/api/v1/users/email/**` — доступ для ADMIN, BUYER, SELLER, MODERATOR
- `/api/v1/users/{id}` — доступ для ADMIN, BUYER, SELLER, MODERATOR
- `/actuator/**` — доступ без аутентификации
- `/swagger-ui/**` и `/v3/api-docs/**` — доступ без аутентификации

**FR-2.4.** Настроить JWT декодер с `issuer-uri` для Keycloak.

### FR-3. Конфигурация Keycloak

**FR-3.1.** В `application.yml` указать `issuer-uri` для Keycloak.

**FR-3.2.** Настроить `jwk-set-uri` для получения публичных ключей Keycloak.

---

## 3. Нефункциональные требования

**NFR-1.** Валидация JWT токенов должна выполняться менее чем за 50 мс.

**NFR-2.** Все токены должны валидироваться через HTTPS (для production).

**NFR-3.** Кэширование JWKS для уменьшения количества запросов к Keycloak.

**NFR-4.** Система должна устойчиво работать при недоступности Keycloak (с кэшированием).

---

## 4. Технические требования

### 4.1. Зависимости

В `build.gradle.kts` должны быть добавлены:
- `org.springframework.boot:spring-boot-starter-security`
- `org.springframework.security:spring-security-oauth2-resource-server`
- `org.springframework.security:spring-security-oauth2-jose`

### 4.2. Структура JWT токена

Keycloak выдает JWT токены со следующими полями:
- `iss` — issuer URI
- `aud` — audience
- `sub` — subject (ID пользователя)
- `realm_access.roles` — роли пользователя
- `email` — email пользователя
- `exp` — время истечения

### 4.3. Настройка Keycloak

**Realm:** autodev

**Client:** auth-service
- Client ID: `auth-service`
- Client Protocol: `openid-connect`
- Authorization Enabled: `Off`
- Standard Flow Enabled: `On`
- Direct Access Grants Enabled: `On`

**Роли:**
- BUYER
- SELLER
- MODERATOR
- ADMIN

---

## 5. Критерии приемки

**HC-1.** `SecurityConfig` создан с настройками OAuth2 Resource Server.

**HC-2.** JWT токены валидируются через Keycloak.

**HC-3.** Проверка ролей настроена корректно.

**HC-4.** Эндпоинты защищены в соответствии с конфигурацией.

**HC-5.** Проект компилируется без ошибок: `./gradlew build`.

---

## 6. Примечания для разработчика

- Разработчик должен сам решить, как организовать настройку Security.
- Можно использовать любые паттерны для защиты API.
- Проверить конфигурацию через запросы с валидным токеном.

---

## 7. Ответственный

**Системный аналитик** — составил техническое задание  
**Дата составления:** 2026-06-02  
**Версия документа:** 1.0
