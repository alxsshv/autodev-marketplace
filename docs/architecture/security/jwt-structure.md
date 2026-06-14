# JWT структура и маппинг ролей в AutoDev Marketplace

**Версия документа:** 1.0  
**Дата создания:** 2026-06-14  
**Последнее обновление:** 2026-06-14

---

## Обзор

Документ описывает структуру JWT токенов в системе AutoDev Marketplace и маппинг ролей из Keycloak в JWT claims.

---

## Общая структура JWT токена

```
┌─────────────────────────────────────────────────────────────┐
│ Header                          │ Payload                   │ Signature
└─────────────────────────────────────────────────────────────┘
```

### Header

```json
{
  "alg": "RS256",
  "typ": "JWT"
}
```

### Payload

Обязательные claims (согласно JWT спецификации):

| Claim | Описание | Пример |
|-------|----------|--------|
| `sub` | Subject (ID пользователя) | `123e4567-e89b-12d3-a456-426614174000` |
| `iss` | Issuer (Keycloak URL) | `https://keycloak.autodev.local` |
| `aud` | Audience (получатели) | `["api-gateway", "auth-service"]` |
| `exp` | Expiration time (Unix timestamp) | `1720000000` |
| `iat` | Issued at (Unix timestamp) | `1719996400` |

Дополнительные claims (Keycloak специфичные):

| Claim | Описание | Пример |
|-------|----------|--------|
| `realm_access.roles` | Роли пользователя в realm | `["BUYER", "SELLER"]` |
| `resource_access.{client}.roles` | Роли для конкретного client | `{"api-gateway": ["ADMIN"]}` |
| `email` | Email пользователя | `user@example.com` |
| `preferred_username` | Имя пользователя | `john_doe` |
| `given_name` | Имя | `John` |
| `family_name` | Фамилия | `Doe` |

---

## Маппинг ролей

### Ключевой принцип

**Ключевая роль:** `realm_access.roles` (стандарт Keycloak)

### Пример маппинга

```json
{
  "realm_access": {
    "roles": ["BUYER", "SELLER"]
  },
  "resource_access": {
    "api-gateway": {
      "roles": ["ADMIN"]
    }
  }
}
```

### Spring Security маппинг

Spring Security автоматически маппит `realm_access.roles` в Spring Authority:

```
realm_access.roles → ROLE_BUYER, ROLE_SELLER
```

---

## Пример JWT payload с ролями

```json
{
  "exp": 1720000000,
  "iat": 1719996400,
  "auth_time": 1719996400,
  "jti": "abc-123-def",
  "sub": "123e4567-e89b-12d3-a456-426614174000",
  "typ": "Bearer",
  "azp": "api-gateway",
  "session_state": "xyz-789",
  "acr": "1",
  "realm_access": {
    "roles": ["BUYER", "SELLER", "MODERATOR"]
  },
  "resource_access": {
    "api-gateway": {
      "roles": ["ADMIN"]
    },
    "auth-service": {
      "roles": ["SERVICE_AUTH"]
    }
  },
  "scope": "email profile",
  "email_verified": true,
  "name": "John Doe",
  "preferred_username": "john_doe",
  "given_name": "John",
  "family_name": "Doe",
  "email": "user@example.com"
}
```

---

## TTL JWT токенов

| Токен | TTL | Описание |
|-------|-----|----------|
| **Access token** | 12 часов | Для аутентификации запросов |
| **Refresh token** | 7 дней | Для получения нового access token |

### Кэширование в Redis

- **Access token TTL:** 12 часов
- **Refresh token TTL:** 7 дней
- **Revoked tokens:** до истечения TTL (TTL = expires_at - current_time)

### Ключи Redis

```
auth:token:{token_hash}       — JWT токен (валидный или revoked)
auth:blacklist:{token_hash}   — revoked токен (до истечения TTL)
auth:token:{refresh_token_hash} — refresh token (до истечения TTL)
```

---

## Обработка ролей в Spring Security

### Конвертер JWT в Spring Security

