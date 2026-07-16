Задача №1: Базовый каркас platform-service
Ветка: feature/platform-service/bootstrap

Приоритет: Высокий
Оценка: ~2–3 часа
Зависимости: нет

Контекст
api-gateway завершён. Наступает очередь platform-service — первого downstream-сервиса. Перед реализацией бизнес-логики (профили, отзывы) нужно создать работающий каркас: проект компилируется, подключается к инфраструктуре, поднимается как бин в Consul, валидирует JWT через Keycloak. Без этого невозможно писать ни одну бизнес-задачу.

Важное отличие от api-gateway: Gateway построен на WebFlux (reactive). platform-service — стандартный Spring MVC (servlet), поэтому конфигурация безопасности и подход к написанию кода будут другими. Не копируй паттерны из Gateway blindly.

Что сделать
Настроить platform-service как минимально жизнеспособный Spring Boot проект, который:

Собирается через Gradle (Kotlin DSL) без ошибок
Подключается к PostgreSQL, Redis, Kafka, Consul
Валидирует JWT через Keycloak как OAuth2 Resource Server
Регистрируется в Consul и доступен через Service Discovery
Предоставляет health-эндпоинт для проверки
Содержит инфраструктурный код, который будет использоваться во всех последующих задачах
Требования
1. build.gradle.kts
   Зависимости с версиями из system-overview.md:

Группа
Артефакт
Назначение
org.springframework.boot	spring-boot-starter-web	Servlet-контейнер
org.springframework.boot	spring-boot-starter-data-jpa	Доступ к БД
org.springframework.boot	spring-boot-starter-security	Безопасность
org.springframework.boot	spring-boot-starter-oauth2-resource-server	JWT валидация
org.springframework.boot	spring-boot-starter-validation	Bean Validation
org.springframework.boot	spring-boot-starter-data-redis	Кэширование
org.springframework.cloud	spring-cloud-starter-consul-discovery	Service Discovery
org.springframework.kafka	spring-kafka	Продюсер/консьюмер событий
io.github.resilience4j	resilience4j-spring-boot3	Circuit Breaker для Keycloak
org.liquibase	liquibase-core	Миграции БД
org.mapstruct	mapstruct	Маппинг DTO
org.projectlombok	lombok	Boilerplate
org.postgresql	postgresql	JDBC драйвер
org.mapstruct	mapstruct-processor	Annotation processor
org.projectlombok	lombok-mapstruct-binding	Совместимость Lombok + MapStruct
org.springframework.boot	spring-boot-starter-test	Тесты
org.testcontainers	junit-jupiter, postgresql, kafka	Интеграционные тесты
org.wiremock	wiremock-standalone	Мокирование Keycloak в тестах

Версии брать из system-overview.md (раздел 8). Плагины: spring-boot, java (17), kotlin-dsl уже должны быть настроены (как в остальных сервисах проекта).

2. application.yml
   Конфигурация через переменные окружения (с дефолтами для локального запуска):

yaml

# Сервер
jpa:
hibernate:
ddl-auto: validate
open-in-view: false
properties:
hibernate:
dialect: org.hibernate.dialect.PostgreSQLDialect

# Liquibase
liquibase:
change-log: classpath:db/changelog/master.yaml
enabled: true

# Redis
data:
redis:
host: ${REDIS_HOST:localhost}
port: ${REDIS_PORT:6379}

# Kafka
kafka:
bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
producer:
key-serializer: org.apache.kafka.common.serialization.StringSerializer
value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
consumer:
group-id: ${KAFKA_GROUP_ID:platform-service}
auto-offset-reset: earliest
key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
properties:
spring.json.trusted.packages: "*"

