# RBAC (Role-Based Access Control) в AutoDev Marketplace

**Версия документа:** 1.0  
**Дата создания:** 2026-06-14  
**Последнее обновление:** 2026-06-14

---

## Обзор

Документ описывает систему RBAC (Role-Based Access Control) в AutoDev Marketplace.

---

## Уровни доступа

| Роль | Описание | Примеры сервисов |
|------|----------|-----------------|
| `BUYER` | Покупатель товаров | order-service, catalog-service |
| `SELLER` | Продавец товаров | catalog-service, order-service, platform-service |
| `MODERATOR` | Модератор контента | platform-service, communication-service |
| `ADMIN` | Администратор системы | auth-service, platform-service, admin-service |

---

## Маппинг ролей Keycloak → JWT claims

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
realm_access.roles → ROLE_BUYER, ROLE_SELLER, ROLE_MODERATOR, ROLE_ADMIN
```

---

## Как сервисы проверяют роли

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
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter scopesConverter = new JwtGrantedAuthoritiesConverter();
        scopesConverter.setScopeAttributeName("realm_access");
        scopesConverter.setAuthoritiesPrefix("ROLE_");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(scopesConverter);
        return converter;
    }
}
```

### Примеры аннотаций для каждого сервиса

#### Auth Service

```java
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        // Публичный endpoint
    }
    
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('BUYER', 'SELLER', 'MODERATOR', 'ADMIN')")
    public ResponseEntity<UserDto> getCurrentUser() {
        return ResponseEntity.ok(authService.getCurrentUser());
    }
    
    @PutMapping("/me")
    @PreAuthorize("hasAnyRole('BUYER', 'SELLER', 'MODERATOR', 'ADMIN')")
    public ResponseEntity<UserDto> updateProfile(@RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(authService.updateProfile(request));
    }
    
    @GetMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(authService.getUserById(id));
    }
    
    @PostMapping("/logout")
    @PreAuthorize("hasAnyRole('BUYER', 'SELLER', 'MODERATOR', 'ADMIN')")
    public ResponseEntity<Void> logout() {
        authService.logout();
        return ResponseEntity.noContent().build();
    }
}
```

#### Platform Service

```java
@RestController
@RequestMapping("/api/v1/platform")
public class PlatformController {
    
    @GetMapping("/users/profile")
    @PreAuthorize("hasAnyRole('BUYER', 'SELLER', 'MODERATOR', 'ADMIN')")
    public ResponseEntity<UserProfileDto> getProfile() {
        return ResponseEntity.ok(platformService.getProfile());
    }
    
    @PutMapping("/users/profile")
    @PreAuthorize("hasAnyRole('BUYER', 'SELLER', 'MODERATOR', 'ADMIN')")
    public ResponseEntity<UserProfileDto> updateProfile(@RequestBody UserProfileDto profile) {
        return ResponseEntity.ok(platformService.updateProfile(profile));
    }
    
    @GetMapping("/moderation/reports")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<List<ReportDto>> getReports() {
        return ResponseEntity.ok(platformService.getReports());
    }
    
    @PostMapping("/admin/config")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateConfig(@RequestBody ConfigDto config) {
        platformService.updateConfig(config);
        return ResponseEntity.noContent().build();
    }
}
```

#### Order Service

```java
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {
    
    @PostMapping("")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<OrderDto> createOrder(@RequestBody CreateOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(request));
    }
    
    @GetMapping("")
    @PreAuthorize("hasAnyRole('BUYER', 'SELLER')")
    public ResponseEntity<List<OrderDto>> getOrders() {
        return ResponseEntity.ok(orderService.getOrders());
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('BUYER', 'SELLER', 'MODERATOR', 'ADMIN')")
    public ResponseEntity<OrderDto> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }
    
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('SELLER', 'MODERATOR', 'ADMIN')")
    public ResponseEntity<OrderDto> updateOrderStatus(@PathVariable Long id, @RequestBody OrderStatusDto status) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, status));
    }
}
```

#### Catalog Service

