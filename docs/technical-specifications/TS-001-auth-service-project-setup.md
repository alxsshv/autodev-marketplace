# Техническое задание TS-001: Подготовка проекта Auth Service

**Версия документа:** 1.0  
**Дата создания:** 2026-06-02  
**Автор:** Аналитик  
**Статус:** Готово к реализации  
**Приоритет:** Высокий  
**Оценка трудоемкости:** 2 дня

---

## 1. Введение

### 1.1 Цель документа
Настоящее техническое задание описывает подготовку структуры проекта и конфигурацию Spring Boot приложения для микросервиса Auth Service, который будет обеспечивать централизованную аутентификацию и авторизацию пользователей через интеграцию с Keycloak.

### 1.2 Область применения
Техническое задание предназначено для Java backend разработчика, который будет создавать и настраивать проект Auth Service в соответствии с требованиями.

### 1.3 Ссылки
- [docs/tasks/third-task.md](../tasks/third-task.md) - Основная задача
- [docs/architecture/system-overview.md](../architecture/system-overview.md) - Архитектура системы

---

## 2. Общие требования

### 2.1 Цель проекта
Создать микросервис Auth Service с нуля по стандартной структуре Spring Boot 3.4.5 с использованием Java 17.

### 2.2 Функциональные требования
- [ ] Проект должен создаваться по стандартной структуре Spring Boot
- [ ] Все зависимости должны быть корректно настроены в build.gradle.kts
- [ ] Основной класс приложения должен содержать все необходимые аннотации
- [ ] Конфигурация должна поддерживать локальный и docker режимы работы

### 2.3 Нефункциональные требования
- **Производительность:** Запуск проекта не должен превышать 60 секунд
- **Совместимость:** Все зависимости должны быть совместимы между собой
- **Документация:** Код должен быть сопровожден JavaDoc комментариями

---

## 3. Требования к проекту

### 3.1 Структура проекта

#### 3.1.1 Директории
```
services/
└── auth-service/
    ├── src/
    │   ├── main/
    │   │   ├── java/com/autodev/auth/
    │   │   │   ├── AuthApplication.java
    │   │   │   ├── config/
    │   │   │   ├── controller/
    │   │   │   ├── dto/
    │   │   │   ├── entity/
    │   │   │   ├── repository/
    │   │   │   ├── service/
    │   │   │   └── event/
    │   │   └── resources/
    │   │       ├── application.yml
    │   │       ├── application-docker.yml
    │   │       └── db/
    │   │           └── changelog/
    │   └── test/
    │       └── java/com/autodev/auth/
    ├── build.gradle.kts
    ├── Dockerfile
    └── README.md
```

#### 3.1.2 Описание директорий
- `src/main/java/com/autodev/auth/` - основной исходный код
- `src/main/resources/` - ресурсы (конфигурация, миграции)
- `src/test/java/com/autodev/auth/` - тесты
- `build.gradle.kts` - конфигурация сборки
- `Dockerfile` - Dockerfile для контейнеризации

### 3.2 Build Configuration

#### 3.2.1 Зависимости
В `build.gradle.kts` должны быть указаны следующие зависимости:

```kotlin
dependencies {
    // Spring Boot Starters
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    
    // Spring Cloud
    implementation("org.springframework.cloud:spring-cloud-starter-consul-discovery")
    implementation("org.springframework.cloud:spring-cloud-starter-security")
    
    // Database
    runtimeOnly("org.postgresql:postgresql")
    
    // Caching
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    
    // Messaging
    implementation("org.springframework.kafka:spring-kafka")
    
    // Liquibase
    implementation("org.liquibase:liquibase-core")
    
    // MapStruct
    implementation("org.mapstruct:mapstruct")
    annotationProcessor("org.mapstruct:mapstruct-processor")
    
    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    
    // Test dependencies
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.kafka:spring-kafka-test")
    testImplementation("org.testcontainers:testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.testcontainers:kafka")
}
```

#### 3.2.2 Плагины
```kotlin
plugins {
    java
    id("org.springframework.boot") version "3.4.5"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.liquibase.gradle") version "2.2.1"
}
```

#### 3.2.3 Версии библиотек
Используются версии из `gradle.properties`:
- `springBootVersion=3.4.5`
- `springCloudVersion=2024.0.1`

### 3.3 Main Application Class

#### 3.3.1 AuthApplication.java
Создать файл `src/main/java/com/autodev/auth/AuthApplication.java`:

```java
package com.autodev.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Главный класс приложения Auth Service.
 * 
 * @author AutoDev Team
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableCaching
@EnableKafka
public class AuthApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }
}
```

### 3.4 Конфигурация приложения

#### 3.4.1 application.yml
Создать файл `src/main/resources/application.yml`:

