# Третья задача: Создание Auth Service для централизованной аутентификации

## Цель
Создать микросервис Auth Service для централизованной аутентификации и авторизации пользователей через интеграцию с Keycloak, настроить Service Discovery и реализовать базовые операции по управлению пользователями.

## Контекст

Ты успешно выполнил задачу №2 по настройке Service Discovery с Consul. Теперь пришло время создать первый бизнес-сервис - Auth Service.

**Почему Auth Service?**
- Это фундаментальный сервис, который обеспечивают безопасность всей системы
- Остальные сервисы будут зависеть от аутентификации через Auth Service
- Понимание интеграции с Keycloak критично для проекта
- Auth Service использует множество технологий из стека: JPA, Kafka, Redis, Liquibase

**Что ты освоишь:**
- Создание микросервиса с нуля
- Интеграция с Keycloak через OAuth2/OpenID Connect
- Настройка Service Discovery в Consul
- Работа с PostgreSQL через Spring Data JPA
- Асинхронная коммуникация через Apache Kafka
- Кэширование с Redis
- Управление миграциями базы данных через Liquibase
- Создание REST API с валидацией

## Задача

### Часть 1: Подготовка проекта

1. **Создай структуру проекта**
   - Создай директорию `services/auth-service`
   - Создай основные каталоги: `src/main/java/com/autodev/auth/`, `src/main/resources/`, `src/test/java/com/autodev/auth/`

2. **Создай `build.gradle.kts`**
   - Зависимости: Spring Boot, Spring Cloud Consul, Spring Security, Liquibase, PostgreSQL, Kafka, Redis, MapStruct, Lombok
   - Плагины: Spring Boot, Dependency Management, Liquibase
   - Версии библиотек из `gradle.properties`

3. **Создай основной класс приложения `AuthApplication.java`**
   - Аннотации: `@SpringBootApplication`, `@EnableDiscoveryClient`, `@EnableCaching`, `@EnableKafka`

4. **Настрой `application.yml`**
   - Серверный порт: 8080
   - Имя сервиса: auth-service
   - Конфигурация базы данных PostgreSQL
   - Конфигурация Redis
   - Конфигурация Kafka
   - Интеграция с Consul для Service Discovery
   - Настройки безопасности Keycloak (issuer-uri)

### Часть 2: Настройка базы данных

5. **Создай миграции Liquibase**
   - `db.changelog-master.yaml` - главный файл
   - `001-create-users-table.yaml` - таблица users с полями:
     - id (BIGINT, PK)
     - keycloak_user_id (VARCHAR(255), UNIQUE)
     - email (VARCHAR(255), UNIQUE)
     - first_name, last_name, phone (VARCHAR)
     - role (VARCHAR(50))
     - enabled (BOOLEAN)
     - created_at, updated_at (TIMESTAMP)

6. **Создай JPA сущность `User.java`**
   - Аннотации: `@Entity`, `@Table`, `@Data`, `@Builder`
   - Поля с соответствующими аннотациями `@Column`, `@Enumerated`
   - Используй `@CreatedDate` и `@LastModifiedDate` для автоматического управления временем

7. **Создай репозиторий `UserRepository.java`**
   - Наследуйся от `JpaRepository<User, Long>`
   - Добавь методы: `findByKeycloakUserId()`, `findByEmail()`

### Часть 3: Реализация бизнес-логики

8. **Создай DTO классы**
   - `UserDto.java` - для обмена данными с клиентами
   - Используй Lombok аннотации для сокращения boilerplate

9. **Создай сервис `UserService.java`**
   - Методы:
     - `findByKeycloakUserId()` - кэшируемый метод
     - `findByEmail()` - кэшируемый метод
     - `createUser()` - транзакционный метод
     - `updateUser()` - транзакционный метод
     - `getAllUsers()` - метод получения всех пользователей
   - Реализуй конвертацию между User и UserDto
   - Используй `@Cacheable` для методов поиска
   - Добавь логирование через SLF4J

10. **Создай REST контроллер `UserController.java`**
    - Base path: `/api/v1/users`
    - Эндпоинты:
      - `GET /{id}` - получение пользователя по ID
      - `GET /email/{email}` - получение пользователя по email
      - `GET /` - получение всех пользователей
      - `POST /` - создание нового пользователя
    - Используй `@Valid` для валидации входных данных
    - Обрабатывай ошибки через `ResponseEntity`

### Часть 4: Интеграция с Keycloak

11. **Настрой интеграцию с Keycloak**
    - В `application.yml` укажи `issuer-uri` для Keycloak
    - Spring Security автоматически настроит resource server
    - Настрой `SecurityConfig.java` для JWT валидации:
      ```java
      @Configuration
      @EnableWebSecurity
      public class SecurityConfig {
          @Bean
          public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
              http
                  .authorizeHttpRequests(authz -> authz
                      .requestMatchers("/api/v1/users/**").hasRole("ADMIN")
                      .anyRequest().permitAll()
                  )
                  .oauth2ResourceServer(oauth2 -> oauth2.jwt());
              return http.build();
          }
      }
      ```

