# Стандарты межсервисной коммуникации для AutoDev Marketplace

Версия документа: 2.0
Дата создания: 2026-07-01
Последнее обновление: 2026-07-13
Статус: Документ архитектурных стандартов (Актуально для 6 сервисов MVP)

---

## Предисловие

Этот документ определяет стандарты межсервисной коммуникации. В версии 2.0 мы полностью отказались от подхода "API Gateway парсит JWT и пробрасывает внутренние заголовки" в пользу Direct JWT Propagation (Слепой прокси).

Это устраняет критическое противоречие, повышает безопасность (Zero-Trust между сервисами) и полностью соответствует стандартам Spring Security OAuth2 Resource Server.

---

# 1. Синхронная коммуникация (REST / OpenFeign)
##   1.1. Правило распространения аутентификации (JWT)

### API Gateway:

- Выступает в роли слепого прокси (Blind Proxy).
- НЕ валидирует JWT токен.
- НЕ извлекает claims (sub, roles, email).
- Пробрасывает заголовок Authorization: Bearer <token> в downstream-сервисы без изменений.

### Downstream-сервисы (Catalog, Order, Platform и др.):

- Настроены как OAuth2 Resource Server.
- Самостоятельно валидируют подпись JWT через JWK (Public Keys) от Keycloak.
- Самостоятельно извлекают sub (User ID), email и realm_access.roles в SecurityContext.
- Принимают решения об авторизации (@PreAuthorize) на основе валидированного JWT, а не заголовков.


## 1.2. Межсервисные вызовы (Service-to-Service)

Когда сервису A нужно синхронно вызвать сервис B (например, Order Service проверяет наличие в Catalog Service через OpenFeign), токен текущего пользователя также должен быть передан.

Стандарт: Использование FeignRequestInterceptor для проброса текущего контекста безопасности.

```java
@Component
public class JwtFeignInterceptor implements RequestInterceptor {
    
    @Override
    public void apply(RequestTemplate template) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            String tokenValue = jwtAuth.getToken().getTokenValue();
            template.header("Authorization", "Bearer " + tokenValue);
        }
    }
}
```

## 1.3. Запрещенные практики (Антипаттерны)

- ❌ ЗАПРЕЩЕНО формировать и передавать заголовки X-User-Id, X-User-Email, X-User-Roles из Gateway или между сервисами.
- ❌ ЗАПРЕЩЕНО читать пользовательские данные из заголовков в downstream-сервисах для принятия бизнес-решений (это нарушает Zero-Trust).
- ❌ ЗАПРЕЩЕНО удалять или подменять заголовок Authorization при маршрутизации.

# 2. Асинхронная коммуникация (Apache Kafka)
Поскольку в асинхронном взаимодействии нет HTTP-запроса и заголовков, контекст пользователя и запроса должен быть встроен в тело сообщения (Payload).

## 2.1. Стандарт формата события (CloudEvents)
Все события в Kafka должны соответствовать формату CloudEvents (упрощенная версия) для унификации обработки.

```json
{
  "eventId": "3f7b1c92-8e45-4f7a-9d6c-1e2b3a4d5c6e",
  "eventType": "autodev.order.created.v1",
  "timestamp": "2026-07-13T10:00:00Z",
  "traceId": "00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01",
  "payload": {
    "orderId": "ord_12345",
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "userEmail": "buyer@example.com",
    "totalAmount": 5000.00,
    "items": []
  }
}
```

Важно: Поле userId и другие данные пользователя дублируются в payload события. Consumer-сервис (Notification Service) не может запросить JWT токен, поэтому он должен получить идентификатор пользователя из самого события.


## 2.2. Паттерн Transactional Outbox (Стандарт публикации)
Для гарантированной доставки событий (чтобы не потерять данные при падении приложения после коммита БД) используется паттерн Outbox:

- Сервис пишет бизнес-данные и событие в таблицу outbox_events в одной транзакции (@Transactional).
- Фоновый джоб (Scheduled Task) читает не отправленные события из outbox и публикует их в Kafka через KafkaTemplate.
- После успешного подтверждения (ack) от Kafka, событие помечается как отправленное.

## 2.3. Паттерн Idempotent Consumer (Стандарт потребления)
Consumer-сервисы должны быть идемпотентными (обработка одного и того же события дважды не должна вызывать побочных эффектов).

