# Учебный план для Java backend-разработчика: AutoDev Marketplace

## 1. Введение

### Цели обучения
В ходе прохождения этого учебного плана разработчик освоит современные практики разработки микросервисных приложений на Spring Boot 3 и Java 17. Основные цели:

- Освоить микросервисную архитектуру и её ключевые паттерны
- Научиться проектировать и реализовывать REST API
- Освоить работу с Spring Security и Keycloak для аутентификации и авторизации
- Научиться работать с Spring Data JPA и Liquibase для управления данными
- Освоить асинхронное взаимодействие через Apache Kafka
- Научиться использовать Docker и Docker Compose для контейнеризации
- Освоить практики тестирования (юнит, интеграционные, E2E тесты)
- Научиться настраивать CI/CD pipeline
- Освоить мониторинг и логирование распределённых систем

### Ожидаемые результаты
После завершения плана разработчик сможет:

- Самостоятельно проектировать и реализовывать микросервисы
- Находить и устранять проблемы в распределённых системах
- Настраивать и поддерживать CI/CD pipeline
- Реализовывать безопасные и производительные REST API
- Работать с различными типами баз данных и кэшами
- Реализовывать сложную бизнес-логику
- Писать качественные тесты с покрытием не менее 80%
- Работать в команде по методологии Scrum

### Продолжительность обучения
13 недель (1 сервис в неделю)

### Формат взаимодействия
- **Груминг**: Обсуждение задачи, уточнение требований
- **Планирование**: Разбиение задачи на подзадачи, оценка трудозатрат
- **Реализация**: Разработчик реализует код, ИИ-ментор предоставляет обратную связь
- **Код-ревью**: Проверка кода на соответствие стандартам, выявление ошибок
- **Демонстрация**: Демонстрация реализованной функциональности
- **Ретроспектива**: Обсуждение успехов и зон роста

## 2. Подготовительный этап

### Установка и настройка окружения

1. Установите необходимые инструменты:
   - Java 17 (OpenJDK)
   - Gradle 8.x
   - Docker 20.x
   - Docker Compose 2.x
   - Git 2.30+
   - IntelliJ IDEA 2023.x

2. Настройте локальное окружение согласно инструкции в `docs/dev-environment.md`

3. Клонируйте репозиторий:
```bash
git clone http://192.168.0.162/autodev-marketplace.git
cd autodev-marketplace
```

4. Запустите инфраструктуру:
```bash
docker-compose up -d
```

### Настройка CI/CD pipeline

1. Создайте `.gitlab-ci.yml` в корне проекта
2. Настройте pipeline для:
   - Сборки и тестирования каждого сервиса
   - Сборки Docker образов
   - Публикации образов в registry
   - Развёртывания на staging окружении

```yaml
stages:
  - build
  - test
  - package
  - deploy

variables:
  DOCKER_REGISTRY: 192.168.0.162:5000
  IMAGE_NAME: $DOCKER_REGISTRY/$CI_PROJECT_NAME:$CI_COMMIT_REF_SLUG

.cache_template: &cache
  cache:
    key: $CI_PROJECT_NAME
    paths:
      - .gradle

build:
  stage: build
  image: gradle:8-jdk17
  <<: *cache
  script:
    - ./gradlew build -x test
  artifacts:
    paths:
      - services/*/build/libs/*.jar

test:
  stage: test
  image: gradle:8-jdk17
  <<: *cache
  services:
    - postgres:14
    - redis:7
    - confluentinc/cp-kafka:7.4.0
  script:
    - ./gradlew test
  coverage: '/TOTAL.+ ([0-9]{1,3}\.?[0-9]*)%/'

package:
  stage: package
  image: docker:20.10.16
  services:
    - docker:20.10.16-dind
  script:
    - docker login -u $CI_REGISTRY_USER -p $CI_REGISTRY_PASSWORD $DOCKER_REGISTRY
    - docker build -t $IMAGE_NAME services/api-gateway/.
    - docker push $IMAGE_NAME

deploy_staging:
  stage: deploy
  image: docker:20.10.16
  services:
    - docker:20.10.16-dind
  script:
    - docker login -u $CI_REGISTRY_USER -p $CI_REGISTRY_PASSWORD $DOCKER_REGISTRY
    - docker-compose -f docker-compose.staging.yml pull
    - docker-compose -f docker-compose.staging.yml up -d
  only:
    - develop

```