```yaml
server:
  port: 8080

spring:
  application:
    name: auth-service
  
  profiles:
    active: local
  
  jpa:
    open-in-view: false
    hibernate:
      ddl-auto: none
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
        enable_lazy_load_no_trans: true
    database-platform: org.hibernate.dialect.PostgreSQLDialect
  
  datasource:
    driver-class-name: org.postgresql.Driver
  
  data:
    redis:
      host: ${REDIS_HOST:redis}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD}
  
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:kafka:9092}
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
      properties:
        spring.json.trusted.packages: com.autodev.auth.event
    consumer:
      group-id: auth-service-group
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        spring.json.trusted.packages: com.autodev.auth.event
        spring.json.value.default.type: com.autodev.auth.event.UserCreatedEvent

  liquibase:
    enabled: true
    change-log: classpath:db/changelog/db.changelog-master.yaml
  
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: ${KEYCLOAK_ISSUER_URI:http://keycloak:8080/realms/autodev}

consul:
  host: ${CONSUL_HOST:consul}
  port: ${CONSUL_PORT:8500}
  discovery:
    enabled: true
    instance-id: ${spring.application.name}:${random.value}
    service-name: ${spring.application.name}
    health-check-path: /actuator/health
    health-check-interval: 10s
    register: true
    prefer-ip-address: false

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,env,configprops
  endpoint:
    health:
      show-details: always
      probes:
        enabled: true

logging:
  level:
    root: INFO
    com.autodev.auth: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

#### 3.4.2 application-docker.yml
Создать файл `src/main/resources/application-docker.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST:auth-database}:${DB_PORT:5432}/${DB_NAME:auth}
    username: ${DB_USER:auth_user}
    password: ${DB_PASSWORD:auth_password}
  
  redis:
    host: ${REDIS_HOST:redis}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD}
  
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:kafka:9092}
  
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: ${KEYCLOAK_ISSUER_URI:http://keycloak:8080/realms/autodev}

consul:
  host: ${CONSUL_HOST:consul}
  port: ${CONSUL_PORT:8500}

server:
  port: 8080
```

### 3.5 Дополнительные требования

#### 3.5.1 Папка для миграций
Создать структуру папок:
```
src/main/resources/db/changelog/
```

#### 3.5.2 Структура Java пакетов
```
com.autodev.auth
├── AuthApplication.java
├── config/              # Конфигурационные классы
├── controller/          # REST контроллеры
├── dto/                 # DTO классы
├── entity/              # JPA сущности
├── repository/          # Репозитории
├── service/             # Сервисы
└── event/               # Классы событий для Kafka
```

---

## 4. Требования к коду

### 4.1 Стандарты кодирования
- Использовать Java 17
- Соблюдать принятые в проекте соглашения об именовании
- Добавлять JavaDoc ко всем публичным классам и методам
- Использовать аннотации Lombok для сокращения boilerplate кода

### 4.2 Именование
- Классы: UpperCamelCase (AuthApplication, UserService)
- Методы: lowerCamelCase (findByEmail, getAllUsers)
- Константы: UPPER_SNAKE_CASE (MAX_LENGTH)
- Пакеты: lower snake case (com.autodev.auth)

### 4.3 Версионирование
- Использовать Git для управления версиями
- Коммиты должны следовать Conventional Commits

---

## 5. Критерии приемки

- [ ] Проект создается по стандартной структуре Spring Boot
- [ ] `build.gradle.kts` содержит все необходимые зависимости с правильными версиями
- [ ] Основной класс приложения содержит все необходимые аннотации
- [ ] `application.yml` настроен для локальной разработки
- [ ] `application-docker.yml` настроен для работы в Docker
- [ ] Структура папок соответствует требованиям
- [ ] Проект компилируется без ошибок: `./gradlew build`
- [ ] Проект запускается без ошибок: `./gradlew bootRun`

---

## 6. Риски

| Риск | Влияние | Вероятность | Митигация |
|------|---------|-------------|-----------|
| Неправильные версии зависимостей | Высокое | Средняя | Использовать версии из gradle.properties |
| Неправильная структура проекта | Среднее | Средняя | Следовать стандартам проекта |
| Проблемы с компиляцией | Высокое | Низкая | Проверять совместимость зависимостей |

---

## 7. Приложения

### 7.1 Проверка компиляции
После создания проекта выполнить:
```bash
cd services/auth-service
./gradlew build
```

### 7.2 Проверка запуска
```bash
./gradlew bootRun
```

Ожидаемое поведение:
- Приложение запускается на порту 8080
- Логи содержат сообщения о запуске Spring Boot
- Приложение готово принимать запрос��

---

## 8. История изменений

| Версия | Дата | Автор | Описание изменений |
|--------|------|-------|-------------------|
| 1.0 | 2026-06-02 | Аналитик | Первоначальная версия |