- Используется поле eventId из CloudEvents.
- Notification Service перед отправкой email/SMS проверяет по eventId в своей БД (notification_db), не отправлял ли он уже это уведомление.

## 2.4. Обработка ошибок в Kafka (Dead Letter Queue)
Если Consumer не может обработать сообщение (например, упала внешняя SMTP-система), он не должен бесконечно ретраить, блокируя партицию.

- Настроить стандартный Spring Kafka DefaultErrorHandler с DeadLetterPublishingRecoverer.
- После N неудачных попыток сообщение перемещается в топик <original-topic>.DLT (Dead Letter Topic) для ручного разбора или отложенного ретрая.

# 3.Распределенная трассировка (Observability)
Трассировка запросов через 6 микросервисов — ключевой навык в распределенных системах.

## 3.1. W3C Trace Context
Используются стандартные заголовки OpenTelemetry:

- traceparent — содержит trace-id и span-id.
- tracestate — дополнительные vendor-specific данные.

- Примечание: Spring Cloud Gateway и Spring Boot с подключенным micrometer-tracing-bridge-otel автоматически пробрасывают эти заголовки в синхронных вызовах. Ручная работа с ними в коде не требуется.

## 3.2. Трассировка в асинхронных вызовах (Kafka)
При публикации в Kafka (из Outbox) и потреблении, trace_id обязательно извлекается из SecurityContext (при публикации) и помещается в поле traceId в JSON событии (см. раздел 2.1).
Consumer-сервис должен восстановить контекст трассировки при обработке события, чтобы логи корректно связывались в Grafana Loki/Tempo.

## 3.3. Логирование (MDC)
Каждый сервис автоматически должен добавлять trace_id и span_id в MDC (Mapped Diagnostic Context) логгера (настраивается через logback-spring.xml и зависимости micrometer).
Пример формата лога: [%d{ISO8601}] [%thread] [traceId=%X{traceId}, spanId=%X{spanId}] [%level] %logger{36} - %msg%n 

# 4. Стандарты HTTP заголовков для API
## 4.1. Заголовки запросов (от клиента)

| Заголовок     | Обязательный | Описание                                                          |
|---------------|--------------|-------------------------------------------------------------------|
| Authorization |	Да* | 	JWT токен в формате Bearer {token} (*кроме публичных эндпоинтов) |
| Content-Type  |	Да | 	application/json                                                 |
| Accept        |	Да | 	application/json                                                 |

---

# 4.2. Рекомендуемые контекстные заголовки

| Заголовок  | Описание | Пример |
|------------|----------|--------|
| X-Language |	Язык интерфейса (для локализации ошибок) |	ru-RU, en-US |
| X-Timezone |	Часовой пояс клиента	Europe/Moscow |




## 4.3. Заголовки ответов
| Заголовок | Описание |
|-----------|----------|
|Content-Type |	application/json |
|X-RateLimit-Limit |	Лимит запросов (добавляется API Gateway) |
|X-RateLimit-Remaining |	Оставшееся количество запросов |
|X-RateLimit-Reset |	Время сброса лимита (Unix timestamp) |

# 5. Обработка ошибок

##  5.1. Стандартный формат REST ошибки
Все downstream-сервисы должны возвращать ошибки в едином формате (реализуется через @RestControllerAdvice).

```json
{
  "errorCode": "PRODUCT_NOT_FOUND",
  "message": "Товар с артикулом ABC123 не найден",
  "timestamp": "2026-07-13T10:00:00Z",
  "path": "/api/v1/catalog/products/ABC123",
  "details": null 
}
```

(Поле details заполняется списком ошибок валидации FieldViolation при коде 400 BAD_REQUEST).

## 5.2. Стратегия ошибок при синхронных вызовах (OpenFeign + Resilience4j)

Если Order Service вызывает Catalog Service, а тот возвращает 500 или таймаут:

- Срабатывает Circuit Breaker (открывает цепь после N ошибок).
- Вызывается Fallback метод в Order Service.
- Fallback должен вернуть бизнес-ожидаемый результат (например, выбросить кастомное ServiceUnavailableException с сообщением "Сервис каталога временно недоступен, попробуйте позже"), которое превратится в HTTP 503 для клиента.