### Правила работы с ветками и Git Flow

1. Основные ветки:
   - `main` - production код
   - `develop` - основная ветка разработки

2. Ветки для задач:
   - `feature/<feature-name>` - для реализации новых функций
   - `bugfix/<issue>` - для исправления багов
   - `hotfix/<issue>` - для срочных исправлений в production

3. Правила коммитов:
   - Использовать Conventional Commits
   - Формат: `<type>(<scope>): <description>`
   - Примеры: `feat(auth): add user registration`, `fix(user): resolve NPE in profile update`

4. Pull Request:
   - Описание должно содержать цель изменений, описание реализации и инструкции по тестированию
   - Минимум один ревьюер
   - Прохождение CI обязательно

## 3. Укрупнённый план

Порядок разработки микросервисов:

1. **Service Discovery** - централизованное обнаружение сервисов (Consul)
2. **Auth Service** - централизованная аутентификация
3. **User Service** - управление пользователями
4. **Catalog Service** - каталог товаров
5. **Pricing & Inventory Service** - цены и наличие
6. **Search Service** - поиск по VIN и артикулам
7. **API Gateway** - маршрутизация запросов
8. **Order Service** - оформление заказов
9. **Notification Service** - уведомления
10. **Review Service** - отзывы
11. **Recommendation Service** - рекомендации
12. **Admin Service** - администрирование
13. **Analytics & Reporting Service** - аналитика

## 4. Детализированный план по микросервисам

### Service Discovery

**Назначение:** Централизованное обнаружение сервисов для динамического обнаружения и балансировки нагрузки.

**Технологии:**
- Consul
- Spring Cloud Consul
- Docker Compose

**Задачи:**
1. Добавить Consul в `docker-compose.yaml`
2. Создать конфигурацию Consul
3. Настроить Spring Cloud Consul в сервисах
4. Реализовать health check для сервисов
5. Настроить service registry и discovery

**Критерии приёмки:**
- Все сервисы регистрируются в Consul при старте
- API Gateway может обнаруживать сервисы через Consul
- Consul UI доступен по http://localhost:8500
- Health check проходит успешно для всех сервисов

**Пример кода:**

`docker-compose.yaml`:
```yaml
  consul:
    image: consul:1.15
    container_name: consul
    ports:
      - "8500:8500"
      - "8600:8600/tcp"
    command: "agent -server -bootstrap -ui -client=0.0.0.0"
```

`build.gradle.kts`:
```kotlin
dependencies {
    implementation("org.springframework.cloud:spring-cloud-starter-consul-discovery")
}
```

`application.yml`:
```yaml
spring:
  cloud:
    consul:
      host: localhost
      port: 8500
      discovery:
        service-name: ${spring.application.name}
        health-check-path: /actuator/health
        health-check-interval: 15s
```

### Auth Service

**Назначение:** Централизованная аутентификация и авторизация через Keycloak.

**Технологии:**
- Keycloak
- Spring Security
- Spring Boot 3
- JWT
- OAuth2/OpenID Connect

**Задачи:**
1. Создать структуру проекта `services/auth-service`
2. Настроить зависимость от Keycloak
3. Реализовать регистрацию пользователей
4. Реализовать аутентификацию через OAuth2
5. Реализовать генерацию JWT токенов
6. Настроить интеграцию с User Service
7. Реализовать восстановление пароля

**Критерии приёмки:**
- Пользователь может зарегистрироваться через REST API
- Пользователь может аутентифицироваться и получить JWT токен
- JWT токен содержит информацию о пользователе и ролях
- Доступ к защищённым эндпоинтам требует валидный JWT токен
- Интеграция с Keycloak работает корректно

