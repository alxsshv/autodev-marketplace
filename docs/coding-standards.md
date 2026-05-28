# Стандарты кодирования для AutoDev Marketplace

## Общие принципы

### Java
- Версия: Java 17
- Стиль кодирования: Oracle Java Code Conventions
- Использовать современные возможности Java (Stream API, Optional, Records, Sealed Classes)
- Предпочитать иммутабельность
- Избегать null, использовать Optional
- Использовать try-with-resources для управления ресурсами

### Spring Boot
- Использовать Spring Boot 3.x
- Предпочитать конфигурацию через аннотации и Java Config
- Использовать @ComponentScan, @ConfigurationProperties
- Использовать @Transactional на уровне сервисов
- Использовать @Cacheable, @CacheEvict для кэширования
- Использовать @Async для асинхронных операций

## Именование

### Пакеты
- Использовать полное доменное имя в обратном порядке: `com.autodev.<service-name>.<module>`
- Пример: `com.autodev.gateway.security`, `com.autodev.catalog.product`

### Классы и интерфейсы
- Использовать UpperCamelCase
- Суффиксы для типов:
  - `Controller` - для REST контроллеров
  - `Service` - для сервисных слоёв
  - `Repository` - для репозиториев
  - `Entity` - для JPA сущностей
  - `DTO` - для объектов передачи данных
  - `Mapper` - для мапперов
  - `Exception` - для исключений
  - `Config` - для конфигураций
  - `Filter` - для фильтров
  - `Listener` - для слушателей
  - `Aspect` - для аспектов

### Переменные и методы
- Использовать lowerCamelCase
- Названия должны быть описательными и отражать назначение
- Для булевых переменных использовать префиксы `is`, `has`, `can`

## Структура проекта

```
services/<service-name>/
├── src/main/java
│   └── com/autodev/<service-name>/
│       ├── controller/        # REST контроллеры
│       ├── service/           # Бизнес-логика
│       ├── repository/        # Работа с данными
│       ├── model/             # Сущности и DTO
│       ├── config/            # Конфигурации
│       ├── security/          # Безопасность
│       ├── exception/         # Обработка исключений
│       ├── aspect/            # Аспекты
│       ├── filter/            # Фильтры
│       ├── listener/          # Слушатели событий
│       └── <service-name>Application.java  # Главный класс
├── src/main/resources
│   ├── application.yml        # Основная конфигурация
│   ├── bootstrap.yml          # Конфигурация для Spring Cloud
│   ├── db/migration/          # Миграции базы данных (Liquibase)
│   └── static/                # Статические ресурсы
└── src/test/java
    └── com/autodev/<service-name>/
        ├── controller/        # Тесты контроллеров
        ├── service/           # Тесты сервисов
        ├── repository/        # Тесты репозиториев
        └── integration/       # Интеграционные тесты
```

## Комментарии и документация

- Использовать Javadoc для публичных классов и методов
- Комментарии должны объяснять "почему", а не "что"
- Избегать избыточных комментариев
- Использовать TODO, FIXME для пометки задач

```java
/**
 * Сервис для управления пользователями.
 * Отвечает за регистрацию, аутентификацию и профили пользователей.
 * Использует Keycloak для централизованной аутентификации.
 */
@Service
public class UserService {
    
    /**
     * Регистрирует нового пользователя в системе.
     * Создаёт запись в базе данных и пользователя в Keycloak.
     * 
     * @param userRegistrationDTO данные для регистрации
     * @return зарегистрированный пользователь
     * @throws UserAlreadyExistsException если пользователь с таким email уже существует
     */
    public User registerUser(UserRegistrationDTO userRegistrationDTO) {
        // Проверка существования пользователя
        if (userRepository.findByEmail(userRegistrationDTO.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("User with email " + userRegistrationDTO.getEmail() + " already exists");
        }
        
        // Создание сущности пользователя
        User user = userMapper.toEntity(userRegistrationDTO);
        
        // Сохранение в базе данных
        User savedUser = userRepository.save(user);
        
        // Создание пользователя в Keycloak
        keycloakService.createUser(savedUser);
        
        return savedUser;
    }
}
```

## Конфигурация

### application.yml
- Использовать YAML для конфигурации
- Группировать настройки по функциональности
- Использовать профили для разных окружений
- Выносить чувствительные данные в переменные окружения

```yaml
server:
  port: 8080

spring:
  application:
    name: user-service
  
  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/autodev_users}
    username: ${DB_USERNAME:autodev}
    password: ${DB_PASSWORD:password}
    driver-class-name: org.postgresql.Driver

  jpa:
    hibernate:
      ddl-auto: none
    show-sql: false
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.PostgreSQLDialect

  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: ${KEYCLOAK_ISSUER_URI:http://localhost:8081/realms/autodev}

  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}

  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
    consumer:
      group-id: user-service-group

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true

app:
  jwt:
    secret: ${JWT_SECRET:mySecretKey}
    expiration: ${JWT_EXPIRATION:86400}

logging:
  level:
    com.autodev: DEBUG
    org.springframework: INFO
    org.hibernate: WARN
```

## Тестирование

### Юнит-тесты
- Использовать JUnit 5
- Использовать Mockito для мокирования зависимостей
- Покрывать бизнес-логику
- Использовать AssertJ для ассертов

### Интеграционные тесты
- Использовать @SpringBootTest
- Использовать Testcontainers для запуска зависимостей (PostgreSQL, Redis, Kafka)
- Покрывать интеграцию компонентов

### Тесты API
- Использовать @WebMvcTest для тестирования контроллеров
- Использовать MockMvc
- Проверять HTTP статусы, заголовки, тело ответа

### Тесты безопасности
- Использовать @WebMvcTest с Spring Security
- Проверять доступ к защищённым эндпоинтам
- Проверять права доступа для разных ролей

## Git и CI/CD

### Ветвление
- Использовать Git Flow
- Основные ветки: `main`, `develop`
- Фича-ветки: `feature/<feature-name>`
- Релиз-ветки: `release/<version>`
- Хотфиксы: `hotfix/<issue>`

### Коммиты
- Использовать Conventional Commits
- Формат: `<type>(<scope>): <description>`
- Примеры типов: feat, fix, docs, style, refactor, perf, test, build, ci, chore
- Пример: `feat(user): add user registration endpoint`

### Pull Request
- Описание PR должно содержать:
  - Цель изменений
  - Описание реализации
  - Ссылки на задачи
  - Инструкции по тестированию
- Минимум один ревьюер
- Прохождение CI

## Docker

### Dockerfile
- Использовать multi-stage сборку
- Использовать официальные образы
- Минимизировать размер образа
- Использовать non-root пользователя

```dockerfile
# Сборка
FROM gradle:8-jdk17 AS builder
WORKDIR /app
COPY build.gradle.kts gradle.properties ./
COPY gradle gradle
RUN gradle dependencies --no-daemon

COPY src src
RUN gradle build -x test --no-daemon

# Запуск
FROM eclipse-temurin:17-jre-alpine
RUN addgroup -g 1001 -S appuser && \
    adduser -u 1001 -S appuser -G appuser
USER appuser

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

CMD ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
```

## Безопасность

- Использовать HTTPS
- Валидировать входные данные
- Использовать OWASP рекомендации
- Регулярно обновлять зависимости
- Использовать Snyk или Dependabot для анализа уязвимостей
- Не коммитить секреты в репозиторий
- Использовать переменные окружения для чувствительных данных