```java
@RestController
@RequestMapping("/api/v1/catalog")
public class CatalogController {
    
    @GetMapping("/products")
    @PreAuthorize("hasAnyRole('BUYER', 'SELLER', 'MODERATOR', 'ADMIN')")
    public ResponseEntity<List<ProductDto>> getProducts() {
        return ResponseEntity.ok(catalogService.getProducts());
    }
    
    @GetMapping("/products/{id}")
    @PreAuthorize("hasAnyRole('BUYER', 'SELLER', 'MODERATOR', 'ADMIN')")
    public ResponseEntity<ProductDto> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(catalogService.getProductById(id));
    }
    
    @PostMapping("/products")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ProductDto> createProduct(@RequestBody CreateProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(catalogService.createProduct(request));
    }
    
    @PutMapping("/products/{id}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ProductDto> updateProduct(@PathVariable Long id, @RequestBody ProductDto product) {
        return ResponseEntity.ok(catalogService.updateProduct(id, product));
    }
    
    @GetMapping("/categories")
    @PreAuthorize("hasAnyRole('BUYER', 'SELLER', 'MODERATOR', 'ADMIN')")
    public ResponseEntity<List<CategoryDto>> getCategories() {
        return ResponseEntity.ok(catalogService.getCategories());
    }
}
```

#### Payment Service

```java
@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {
    
    @PostMapping("/process")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<PaymentDto> processPayment(@RequestBody PaymentRequest request) {
        return ResponseEntity.ok(paymentService.processPayment(request));
    }
    
    @GetMapping("/history")
    @PreAuthorize("hasAnyRole('BUYER', 'SELLER')")
    public ResponseEntity<List<PaymentHistoryDto>> getPaymentHistory() {
        return ResponseEntity.ok(paymentService.getPaymentHistory());
    }
    
    @GetMapping("/fraud-detection")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FraudDetectionDto> getFraudDetection() {
        return ResponseEntity.ok(paymentService.getFraudDetection());
    }
}
```

---

## Управление ролями

### Единственный источник правды

**Keycloak Admin Console** и **Keycloak Admin API** — единственные способы управления ролями.

### Keycloak Admin Console

```
http://localhost:8090/admin
```

**Как назначить роль пользователю:**
1. Зайти в Keycloak Admin Console
2. Выбрать пользователя
3. Вкладка "Role Mappings"
4. Выбрать роль и нажать "Add selected"

### Keycloak Admin API

```bash
# Назначить роль пользователю
POST /admin/realms/{realm}/users/{user_id}/role-mappings/realm

# Тело запроса
[
  {
    "id": "role-id-buyer",
    "name": "BUYER"
  },
  {
    "id": "role-id-seller",
    "name": "SELLER"
  }
]
```

### Пример через curl

```bash
# Получить access token для Admin API
TOKEN=$(curl -s -X POST \
  http://localhost:8090/realms/master/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=admin-cli" \
  -d "grant_type=password" \
  -d "username=admin" \
  -d "password=admin" | jq -r '.access_token')

# Назначить роль пользователю
curl -s -X POST \
  http://localhost:8090/admin/realms/autodev/users/123e4567-e89b-12d3-a456-426614174000/role-mappings/realm \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '[
    {
      "id": "role-id-buyer",
      "name": "BUYER"
    }
  ]'
```

---

## Управление профилем пользователя

### Ответственность сервисов

В системе существуют два уровня данных пользователя:

#### Аутентификационные данные (email, enabled)
- **Источник правды:** Keycloak
- **Обновление:** только через Keycloak Admin API
- **Примеры операций:**
  - Изменение email адреса
  - Блокировка/разблокировка пользователя (enabled)
  - Смена пароля

#### Бизнес-данные профиля (first_name, last_name, phone, favorites)
- **Источник правды:** PostgreSQL (через соответствующие сервисы)
- **Обновление:** через соответствующие микросервисы
- **Ответственные сервисы:**
  - `platform-service` - управление основным профилем (first_name, last_name, phone)
  - `catalog-service` - управление избраным (favorites)

### API endpoints для управления профилем

#### Platform Service (основной профиль)

```yaml
GET /api/v1/platform/users/profile
  - Получение профиля текущего пользователя
  - Роль: BUYER, SELLER, MODERATOR, ADMIN

PUT /api/v1/platform/users/profile
  - Обновление профиля текущего пользователя
  - Роль: BUYER, SELLER, MODERATOR, ADMIN
  - Доступные поля: first_name, last_name, phone, favorites
```

#### Auth Service (синхронизация)

**ВАЖНО:** Auth Service **не управляет** данными профиля напрямую. Он только:
- Синхронизирует данные из Keycloak в PostgreSQL при регистрации/обновлении
- Предоставляет endpoints для аутентификации (login, refresh, logout)
- Предоставляет GET `/api/v1/auth/me` для получения данных текущего пользователя из кэша Redis
- Предоставляет GET `/api/v1/auth/users/{id}` для получения пользователя по ID (для ADMIN синхронизации с PostgreSQL)