**Пример кода:**

`SecurityConfig.java`:
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/auth/**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            );
        return http.build();
    }

    private Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new KeycloakRoleConverter());
        return converter;
    }
}
```

### User Service

**Назначение:** Управление пользователями, профилями и рейтингами.

**Технологии:**
- Spring Boot 3
- Spring Data JPA
- PostgreSQL
- Redis (кэширование)
- Kafka (события)
- Liquibase

**Задачи:**
1. Создать структуру проекта `services/user-service`
2. Настроить подключение к PostgreSQL
3. Создать сущности User, Profile, Rating
4. Реализовать репозитории
5. Реализовать сервисы бизнес-логики
6. Настроить кэширование профилей в Redis
7. Реализовать отправку событий в Kafka
8. Создать REST контроллеры

**Критерии приёмки:**
- CRUD операции для пользователей работают корректно
- Профили пользователей кэшируются в Redis
- События о создании/обновлении пользователей отправляются в Kafka
- Все API покрыты тестами (не менее 80%)
- Миграции базы данных управляются через Liquibase

**Пример кода:**

`User.java`:
```java
@Entity
@Table(name = "users")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true)
    private String email;
    
    private String firstName;
    private String lastName;
    
    @Enumerated(EnumType.STRING)
    private UserRole role;
    
    private Boolean enabled;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
```

### Catalog Service

**Назначение:** Управление каталогом товаров, категориями и совместимостью.

**Технологии:**
- Spring Boot 3
- Spring Data JPA
- PostgreSQL
- MinIO (хранение изображений)
- Kafka (события)
- Liquibase

**Задачи:**
1. Создать структуру проекта `services/catalog-service`
2. Настроить подключение к PostgreSQL
3. Создать сущности Product, Category, Compatibility
4. Реализовать репозитории
5. Реализовать сервисы бизнес-логики
6. Настроить интеграцию с MinIO для хранения изображений
7. Реализовать отправку событий в Kafka
8. Создать REST контроллеры

**Критерии приёмки:**
- CRUD операции для продуктов и категорий работают корректно
- Изображения продуктов хранятся в MinIO
- События о создании/обновлении продуктов отправляются в Kafka
- Все API покрыты тестами (не менее 80%)
- Миграции базы данных управляются через Liquibase

**Пример кода:**

`Product.java`:
```java
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true)
    private String sku;
    
    private String name;
    private String description;
    
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
    
    private BigDecimal price;
    
    @ElementCollection
    private List<String> images = new ArrayList<>();
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
```

### Pricing & Inventory Service

**Назначение:** Управление ценами, наличием и импортом прайс-листов.

**Технологии:**
- Spring Boot 3
- Spring Data JPA
- PostgreSQL
- Redis (кэширование актуальных цен)
- Kafka (события)
- Liquibase
- Apache POI (XLSX)
- OpenCSV (CSV)

**Задачи:**
1. Создать структуру проекта `services/pricing-inventory-service`
2. Настроить подключение к PostgreSQL
3. Создать сущности Price, Inventory, PriceList
4. Реализовать репозитории
5. Реализовать сервисы бизнес-логики
6. Настроить кэширование актуальных цен в Redis
7. Реализовать импорт прайс-листов (CSV, XLSX, XML, YML)
8. Реализовать отправку событий в Kafka
9. Создать REST контроллеры

**Критерии приёмки:**
- CRUD операции для цен и наличия работают корректно
- Актуальные цены кэшируются в Redis
- Импорт прайс-листов различных форматов работает корректно
- События об изменении цен отправляются в Kafka
- Все API покрыты тестами (не менее 80%)
- Миграции базы данных управляются через Liquibase

**Пример кода:**

`PriceService.java`:
```java
@Service
@RequiredArgsConstructor
public class PriceService {
    
    private final PriceRepository