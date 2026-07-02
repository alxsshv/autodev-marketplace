# Стандарты межсервисной коммуникации для AutoDev Marketplace

**Версия документа:** 1.0  
**Дата создания:** 2026-07-01  
**Последнее обновление:** 2026-07-01  
**Статус:** Документ архитектурных стандартов

---

## Предисловие

Этот документ определяет стандарты межсервисной коммуникации для микросервисной архитектуры AutoDev Marketplace. Он описывает правила передачи данных между сервисами, форматы сообщений, заголовки и другие важные аспекты.

---

## Заголовки для передачи claims из JWT токена

### Цель

При прохождении запроса через API Gateway claims из JWT токена должны быть извлечены и переданы downstream сервисам через HTTP headers. Это позволяет сервисам знать о пользователе без необходимости повторной валидации JWT токена.

### Стандартизированные заголовки

| Заголовок | JWT Claim | Описание | Обязательный | Пример значения |
|-----------|-----------|----------|-------------|-----------------|
| `X-User-Id` | `sub` | Уникальный идентификатор пользователя | Да | `550e8400-e29b-41d4-a716-446655440000` |
| `X-User-Email` | `email` | Email пользователя | Да | `user@example.com` |
| `X-User-Name` | `preferred_username` или `name` | Имя пользователя | Да | `ivan_ivanov` |
| `X-User-Roles` | `roles` или `realm_access.roles` | Роли пользователя | Да | `BUYER,MODERATOR` |
| `traceparent` | — | OpenTelemetry Trace Context (W3C Trace Context) | Да | `00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01` |
| `tracestate` | — | OpenTelemetry Trace Context ( Vendor-specific trace state) | Нет | `congo=t61rcWkgMzE` |

**Примечание:** Вместо кастомного `X-Request-ID` используется стандартный `traceparent` заголовок по W3C Trace Context, который поддерживается OpenTelemetry и интегрируется с Tempo для трейсинга и Loki для логирования.

### Схема передачи claims

```
Client Request (with JWT token)
    ↓
[API Gateway]
    ↓
1. Валидация JWT токена через Keycloak
2. Извлечение claims из токена
3. Добавление standard headers в запрос
    ↓
Downstream Service (receives claims via headers)
```

**ВАЖНО: Downstream-сервисы должны самостоятельно валидировать JWT и проверять роли, а не доверять заголовкам.**

### Критические правила безопасности для Downstream-сервисов

1. **Самостоятельная валидация JWT**:
   - Каждый downstream-сервис должен валидировать JWT токен через Keycloak или кэш в Redis
   - Нельзя полагаться только на заголовки (X-User-Id, X-User-Roles и т.д.)
   - Заголовки могут быть подделаны или устареть

2. **Проверка ролей (RBAC)**:
   - Downstream-сервисы должны извлекать роли из JWT токена, а не из заголовков
   - Проверка ролей должна происходить на каждом endpoint перед выполнением операции
   - Даже если заголовок `X-User-Roles` присутствует, он не может быть единственным источником прав

3. **Доверие к заголовкам**:
   - Заголовки (X-User-Id, X-User-Email, X-User-Name) могут использоваться только для:
     - Логирования (MDC, аудит)
     - Персонализации ответов
     - Улучшения user experience
   - Заголовки НИКОГДА не должны использоваться для принятия решений о доступе

4. **Приоритет источников**:
   - Источник 1 (наивысший приоритет): JWT токен, валидированный через Keycloak
   - Источник 2: Кэшированный JWT в Redis (если токен валиден и не истёк)
   - Источник 3 (низкий приоритет): Заголовки для логирования (только после валидации токена)

### Пример правильной реализации в downstream-сервисе

```java
@RestController
@RequestMapping("/api/v1/products")
public class ProductController {
    
    @PreAuthorize("hasRole('BUYER')")
    @GetMapping
    public List<ProductDto> getAllProducts() {
        // Spring Security автоматически проверит роли из JWT, а не из заголовков
        return productService.getAllProducts();
    }
    
    @PreAuthorize("hasRole('SELLER')")
    @PostMapping
    public ProductDto createProduct(@RequestBody ProductCreateDto dto) {
        // Даже если заголовок X-User-Roles содержит SELLER,
        // проверка происходит по валидированному JWT токену
        return productService.createProduct(dto);
    }
}
```

**НЕПРАВИЛЬНО:**

```java
// ❌ ПЛОХОЙ ПРИМЕР - НЕ СЛЕДУЕТ ПИСАТЬ ТАК
@GetMapping
public List<ProductDto> getAllProducts(@RequestHeader("X-User-Roles") String roles) {
    if (roles.contains("SELLER")) {
        // Так делать нельзя! Заголовок может быть подделан
        return productService.getAllProducts();
    }
    throw new AccessDeniedException("Access denied");
}
```

### Пример конфигурации API Gateway

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .oauth2ResourceServer(oauth2 -> oauth2
            .jwt(jwt -> jwt
                .jwtAuthenticationConverter(jwtAuthenticationConverter())
            )
        )
        .authorizeHttpRequests(authz -> authz
            .requestMatchers("/actuator/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
            .anyRequest().authenticated()
        );
    
    return http.build();
}

