# AutoDev Marketplace — Стратегия безопасности

**Версия документа:** 1.0  
**Дата создания:** 2026-06-13  
**Последнее обновление:** 2026-06-13

---

## Обзор

Документ описывает стратегию обеспечения безопасности AutoDev Marketplace, включая аутентификацию, авторизацию, шифрование данных и аудит.

---

## 1. Межсервисная аутентификация

### 1.1 Для MVP (без TLS)

**Keycloak как единственный источник правды для ролей:**
- Каждый сервис имеет свой service account в Keycloak
- При запуске сервис получает JWT token от Keycloak
- Для межсервисных вызовов сервисы предъявляют свои токены
- Получающий сервис валидирует токен через Redis кэш (или Keycloak)
- Роли пользователей хранятся только в Keycloak и передаются в JWT токене

**Auth Service (обертка над Keycloak):**
- Auth Service не управляет ролями в PostgreSQL (таблицы auth.roles удалены)
- Auth Service служит для синхронизации пользователей между Keycloak и PostgreSQL
- Кэширование JWT токенов и публичных ключей Keycloak в Redis
- **Механизм revoked tokens:** Redis-only подход с TTL = expires_at - current_time (для MVP)
- Вспомогательные операции: logout, view profile, get user by ID

**Пример использования (Spring Security):**
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/v1/**").hasAnyRole("BUYER", "SELLER", "MODERATOR", "ADMIN")
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            );
        return http.build();
    }

    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter scopesConverter = new JwtGrantedAuthoritiesConverter();
        scopesConverter.setScopeAttributeName("scope");
        scopesConverter.setAuthoritiesPrefix("ROLE_");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(scopesConverter);
        return converter;
    }
}
```

### 1.2 Для продакшена (планируемое улучшение)

**MTLS (mutual TLS) для service-to-service коммуникации:**
- Каждый сервис имеет сертификат
- TLS handshake проверяет сертификаты обеих сторон
- Высокая степень безопасности для production окружения

### 1.3 Рекомендация для MVP

Использовать Service Account Tokens через Keycloak:
- Простота реализации
- Уже интегрирован Keycloak
- Возможность отозвать токен в любой момент
- Поддержка в Spring Security

---

## 2. Стратегия шифрования

### 2.1 Для MVP

**TLS 1.3 для внешних API (client ↔ service):**
```yaml
server:
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${SSL_KEYSTORE_PASSWORD}
    key-store-type: PKCS12
    key-alias: ${SSL_KEY_ALIAS}
    protocol: TLSv1.3
```

**Внутренняя сеть без TLS между сервисами (Docker network isolation):**
```yaml
# docker-compose.yaml
services:
  api-gateway:
    networks:
      - internal-network
  
  auth-service:
    networks:
      - internal-network

networks:
  internal-network:
    driver: bridge
```

**Шифрование данных в покое:**
- PostgreSQL: SSL для внешних подключений
- Регулярные бэкапы с шифрованием (MinIO server-side encryption)

### 2.2 Для продакшена (планируемое улучшение)

**MTLS между сервисами (внутренняя коммуникация):**
- Mutual TLS handshake между сервисами
- Сертификаты управляются через Keycloak或Hashicorp Vault

**Шифрование на уровне приложения для чувствительных данных:**
- Платёжные данные: AES-256
- Персональные данные: AES-256
- Ключи хранятся в Hashicorp Vault

### 2.3 Шифрование паролей

**BCrypt для пользовательских паролей:**
```java
@Service
public class PasswordEncoder {
    private final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder(12);
    
    public String encode(String rawPassword) {
        return bCryptPasswordEncoder.encode(rawPassword);
    }
    
    public boolean matches(String rawPassword, String encodedPassword) {
        return bCryptPasswordEncoder.matches(rawPassword, encodedPassword);
    }
}
```

**PBKDF2 для service account паролей:**
```java
@Service
public class ServiceAccountService {
    
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;
    
    public String hashPassword(String password, String salt) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), 
            Base64.getDecoder().decode(salt), ITERATIONS, KEY_LENGTH);
        SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
        return Base64.getEncoder().encodeToString(factory.generateSecret(spec).getEncoded());
    }
}
```

---

## 3. RBAC (Role-Based Access Control)

### 3.1 Уровни доступа

| Роль | Описание | Примеры сервисов |
|------|----------|-----------------|
| BUYER | Покупатель товаров | order-service, catalog-service |
| SELLER | Продавец товаров | catalog-service, order-service, platform-service |
| MODERATOR | Модератор контента | platform-service, communication-service |
| ADMIN | Администратор системы | auth-service, platform-service, admin-service |

**ВАЖНО:** Роли пользователей хранятся исключительно в Keycloak. JWT токен содержит список ролей для авторизации во всех сервисах. В PostgreSQL нет таблиц для хранения ролей (`auth.roles`, `auth.permissions`, `auth.role_permissions` удалены).

**Маппинг:** `realm_access.roles` → Spring Security `ROLE_*`

**Подробнее:** [security/rbac.md](security/rbac.md)

### 3.2 Применение RBAC по сервисам

#### Auth Service

| Роль | Методы API |
|------|-----------|
| BUYER | login, refresh, logout, view profile |
| SELLER | login, refresh, logout, view profile |
| MODERATOR | login, refresh, logout, view profile |
| ADMIN | login, refresh, logout, view profile, manage users |

**ВАЖНО:** Управление ролями осуществляется ТОЛЬКО через Keycloak Admin Console или Keycloak Admin API. Auth Service НЕ предоставляет endpoints для управления ролями. Роли хранятся исключительно в Keycloak и не дублируются в PostgreSQL.

**Пример аннотации:**
```java
@GetMapping("/users/{id}")
@PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
public UserDto getUserById(@PathVariable Long id) {
    return userService.findById(id);
}
```

**Примеры:**
- Всё по RBAC: [security/rbac.md](security/rbac.md)

#### Platform Service

| Роль | Права |
|------|-------|
| BUYER | view own profile, manage favorites, search |
| SELLER | manage store, manage own products, view analytics |
| MODERATOR | moderate content, view reports |
| ADMIN | system configuration, role management via Keycloak |

**Примечание:** Роли проверяются через JWT токен (Keycloak). Пользователи управляются только через Keycloak Admin Console/API.

#### Order Service

| Роль | Права |
|------|-------|
| BUYER | create orders, view own orders |
| SELLER | view orders for own products, update order status |
| MODERATOR | view all orders |
| ADMIN | full access, role management via Keycloak |

**Примечание:** Роли проверяются через JWT токен (Keycloak). Пользователи управляются только через Keycloak Admin/API.

#### Catalog Service

| Роль | Права |
|------|-------|
| BUYER | view products, search |
| SELLER | create/edit own products |
| MODERATOR | view all products, flag inappropriate |
| ADMIN | full access, category management, role management via Keycloak |

**Примечание:** Роли проверяются через JWT токен (Keycloak). Пользователи управляются только через Keycloak Admin/API.

#### Payment Service

| Роль | Права |
|------|-------|
| BUYER | process payment for own orders |
| SELLER | view payment history for own products |
| ADMIN | full access, fraud detection |

**Примечание:** Роли проверяются через JWT токен (Keycloak). Пользователи управляются только через Keycloak Admin/API.

**Примечание:** Роли проверяются через JWT токен (Keycloak). Подробнее: [security/rbac.md](security/rbac.md)

#### Communication Service

| Роль | Права |
|------|-------|
| BUYER | send messages to sellers, receive messages |
| SELLER | send messages to buyers, receive messages |
| MODERATOR | view all messages for moderation |
| ADMIN | full access |

**Примечание:** Роли проверяются через JWT токен (Keycloak). Пользователи управляются только через Keycloak Admin/API.

**Примечание:** Роли проверяются через JWT токен (Keycloak). Подробнее: [security/rbac.md](security/rbac.md)

---

## 4. План аудита безопасности

### 4.1 Еженедельные проверки

- Логи аутентификации (неудачные попытки входа)
- Статистика по rate limiting
- Изменения в RBAC (новые роли, изменения прав) через Keycloak

**Примечание:** Изменения в RBAC происходят только в Keycloak. Подробнее: [security/rbac.md](security/rbac.md)

### 4.2 Ежемесячные аудиты

- Ревью access logs всех сервисов
- Проверка сертификатов и ключей
- Анализ CVE для используемых библиотек
- Аудит конфигураций (application.yml, docker-compose)

### 4.3 Квартальные аудиты

- Penetration testing (внешний аудит)
- Security code review
- Аудит бэкапов и восстановления
- Тестирование disaster recovery плана

### 4.4 Аудит перед релизом

- Security checklist для каждого сервиса
- Review PR с изменениями безопасности
- Тестирование новых endpoints на инъекции

### 4.5 Инструменты аудита

- **Логирование:** Loki с алертингом на подозрительные действия
- **Мониторинг:** Prometheus метрики по безопасности (error rate, auth failures)
- **Трейсинг:** Tempo для отслеживания запросов
- **Static Analysis:** SonarQube для анализа кода
- **Dynamic Analysis:** OWASP ZAP для penetration testing

---

## 5. OWASP Top 10 compliance

### 5.1 Меры по защите

#### A01:2021 – Broken Access Control
- RBAC для всех сервисов
- JWT валидация на каждом endpoint
- Rate limiting на API Gateway

#### A02:2021 – Cryptographic Failures
- BCrypt для паролей
- TLS 1.3 для внешних API
- Шифрование бэкапов

#### A03:2021 – Injection
- Prepared statements (JPA/Hibernate)
- Валидация входных данных
- SQL инъекции блокируются на уровне ORM

#### A04:2021 – Insecure Design
- Security by design原则
- Threat modeling для критичных сервисов
- Code review с упором на безопасность

#### A05:2021 – Security Misconfiguration
- Externalized configuration
- Secrets через environment variables
- Нет hardcoded credentials

#### A06:2021 – Vulnerable Components
- Regular dependency updates
- Snyk/Dependabot для мониторинга CVE
- Отказ от устаревших библиотек

#### A07:2021 – Identification and Authentication Failures
- JWT expiration (12 часов для access token, 7 дней для refresh token)
- **Механизм revoked tokens:** Redis-only подход с TTL = expires_at - current_time (для MVP)
- Rate limiting на login (5 попыток в минуту)
- Password policies (минимум 8 символов, цифры, спецсимволы)
- RBAC через `realm_access.roles` (BUYER, SELLER, MODERATOR, ADMIN)
- Подробнее: [security/rbac.md](security/rbac.md), [security/revoked-tokens.md](security/revoked-tokens.md)

#### A08:2021 – Software and Data Integrity Failures
- Signing docker images (Notary)
- Secure CI/CD pipeline (GitLab CI)
- Checksum verification для зависимостей

#### A09:2021 – Security Logging and Monitoring Failures
- Centralized logging (Loki)
- Real-time monitoring (Prometheus)
- Alerting на security events

#### A10:2021 – Server-Side Request Forgery
- URL validation (白名单 доменов)
- Restricted domains
- Internal network isolation

---

## 6. Security incident response

### 6.1 Процедура реагирования

1. **Обнаружение:** Алерт через Prometheus/Grafana
2. **Оценка:** Определение масштаба инцидента
3. **Изоляция:** Блокировка affected сервисов
4. **Устранение:** Исправление уязвимости
5. **Восстановление:** Возврат сервисов в正常ное состояние
6. **Анализ:** Post-mortem с выводами

### 6.2 Контакты в экстренной ситуации

- Security Team: #security-incidents
- Slack Alert: @security-oncall
- Emergency hotline: +7XXX-XXX-XXXX

---

## 7. Метрики безопасности

| Метрика | Описание | Целевое значение |
|---------|----------|-----------------|
| Error rate | Процент ошибок аутентификации | < 1% |
| Auth failures | Неудачные попытки входа за 5 мин | < 10 |
| Rate limit violations | Количество rate limit срабатываний | < 100 |
| TLS version | Используемая версия TLS | TLSv1.3 |
| Certificate expiry | Дни до истечения сертификата | > 30 дней |
| CVE vulnerabilities | Критичные уязвимости в зависимостях | 0 |

---

## 8. Заключение

Стратегия безопасности AutoDev Marketplace:

- **Service Account Tokens** для межсервисной аутентификации (MVP)
- **TLS 1.3** для внешних API
- **RBAC** для детального контроля доступа
- **BCrypt/PBKDF2** для шифрования паролей
- **Механизм revoked tokens** через Redis-only подход (TTL = expires_at - current_time) — защита от использования отзыванных токенов
- **План аудита** от еженедельных до квартальных проверок
- **OWASP Top 10** compliance
- **Incident response** процедура для быстрого реагирования

Система полностью защищена на уровне требований MVP с возможностью улучшения для production через MTLS и прикладное шифрование.

---

## 9. Ссылки

- [OWASP Top 10 2021](https://owasp.org/Top10/)
- [Spring Security Documentation](https://spring.io/projects/spring-security)
- [Keycloak Documentation](https://www.keycloak.org/documentation)
- [OWASP Security Cheat Sheets](https://cheatsheetseries.owasp.org/)
- [TLS Best Practices](https://tls.mbed.org/best-practices)
- [JWT RFC 7519](https://tools.ietf.org/html/rfc7519)
- [JWT Structure](security/jwt-structure.md)