```java
@Bean
public JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtGrantedAuthoritiesConverter scopesConverter = new JwtGrantedAuthoritiesConverter();
    scopesConverter.setScopeAttributeName("realm_access");
    scopesConverter.setAuthoritiesPrefix("ROLE_");

    JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
    converter.setJwtGrantedAuthoritiesConverter(scopesConverter);
    return converter;
}
```

### Проверка ролей в контроллерах

```java
@GetMapping("/users")
@PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
public List<UserDto> getUsers() {
    return userService.getAllUsers();
}

@GetMapping("/products")
@PreAuthorize("hasAnyRole('BUYER', 'SELLER')")
public List<ProductDto> getProducts() {
    return productService.getAllProducts();
}

@PostMapping("/orders")
@PreAuthorize("hasRole('BUYER')")
public ResponseEntity<OrderDto> createOrder(@RequestBody CreateOrderRequest request) {
    Order order = orderService.createOrder(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(mapToDto(order));
}
```

### RBAC уровни доступа

| Роль | Описание | Примеры сервисов |
|------|----------|-----------------|
| `BUYER` | Покупатель товаров | order-service, catalog-service |
| `SELLER` | Продавец товаров | catalog-service, order-service, platform-service |
| `MODERATOR` | Модератор контента | platform-service, communication-service |
| `ADMIN` | Администратор системы | auth-service, platform-service, admin-service |

---

## Валидация токена в каждом сервисе

### Spring Security конфигурация

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/v1/**").authenticated()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            );
        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        // Используем публичные ключи из Redis или Keycloak
        return NimbusJwtDecoder.withJwkSetUri(
            "http://localhost:8090/realms/autodev/protocol/openid-connect/certs"
        ).build();
    }
}
```

### Проверка в Redis кэше

```java
@Service
public class TokenValidationService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    public boolean isTokenValid(String token) {
        String tokenHash = sha256(token);
        String redisKey = "auth:token:" + tokenHash;
        
        // Проверка в Redis
        String tokenStatus = redisTemplate.opsForValue().get(redisKey);
        if ("revoked".equals(tokenStatus)) {
            return false;
        }
        
        // Валидация через Keycloak (если не в кэше)
        return keycloakService.validateToken(token);
    }
}
```

---

## Безопасность

### Рекомендации

1. **Использовать HTTPS** для всех вызовов
2. **Проверять exp claim** на истечение срока
3. **Проверять iss claim** на валидность (только Keycloak)
4. **Проверять aud claim** на соответствие сервису
5. **Кэшировать публичные ключи** в Redis для быстрой валидации
6. **Revoked tokens:** проверка только через Redis (Redis-only для MVP)

---

## Миграция существующих токенов

### Если уже есть JWT токены

**Шаг 1:** Обновить все токены при следующей авторизации

**Шаг 2:** Использовать `realm_access.roles` вместо `roles` в payload

**Шаг 3:** Обновить Spring Security конфигурацию

### Пример миграции

```java
// Старая конфигурация
scopesConverter.setScopeAttributeName("scope");

// Новая конфигурация
scopesConverter.setScopeAttributeName("realm_access");
```

---

## Заключение

JWT структура в AutoDev Marketplace:

1. **Header:** алгоритм RS256
2. **Payload:** обязательные claims (sub, iss, aud, exp, iat) + Keycloak claims (realm_access.roles)
3. **Роли:** `realm_access.roles` → Spring Authority (ROLE_*)
4. **TTL:** Access token (12 часов), Refresh token (7 дней)
5. **Кэширование:** Redis (TTL = expires_at - current_time)
6. **Валидация:** каждый сервис проверяет токен через Redis (Redis-only для MVP)

---

## Ссылки

- [JWT RFC 7519](https://tools.ietf.org/html/rfc7519)
- [Keycloak JWT Documentation](https://www.keycloak.org/docs/latest/securing_apps/)
- [Spring Security JWT](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html)

---

## История изменений

| Версия | Дата | Автор | Описание |
|--------|------|-------|----------|
| 1.0 | 2026-06-14 | Архитектор | Создание документа, описание JWT структуры |

---

**Контакты:**
- Архитектор: #architecture-team
- Backend Team: #backend-team
