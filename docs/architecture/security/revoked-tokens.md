# Механизм revoked tokens в AutoDev Marketplace (Redis-only подход для MVP)

**Версия документа:** 1.1  
**Дата создания:** 2026-06-14  
**Последнее обновление:** 2026-06-14

---

## Обзор

Документ описывает механизм revoked tokens для защиты от использования отзыванных JWT токенов в системе AutoDev Marketplace.

**ВАЖНО:** Для MVP используется **Redis-only подход** - revoked tokens хранятся только в Redis. PostgreSQL таблица `auth.revoked_tokens` УДАЛЕНА из миграций.

---

## Архитектура механизма (Redis-only)

```
┌──────────────┐
│   Client     │
│ (with token) │
└──────┬───────┘
       │
       │ Request with JWT token
       ▼
┌──────────────┐
│  Auth Service│
│              │
│  - Redis cache (ONLY storage)  │
└──────┬───────┘
       │
       │ Token hash
       ▼
┌──────────────┐
│    Redis     │
│ (TTL = expires_at - current_time) │
└──────┬───────┘
```

---

## Ключевые принципы

### 1. Redis как ЕДИНСТВЕННЫЙ источник хранения
- Быстрая проверка (O(1) complexity)
- TTL = expires_at - current_time
- Автоматическая очистка после истечения TTL
- **NO PostgreSQL fallback** - таблица `auth.revoked_tokens` УДАЛЕНА

### 2. TTL механизм
- Ключ `auth:blacklist:{token_hash}` живёт ровно столько, сколько живёт токен
- При logout TTL рассчитывается как `expires_at - current_time`
- Redis автоматически удаляет ключ после истечения TTL

### 3. Идемпотентность logout
- Повторный logout одного токена безопасен
- Redis SETNX обеспечивает идемпотентность

---

## Flow добавления при logout (Redis-only)

```
sequenceDiagram
    participant Client
    participant AuthService
    participant Redis

    Client->>AuthService: POST /api/v1/auth/logout
    Note over Client,AuthService: Authorization: Bearer {access_token}
    
    AuthService->>AuthService: Validate token structure
    AuthService->>AuthService: Extract token_hash (SHA-256)
    AuthService->>AuthService: Extract expires_at from token
    
    AuthService->>Redis: SETNX auth:blacklist:{token_hash} 1
    Note over AuthService,Redis: TTL = expires_at - current_time
    Redis-->>AuthService: OK
    
    AuthService->>Redis: DEL auth:token:{token_hash}
    Note over AuthService,Redis: Invalidate JWT cache
    Redis-->>AuthService: OK
    
    AuthService-->>Client: HTTP 204 No Content
    Note over Client,AuthService: Logout successful
```

---

## Flow проверки при каждом запросе (Redis-only)

```
sequenceDiagram
    participant Client
    participant AuthService
    participant Redis

    Client->>AuthService: Any request with JWT token
    AuthService->>AuthService: Extract token_hash (SHA-256)
    
    AuthService->>Redis: GET auth:blacklist:{token_hash}
    alt Token found in Redis
        Redis-->>AuthService: 1 (exists)
        AuthService-->>Client: HTTP 401 Unauthorized
        Note over Client,AuthService: Token rejected due to revocation
    else Token not in Redis
        Redis-->>AuthService: nil
        AuthService->>AuthService: Token is valid
        AuthService-->>Client: HTTP 200 OK
        Note over Client,AuthService: Request processed
    end
```

---

## Redis ключи (revoked tokens)

| Ключ | Тип | TTL | Описание |
|------|-----|-----|----------|
| `auth:blacklist:{token_hash}` | String | expires_at - current_time | Revoked token marker |
| `auth:token:{token_hash}` | Hash | 12 hours | Token status (valid/revoked) |
| `auth:user:{keycloak_id}` | Hash | 1 hour | User data cache |

### Примеры ключей:
```
auth:blacklist:a1b2c3d4e5f6...
auth:token:a1b2c3d4e5f6...
auth:user:123e4567-e89b-12d3-a456-426614174000
```

---

## PostgreSQL таблица

**УДАЛЕНА для MVP.**

Таблица `auth.revoked_tokens` больше не используется. Все revoked tokens хранятся только в Redis.

### Причина удаления:
- Упрощение архитектуры для MVP
- Redis достаточен для хранения revoked tokens с TTL
- Нет необходимости в долгосрочном хранении revoked tokens
- Снижение сложности синхронизации между Redis и PostgreSQL

