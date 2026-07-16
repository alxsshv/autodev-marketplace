Задача 003.5: Настроить Observability (Трассировка и Метрики) в API Gateway

Статус: Нужно реализовать

Ветка: feature/003-5-api-gateway-metrics-and-traces

Описание
Текущее состояние: Инфраструктура мониторинга (Prometheus, Grafana, Loki, Tempo, Alloy) уже развёрнута в docker-compose. Однако в самом api-gateway не настроена генерация telemetry-данных.

Сервис должен автоматически собирать метрики HTTP-запросов (латентность, количество запросов, статусы ответов) и обеспечивать сквозную распределённую трассировку (Distributed Tracing), передавая TraceId в downstream-сервисы.

Важно: В рамках этой задачи категорически запрещается писать кастомные GlobalFilter для чтения и логирования тел запросов/ответов (Request/Response Body). В реактивном WebFlux это создает буферизацию стримов, убивает производительность, засоряет хранилище логов и может привести к утечке PII (паролей, токенов). Мы используем только встроенные механизмы Spring Boot 3 Micrometer.

Критерии выполнения
1. Зависимости
   Убедиться, что в pom.xml присутствуют стартеры (обычно тянутся через spring-cloud-starter-gateway, но нужно проверить):
   micrometer-registry-prometheus
   micrometer-tracing-bridge-otel (или micrometer-tracing-bridge-brave, в зависимости от выбранной библиотеки трассировки в проекте)
2. Конфигурация Actuator и Метрик
   В application.yml включен доступ к нужным эндпоинтам Actuator (как минимум health, info, prometheus).
   Приложение успешно отдает метрики по пути /actuator/prometheus (можно проверить через curl или браузер).
3. Настройка распределённой трассировки
   В application.yml включена трассировка (management.tracing.enabled=true).
   Для учебного стенда (MVP) установлен sampling.probability: 1.0 (логируем 100% запросов для наглядности).
   Настроен тип пропагации трассировки: propagation.type: w3c (чтобы Gateway передавал заголовок traceparent в catalog-service, platform-service и т.д.).
4. Связка Логирования и Трассировки (Loki + Tempo)
   Настроен logging.pattern.level таким образом, чтобы в каждый лог автоматически подставлялись traceId и spanId (пример паттерна: %5p [${spring.application.name:},%X{traceId:-},%X{spanId:-}]).
   Опционально: для дебага вызовов к downstream-сервисам можно включить spring.cloud.gateway.httpclient.wiretap: true (логирует только заголовки и статусы, без тел).
5. Проверка (Verification)
   При выполнении запроса к защищенному эндпоинту через Gateway (например, получение профиля), в консоли (логах) Gateway появился traceId.
   Этот traceId пробросился дальше и отображается в логах downstream-сервиса (если он уже настроен; если нет — проверить наличие заголовка traceparent в запросе, который пришел к downstream-сервису).
   Архитектурные ссылки
   Документация Spring Boot 3: Observability, Micrometer Tracing.
   Схема инфраструктуры: docker-compose.yaml (секция с Prometheus/Tempo/Loki).
   Приоритет
   Средний - не блокирует разработку бизнес-логики, но критически важен для отладки межсервисного взаимодействия при развитии downstream-сервисов.

Создано: 2026-07-13
Предыдущая задача: 003-3-api-gateway-cors-filter (или 003-4, если нужно залогировать закрытие)
Следующая задача: 004-platform-service-entities-dto