@Bean
public JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
    
    // Настройка конвертера для извлечения claims и добавления их в headers
    converter.setJwtGrantedAuthoritiesConverter(jwt -> {
        // Извлечение ролей из JWT
        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        
        // Проверка realm_access.roles
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess != null && realmAccess.containsKey("roles")) {
            ((List<String>) realmAccess.get("roles")).forEach(role -> 
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
            );
        }
        
        // Проверка resource_access для client-level roles
        Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
        if (resourceAccess != null) {
            resourceAccess.forEach((client, access) -> {
                if (access instanceof Map) {
                    ((List<String>) ((Map<String, Object>) access).get("roles")).forEach(role -> 
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                    );
                }
            });
        }
        
        return authorities;
    });
    
    return converter;
}
```

### Пример pre-filter для добавления headers в Spring Cloud Gateway

```java
@Component
public class ClaimsPropagationFilter implements GlobalFilter {
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // Извлечение Authentication из SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication instanceof JwtAuthenticationToken) {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            
            // Формирование новых заголовков
            MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
            headers.add("X-User-Id", jwt.getSubject());
            headers.add("X-User-Email", jwt.getClaimAsString("email"));
            headers.add("X-User-Name", jwt.getClaimAsString("preferred_username"));
            headers.add("X-User-Roles", String.join(",", jwt.getClaimAsStringArray("roles")));
            
            // OpenTelemetry Trace Context (W3C Trace Context)
            // traceparent и tracestate автоматически передаются через Spring Cloud Gateway
            // или могут быть добавлены вручную если нужно
            headers.add("traceparent", jwt.getClaimAsString("traceparent"));
            headers.add("tracestate", jwt.getClaimAsString("tracestate"));
            
            // Создание нового запроса с обновлёнными заголовками
            ServerHttpRequest newRequest = request.mutate()
                .headers(httpHeaders -> {
                    httpHeaders.addAll(headers);
                })
                .build();
            
            return chain.filter(exchange.mutate().request(newRequest).build());
        }
        
        return chain.filter(exchange);
    }
}
```

**Примечание:** Spring Cloud Gateway автоматически передаёт `traceparent` и `tracestate` заголовки (OpenTelemetry Trace Context) между сервисами. Это обеспечивает согласованность трейсинга через всю цепочку сервисов с интеграцией в Tempo и Loki.

---

## Стандарты HTTP заголовков для API

### Обязательные заголовки для всех API запросов

| Заголовок | Обязательный | Описание |
|-----------|-------------|----------|
| `Authorization` | Да | JWT токен в формате `Bearer {token}` |
| `Content-Type` | Да | `application/json` для JSON запросов |
| `Accept` | Да | `application/json` |
| `traceparent` | Да | OpenTelemetry Trace Context (W3C Trace Context) |

### Рекомендуемые заголовки

| Заголовок | Описание | Пример |
|-----------|----------|--------|
| `X-Language` | Язык интерфейса | `ru-RU`, `en-US` |
| `X-Timezone` | Часовой пояс | `Europe/Moscow` |
| `X-Device-Info` | Информация об устройстве | `web/1.0.0`, `mobile/2.1.0` |

### Заголовки ответов

| Заголовок | Описание |
|-----------|----------|
| `Content-Type` | `application/json` |
| `traceparent` | OpenTelemetry Trace Context из запроса |
| `X-RateLimit-Limit` | Лимит запросов (если используется rate limiting) |
| `X-RateLimit-Remaining` | Оставшееся количество запросов |
| `X-RateLimit-Reset` | Время сброса лимита (Unix timestamp) |

---

## Обработка ошибок

### Стандартные заголовки ошибок

| Заголовок | Описание |
|-----------|----------|
| `X-Error-Code` | Код ошибки (машиночитаемый) |
| `X-Error-Message` | Сообщение об ошибке (человекочитаемое) |
| `X-Trace-ID` | ID трейса для диагностики |

### Пример ответа с ошибкой

```json
{
  "errorCode": "VALIDATION_ERROR",
  "message": "Invalid email format",
  "timestamp": "2026-07-01T12:34:56Z",
  "path": "/api/v1/platform/users/register"
}
```

С заголовками:
```
X-Error-Code: VALIDATION_ERROR
X-Error-Message: Invalid email format
X-Trace-ID: abc123-def456-ghi789
```

---

## Безопасность

### Запрещённые заголовки

Следующие заголовки **запрещено** передавать между сервисами:

| Заголовок | Причина |
|-----------|---------|
| `Authorization` | Токен уже валидирован API Gateway |
| `Password`, `Secret`, `Key` | Чувствительные данные не должны передаваться |
| `Cookie` | Сессии не используются в микросервисной архитектуре |

### Проверка заголовков

Каждый сервис должен:

1. **Валидировать обязательные заголовки** — отклонять запросы без обязательных заголовков (`traceparent`, `X-User-Id`, `X-User-Email`, `X-User-Name`, `X-User-Roles`)
2. **Логировать отсутствующие заголовки** — записывать предупреждения в лог с использованием MDC и trace ID
3. **Проверять формат заголовков** — валидировать UUID для `X-User-Id`, email для `X-User-Email`, W3C Trace Context для `traceparent`

---

## Обновление стандартов

Стандарты межсервисной коммуникации могут обновляться по следующим причинам:

1. **Добавление новых claim'ов** — при необходимости передачи дополнительной информации
2. **Устаревание заголовков** — при переходе на новые стандарты
3. **Изменение требований безопасности** — при обновлении OWASP Top 10 или других стандартов

Обновления стандартов должны быть задокументированы и согласованы с командой архитекторов.

---

## Ссылки

- `docs/architecture/system-overview.md` - раздел 5 "Основные функциональные модули" (API Gateway)
- `docs/architecture/system-overview.md` - раздел 8.2 "Межсервисная аутентификация" (Direct Keycloak Integration)
- `docs/tasks/003-1-api-gateway-authentication-filter.md` - задача по настройке JWT валидации

---

**Ответственные:**
- **Архитектор:** Проектирование и согласование стандартов
- **Team Lead:** Внедрение стандартов в команде
- **DevOps:** Мониторинг соответствия стандартам