### Альтернатива для production:
Если в будущем потребуется долгосрочное хранение revoked tokens для аудита, можно добавить:
1. Redis Streams для хранения истории logout операций
2. Отдельную таблицу `auth.revoked_tokens_audit` для аудита (без требования TTL)

---

---

## Scheduled cleanup job

**НЕ ТРЕБУЕТСЯ для MVP.**

Redis автоматически удаляет ключи после истечения TTL, поэтому ручная очистка не требуется.

### Когда требуется очистка:
- Если в production добавлена таблица `auth.revoked_tokens_audit` для аудита
- В этом случае требуется ежедневная очистка старых записей

### Пример (для production с аудитом):

```java
@Service
public class RevokedTokenAuditCleanupService {
    
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanupExpiredRevokedTokens() {
        LocalDate threeYearsAgo = LocalDate.now().minusYears(3);
        
        auditRepository.deleteByCreatedAtBefore(threeYearsAgo);
        
        log.info("Cleaned up revoked tokens audit older than {}", threeYearsAgo);
    }
}
```

---

## Алгоритм валидации токена (Java) (Redis-only)

```java
@Service
public class TokenValidationService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    public boolean isTokenRevoked(String token) {
        String tokenHash = sha256(token);
        String redisKey = "auth:blacklist:" + tokenHash;
        
        // Проверка в Redis (ЕДИНСТВЕННЫЙ источник)
        Boolean isRevoked = redisTemplate.opsForValue().get(redisKey);
        return isRevoked != null && Boolean.TRUE.equals(isRevoked);
    }
}
```

---

## Метрики мониторинга

| Метрика | Описание | Тип |
|---------|----------|-----|
| `auth_logout_total` | Количество logout операций | Counter |
| `auth_revoked_tokens_total` | Количество revoked tokens (Redis) | Gauge |
| `auth_revoked_tokens_redis_hits` | Попадания в Redis кэш | Counter |
| `auth_invalid_tokens_rejected` | Отклоненные токены | Counter |

### Алерты

| Алерт | Условие | Действие |
|-------|---------|----------|
| `HighRevokedTokens` | revoked_tokens > 10000 | Алерт в Slack |
| `HighInvalidTokens` | invalid_tokens > 100 за 5 мин | Алерт в Slack |

---

## Риски и ограничения

### 1. Потеря revoked tokens при падении Redis

**Описание:** При падении Redis без持久ности все revoked tokens будут потеряны.

**Решение:**
- Включить Redis AOF (Append Only File) для持久ности
- Или использовать Redis Cluster с репликацией
- Для MVP это допустимый риск (tokens истекают по TTL)

### 2. Утечка памяти в Redis

**Описание:** Большое количество revoked tokens может занять память.

**Решение:**
- Все ключи имеют TTL = expires_at - current_time (автоматическая очистка)
- Мониторинг Redis memory usage через Prometheus
- Настроить `maxmemory` и `maxmemory-policy` в Redis

### 3. Производительность при большом количестве revoked токенов

**Описание:** При большом количестве revoked tokens может замедлиться проверка.

**Решение:**
- Redis как primary cache (O(1) complexity) - уже оптимизировано
- Redis Cluster для горизонтального масштабирования при необходимости
- Мониторинг latency через Prometheus

---

## Заключение

Механизм revoked tokens обеспечивает защиту от использования отзыванных токенов через:

1. **Redis cache** — единственный источник хранения (TTL = expires_at - current_time)
2. **Автоматическая очистка** — Redis удаляет ключи после истечения TTL
3. **Идемпотентность** — повторный logout безопасен
4. **Простота** — упрощённая архитектура без PostgreSQL синхронизации

**KPI:**
- Время проверки revoked токена: < 5ms (из Redis)
- Вероятность утечки revoked токена: < 0.001% (при включенной AOF)
- Скорость автоматической очистки: 100% после истечения TTL

---

## Ссылки

- [JWT Security Best Practices](https://auth0.com/docs/security/tokens/json-web-tokens)
- [Redis Best Practices](https://redis.io/topics/best-practices)
- [Auth Service Specification](../api-specification/auth-service.yaml)

---

## История изменений

| Версия | Дата | Автор | Описание |
|--------|------|-------|----------|
| 1.0 | 2026-06-14 | Архитектор | Создание документа, описание механизма revoked tokens |
| 1.1 | 2026-06-14 | Архитектор | Обновление для Redis-only подхода (MVP), удалена таблица auth.revoked_tokens, упрощена архитектура

---

**Контакты:**
- Архитектор: #architecture-team
- Backend Team: #backend-team