# Keycloak (OAuth2 Resource Server)
security:
oauth2:
resourceserver:
jwt:
issuer-uri: ${KEYCLOAK_ISSUER_URI:http://localhost:8090/realms/autodev}
jwk-set-uri: ${KEYCLOAK_JWK_SET_URI:http://localhost:8090/realms/autodev/protocol/openid-connect/certs}

# Consul
spring.cloud:
consul:
host: ${CONSUL_HOST:localhost}
port: ${CONSUL_PORT:8500}
discovery:
health-check-path: /actuator/health
health-check-interval: 10s
prefer-ip-address: true
register: true

# Actuator
management:
endpoints:
web:
exposure:
include: health,info,prometheus
endpoint:
health:
show-details: when-authorized

# Resilience4j
resilience4j:
circuitbreaker:
instances:
keycloak:
failure-rate-threshold: 50
slow-call-rate-threshold: 80
slow-call-duration-threshold: 2s
wait-duration-in-open-state: 30s
sliding-window-size: 10
minimum-number-of-calls: 5
3. Security
   SecurityConfig — @Configuration + @EnableWebSecurity
   Настройка SecurityFilterChain: все эндпоинты требуют аутентификации, кроме /actuator/health
   JwtAuthenticationConverter — извлечение ролей из realm_access.roles claims JWT и маппинг в ROLE_ префикс (Spring Security convention)
   SecurityUtils — утилитный класс с методами:
   getCurrentUserId() — извлечение sub claim из Authentication
   getCurrentUserEmail() — извлечение email claim
   getCurrentUserRoles() — извлечение списка ролей
4. Обработка ошибок
   @ControllerAdvice — GlobalExceptionHandler
   ErrorResponse record (timestamp, status, message, path)
   FieldViolation record (field, message)
   Обработчики минимум для: MethodArgumentNotValidException, AccessDeniedException, HttpMessageNotReadableException, универсальный Exception
5. Health-проверка
   Стандартный /actuator/health от Spring Boot Actuator — достаточно для этой задачи
   Кастомные health-индикаторы не нужны на этом этапе
6. Миграции БД
   Подключить существующий db/changelog/master.yaml (уже есть в проекте для user-profiles и reviews)
   Убедиться что Liquibase корректно накатывает миграции при старте
   Если в master.yaml есть проблемы — исправить, но не менять SQL-схему
   Структура пакетов
   text

com.autodev.platformservice
├── PlatformServiceApplication.java
├── config
│   ├── SecurityConfig.java
│   └── KafkaConfig.java
├── security
│   └── SecurityUtils.java
├── exception
│   ├── GlobalExceptionHandler.java
│   └── ErrorResponse.java
└── db
└── changelog/
└── master.yaml  (уже существует)
Не создавай пакеты controller, service, repository, dto, mapper — они появятся в следующих задачах.

Критерии приёмки
Сборка: ./gradlew :services:platform-service:build завершается без ошибок
Lint: нет warnings от MapStruct/Lombok (проверить что annotation processors работают корректно вместе)
Запуск: сервис стартует без ошибок при поднятой инфраструктуре (docker compose up)
Consul: сервис виден в UI Consul (http://localhost:8500/ui) с именем platform-service и статусом passing
Health: GET http://localhost:8081/actuator/health возвращает 200 с status: UP
JWT защита: GET http://localhost:8081/actuator/health — 200 (открыт), любой другой путь без токена — 401
JWT валидация: запрос с валидным JWT (получить через Keycloak) — 404 (нет контроллеров, но Security пропускает), с невалидным токеном — 401
Liquibase: при первом старте в логах видно Liquibase: Successfully acquired change log lock и накат миграций без ошибок
Нет лишнего кода: отсутствуют пакеты и классы, которые не относятся к каркасу (нет заглушек под будущие сервисы, нет пустых контроллеров)
Как проверить JWT валидацию (для приёмки)
Получить токен:
bash

curl -X POST http://localhost:8090/realms/autodev/protocol/openid-connect/token \
-d "client_id=autodev-client" \
-d "grant_type=password" \
-d "username=test@example.com" \
-d "password=testpass"
Без токена → 401:
bash

curl -v http://localhost:8081/api/v1/profiles/me
С токеном → 404 (контроллер ещё не создан, но Security пропустил):
bash

curl -v -H "Authorization: Bearer <token>" http://localhost:8081/api/v1/profiles/me
Ссылки на референсы
Конфигурация Gateway: services/api-gateway/ — только для понимания подхода, не копировать (WebFlux vs MVC)
Схема БД: services/platform-service/src/main/resources/db/changelog/
Версии зависимостей: docs/architecture/system-overview.md → раздел 8
Стек технологий: PROJECT_OVERVIEW.md → раздел "Технологический стек"