### График взаимодействия

```
┌─────────────────┐
│   Keycloak      │
│   Admin API     │
└────────┬────────┘
         │
         │ Синхронизация аутентификационных данных
         │
         ▼
┌─────────────────┐
│   Auth Service  │
│   PostgreSQL    │
│   (User table)  │
└────────┬────────┘
         │
         │ Синхронизация бизнес-данных
         │
         ▼
┌─────────────────┐
│ Platform Service│
│   PostgreSQL    │
│  (Profile table)│
└─────────────────┘
```

### Пример сценария обновления профиля

1. Пользователь отправляет запрос на обновление профиля в `platform-service`
2. `platform-service` валидирует JWT токен и проверяет роли через Spring Security
3. `platform-service` обновляет данные в своей PostgreSQL (table: `platform.user_profiles`)
4. При необходимости `platform-service` может отправить событие в `auth-service` для синхронизации
5. `auth-service` обновляет данные в своей PostgreSQL и Redis

### best practices

1. **Пишите в единственный источник правды:**
   - Аутентификационные данные → только Keycloak
   - Бизнес-данные → только соответствующий микросервис

2. **Читайте из кэша:**
   - GET `/api/v1/auth/me` → данные из Redis (синхронизировано из PostgreSQL)
   - GET `/api/v1/platform/users/profile` → данные из PostgreSQL

3. **Не дублируйте данные:**
   - Аутентификационные данные хранятся в Keycloak
   - Бизнес-данные хранятся в соответствующих сервисах (PostgreSQL)
   - Auth Service использует PostgreSQL только для синхронизации

---

## best practices

### 1. Минимизировать количество ролей

- Использовать 4 уровня доступа: BUYER, SELLER, MODERATOR, ADMIN
- Не создавать избыточные роли
- Использовать Spring Security для детального контроля

### 2. Проверять роли в каждом сервисе

- Каждый сервис валидирует токен независимо
- Не полагаться на другие сервисы для проверки ролей
- Использовать `@PreAuthorize` в контроллерах

### 3. Управлять ролями только через Keycloak

- Никогда не хранить роли в PostgreSQL
- Никогда не предоставлять endpoints для управления ролями в сервисах
- Использовать только Keycloak Admin Console или API

### 4. Аудит изменений ролей

- Включить аудит в Keycloak
- Мониторить изменения ролей через Loki
- Алерты на подозрительные действия

---

## Таблица маппинга ролей по сервисам

| Сервис | BUYER | SELLER | MODERATOR | ADMIN |
|--------|-------|--------|-----------|-------|
| Auth Service | login, refresh, logout, view profile (from cache) | login, refresh, logout, view profile (from cache) | login, refresh, logout, view profile (from cache) | login, refresh, logout, view profile (from cache), get user by ID (synchronization) |
| Platform Service | view own profile, manage favorites | manage store, manage own products, view analytics | moderate content, view reports | system configuration, role management |
| Order Service | create orders, view own orders | view orders for own products, update order status | view all orders | full access |
| Catalog Service | view products, search | create/edit own products | view all products, flag inappropriate | full access, category management |
| Payment Service | process payment for own orders | view payment history for own products | view payment history | full access, fraud detection |
| Communication Service | send messages to sellers | send messages to buyers | view all messages | full access |

---

## Заключение

RBAC в AutoDev Marketplace:

1. **4 уровня доступа:** BUYER, SELLER, MODERATOR, ADMIN
2. **Маппинг:** `realm_access.roles` → Spring Security `ROLE_*`
3. **Проверка:** каждый сервис проверяет токен через Spring Security `@PreAuthorize`
4. **Управление:** только через Keycloak Admin Console или API
5. **best practices:** минимизировать роли, проверять в каждом сервисе, аудит изменений

---

## Ссылки

- [Keycloak RBAC](https://www.keycloak.org/docs/latest/server_admin/#_about_roles)
- [Spring Security Authorization](https://docs.spring.io/spring-security/reference.authorization.html)
- [JWT Structure](jwt-structure.md)

---

## История изменений

| Версия | Дата | Автор | Описание |
|--------|------|-------|----------|
| 1.0 | 2026-06-14 | Архитектор | Создание документа, описание RBAC схемы |

---

**Контакты:**
- Архитектор: #architecture-team
- Backend Team: #backend-team