### Часть 5: Интеграция с Kafka

12. **СоздайProducer и Consumer для событий пользователей**
    - События: `UserCreatedEvent`, `UserUpdatedEvent`, `UserDeletedEvent`
    - Producer отправляет события в Kafka при изменении пользователей
    - Consumer слушает события и может выполнять дополнительную логику

### Часть 6: Docker и инфраструктура

13. **Создай `Dockerfile`**
    ```dockerfile
    FROM openjdk:17-slim
    WORKDIR /app
    COPY build/libs/*.jar app.jar
    EXPOSE 8080
    ENTRYPOINT ["java", "-jar", "app.jar"]
    ```

14. **Обнови `docker-compose.yaml`**
    - Добавь сервис `auth-service`
    - Подключи к `app_network`
    - Зависит от `consul`, `auth-database`, `redis`, `kafka`
    - Порт на хосте: 8083 (проброс: 8080:8080)

15. **Создай `auth-database` в PostgreSQL**
    - Добавь в docker-compose.yaml PostgreSQL для Auth Service
    - Используй тот же образ, что и для Keycloak
    - Порт на хосте: 5436

### Часть 7: Тестирование

16. **Напиши юнит-тесты**
    - Тесты для `UserService` с мокированием репозитория
    - Тесты для `UserController` с MockMvc
    - Покрытие должно быть не менее 80%

17. **Напиши интеграционные тесты**
    - Тесты с реальной базой данных через Testcontainers
    - Тесты с реальным Kafka через Testcontainers
    - Проверь регистрацию сервиса в Consul

## Критерии приёмки

- [ ] проект создан по стандартной структуре Spring Boot
- [ ] `build.gradle.kts` содержит все необходимые зависимости
- [ ] `application.yml` настроен для работы в локальном и docker режимах
- [ ] Liquibase миграции созданы и применяются при старте
- [ ] JPA сущность `User` корректно отображается на таблицу `users`
- [ ] `UserRepository` реализует все необходимые методы
- [ ] `UserService` реализует бизнес-логику с кэшированием
- [ ] `UserController` реализует REST API с валидацией
- [ ] Интеграция с Keycloak настроена через OAuth2
- [ ] Сервис регистрируется в Consul при старте
- [ ] Dockerfile создан и образ собирается без ошибок
- [ ] `docker-compose.yaml` обновлен с сервисом auth-service
- [ ] Юнит-тесты проходят (покрытие не менее 80%)
- [ ] Интеграционные тесты проходят
- [ ] Сервис запускается без ошибок и доступен на порту 8083

## Дополнительные ресурсы

### Документация
- [Spring Boot Security OAuth2 Resource Server](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/index.html)
- [Spring Cloud Consul Discovery](https://docs.spring.io/spring-cloud-consul/docs/current/reference/html/)
- [Liquibase with Spring Boot](https://www.baeldung.com/liquibase-refactor-schema-of-java-app)
- [Spring Data JPA](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
- [Spring Kafka](https://docs.spring.io/spring-kafka/reference/index.html)

### Туториалы
- [Spring Boot + Keycloak Integration](https://www.baeldung.com/spring-boot-keycloak)
- [Spring Cloud Consul Tutorial](https://www.baeldung.com/spring-cloud-consul)
- [Liquibase Database Migrations](https://www.youtube.com/watch?v=Kf5t3VXJt3w)

### Вопросы для самопроверки

1. Какую роль играет `@EnableDiscoveryClient` в Spring Cloud?
2. Что такое OAuth2 и как он используется в Keycloak?
3. Зачем нужно кэширование в `UserService`?
4. Как Spring Boot автоматически настраивает Spring Data JPA?
5. Что такое контейнеризация и зачем она нужна в микросервисной архитектуре?
6. Как работает `@Cacheable` в Spring?
7. Что такое Event-driven architecture и как она реализована через Kafka?

## Подсказки

### Проблема: Ошибки компиляции зависимостей
**Решение:** Проверь версии библиотек в `gradle.properties` и `build.gradle.kts`

### Проблема: Service не регистрируется в Consul
**Решение:** Проверь сетевую доступность Consul из контейнера, убедись в правильности `consul.host` и `consul.port`

### Проблема: Ошибки подключения к базе данных
**Решение:** Убедись, что `auth-database` запущен и доступен по `auth-database:5432`, проверь учетные данные в `application.yml`

### Проблема: Ошибки миграций Liquibase
**Решение:** Проверь синтаксис YAML файлов, убедись, что файл `db.changelog-master.yaml` правильно подключен

## Следующий шаг

После успешного выполнения этой задачи приступи к реализации User Service для управления профилями пользователей, настройками магазина и рейтингами.

## Важные моменты

- **Не торопись:** Каждый шаг требует внимательного выполнения
- **Тестируй:** После каждого большого блока запускай тесты
- **Документируй:** Добавляй JavaDoc ко всем классам и методам
- **Следуй стандартам:** Используй конвенции кодирования из `docs/coding-standards.md`
- **Проверяй:** После завершения запусти весь проект через `docker-compose up` и проверь работоспособность
