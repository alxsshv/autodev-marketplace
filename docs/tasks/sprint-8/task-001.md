# Sprint 8: Rate Limiting

**Эпик:** TS-003-SPRINT-8  
**Статус:** To Do  
**Приоритет:** Normal  
**Оценка времени:** 3 дня (24 часа)  
**Блокирует:** Нет (дополнительная функциональность для MVP)  
**Зависимости:** Sprint 1 (Redis, Base models)

---

## Описание

Настройка rate limiting для защиты от злоупотреблений:
- Лимит входа: 5 попыток за 5 минут на IP-адрес
- Лимит привязки провайдеров: 3 попытки за 10 минут на пользователя
- Лимит админ-операций: 10 попыток за 1 минуту на пользователя

---

## Задачи

### 8.1. Redis rate limiting repository

**Оценка:** 8 часов  
**Критерии приёмки:**
- AC-8.1: `RedisRateLimitRepository.saveCounter()` создает ключ `rate:login:{ip}:{5min_window}` со счетчиком
- AC-8.2: `RedisRateLimitRepository.incrementCounter()` увеличивает счетчик через Redis INCR
- AC-8.3: `RedisRateLimitRepository.isRateLimited()` проверяет превышение лимита
- AC-8.4: Unit тест `RedisRateLimitRepositoryTest.loginRateLimit()` проверяет 5 попыток за 5 минут

**Зависимости:** Sprint 1 (Redis конфигурация)

---

### 8.2. Rate limiting filter

**Оценка:** 8 часов  
**Критерии приёмки:**
- AC-8.5: `RateLimitingFilter` проверяет лимит для `/api/v1/auth/login` (5/5min на IP)
- AC-8.6: `RateLimitingFilter` проверяет лимит для `/api/v1/auth/external/link` (3/10min на user_id)
- AC-8.7: `RateLimitingFilter` проверяет лимит для `/api/v1/admin/**` (10/1min на user_id)
- AC-8.8: При превышении лимита возврат `429 Too Many Requests` с JSON: `{ "error": "RateLimitExceeded", "message": "Too many requests", "retry_after": 123 }`
- AC-8.9: Unit тест `RateLimitingFilterTest.loginRateLimit()` проверяет 429 при превышении

**Зависимости:** Sprint 1 (Redis), 8.1 (Redis rate limiting repository)

---

### 8.3. Audit logging rate limiting

**Оценка:** 8 часов  
**Критерии приёмки:**
- AC-8.10: `AuditLogger.logRateLimitExceeded()` пишет событие `RATE_LIMIT_EXCEEDED` в `auth.audit_logs`
- AC-8.11: Лог включает: user_id, ip, endpoint, timestamp
- AC-8.12: Unit тест `AuditLoggerTest.rateLimitExceeded()` проверяет запись в базу

**Зависимости:** Sprint 1 (PostgreSQL, Base models), 8.2 (Rate limiting filter)

---

## Критерии готовности спринта

- [ ] Лимит входа: 5 попыток за 5 минут на IP-адрес (429 при превышении)
- [ ] Лимит привязки провайдеров: 3 попытки за 10 минут на пользователя (429 при превышении)
- [ ] Лимит админ-операций: 10 попыток за 1 минуту на пользователя (429 при превышении)
- [ ] Аудит логи пишутся в `auth.audit_logs` при превышении лимита
- [ ] Демонстрация: 429 возвращается при превышении лимита

---

## Зависимости от других спринтов

**Этот спринт не блокирует другие спринты** — добавляется к существующей функциональности.

**Зависит от:**
- Sprint 1 (Redis для счетчиков, PostgreSQL для audit logs)

---

## GitLab issue structure

```
TS-003-SPRINT-7: Sprint 7: OAuth2 Provider Management (Epic)
├── TS-003-SPRINT-8: Sprint 8: Rate Limiting (Epic)
│   ├── TS-003-8.1: Redis rate limiting repository (Issue)
│   ├── TS-003-8.2: Rate limiting filter (Issue)
│   └── TS-003-8.3: Audit logging rate limiting (Issue)
```

---

## Примечания

- Все лимиты настраиваются через `RateLimitProperties` (`@ConfigurationProperties`)
- TTL ключей Redis автоматически устанавливается равным window size (5 min, 10 min, 1 min)
- Rate limiting работает на уровне Spring Security Filter (до контроллеров)
- Аудит логи пишутся асинхронно (не блокируют выполнение)
