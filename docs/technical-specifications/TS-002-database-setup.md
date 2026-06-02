# Техническое задание TS-002: Настройка базы данных

**Версия документа:** 1.0  
**Дата создания:** 2026-06-02  
**Автор:** Аналитик  
**Статус:** Готово к реализации  
**Приоритет:** Высокий  
**Оценка трудоемкости:** 3 дня

---

## 1. Введение

### 1.1 Цель документа
Настоящее техническое задание описывает настройку базы данных для микросервиса Auth Service, включая создание миграций Liquibase, JPA сущности и репозитория для управления пользователями.

### 1.2 Область применения
Техническое задание предназначено для Java backend разработчика, который будет работать с базой данных в Auth Service.

### 1.3 Ссылки
- [docs/tasks/third-task.md](../tasks/third-task.md) - Основная задача
- [docs/architecture/system-overview.md](../architecture/system-overview.md) - Архитектура системы
- [TS-001-auth-service-project-setup.md](TS-001-auth-service-project-setup.md) - Подготовка проекта

---

## 2. Общие требования

### 2.1 Цель проекта
Настроить базу данных PostgreSQL для хранения информации о пользователях в микросервисе Auth Service с использованием Liquibase для управления миграциями и Spring Data JPA для доступа к данным.

### 2.2 Функциональные требования
- [ ] Создать миграции Liquibase для создания таблицы users
- [ ] Создать JPA сущность User с соответствующими полями
- [ ] Создать UserRepository с методами поиска
- [ ] Настроить миграции на автоматическое применение при старте приложения

### 2.3 Нефункциональные требования
- **Производительность:** Операции с базой данных должны выполняться менее чем за 100 мс
- **Целостность данных:** Все ограничения базы данных (PK, UNIQUE, NOT NULL) должны быть соблюдены
- **Масштабируемость:** Структура таблиц должна поддерживать рост до 1 000 000 пользователей
- **Безопасность:** Чувствительные данные должны быть зашифрованы на уровне приложения

---

## 3. Требования к базе данных

### 3.1 Таблица users

#### 3.1.1 Структура таблицы
Создать таблицу `users` со следующими полями:

| Поле | Тип данных | Ограничения | Описание |
|------|------------|-------------|----------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Уникальный идентификатор пользователя в базе |
| keycloak_user_id | VARCHAR(255) | UNIQUE, NOT NULL | Уникальный идентификатор пользователя в Keycloak |
| email | VARCHAR(255) | UNIQUE, NOT NULL | Email пользователя |
| first_name | VARCHAR(255) | NULL | Имя пользователя |
| last_name | VARCHAR(255) | NULL | Фамилия пользователя |
| phone | VARCHAR(50) | NULL | Номер телефона пользователя |
| role | VARCHAR(50) | NOT NULL | Роль пользователя (BUYER, SELLER, MODERATOR, ADMIN) |
| enabled | BOOLEAN | NOT NULL, DEFAULT TRUE | Флаг активности пользователя |
| created_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Время создания записи |
| updated_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Время последнего обновления |

#### 3.1.2 Индексы
- PRIMARY KEY: id
- UNIQUE INDEX: keycloak_user_id
- UNIQUE INDEX: email
- INDEX: role
- INDEX: enabled

### 3.2 Миграции Liquibase

#### 3.2.1 Файл db.changelog-master.yaml
Создать файл `src/main/resources/db/changelog/db.changelog-master.yaml`:

```yaml
databaseChangeLog:
  - include:
      file: classpath:db/changelog/001-create-users-table.yaml
```

#### 3.2.2 Файл 001-create-users-table.yaml
Создать файл `src/main/resources/db/changelog/001-create-users-table.yaml`:

```yaml
databaseChangeLog:
  - changeSet:
      id: 001-create-users-table
      author: autodev
      description: Creates users table for auth service
      preConditions:
        - not:
            tableExists:
              tableName: users
      changes:
        - createTable:
            tableName: users
            columns:
              - column:
                  name: id
                  type: BIGINT
                  constraints:
                    primaryKey: true
                    nullable: false
              - column:
                  name: keycloak_user_id
                  type: VARCHAR(255)
                  constraints:
                    nullable: false
              - column:
                  name: email
                  type: VARCHAR(255)
                  constraints:
                    nullable: false
              - column:
                  name: first_name
                  type: VARCHAR(255)
              - column:
                  name: last_name
                  type: VARCHAR(255)
              - column:
                  name: phone
                  type: VARCHAR(50)
              - column:
                  name: role
                  type: VARCHAR(50)
                  constraints:
                    nullable: false
              - column:
                  name: enabled
                  type: BOOLEAN
                  constraints:
                    nullable: false
                    defaultValueBoolean: true
              - column:
                  name: created_at
                  type: TIMESTAMP
                  constraints:
                    nullable: false
                    defaultValueComputed: CURRENT_TIMESTAMP
              - column:
                  name: updated_at
                  type: TIMESTAMP
                  constraints:
                    nullable: false
                    defaultValueComputed: CURRENT_TIMESTAMP
        - addUniqueConstraint:
            tableName: users
            columnNames: keycloak_user_id
            constraintName: uk_users_keycloak_user_id
        - addUniqueConstraint:
            tableName: users
            columnNames: email
            constraintName: uk_users_email
        - createIndex:
            tableName: users
            indexName: idx_users_role
            columnNames: role
        - createIndex:
            tableName: users
            indexName: idx_users_enabled
            columnNames: enabled

  - changeSet:
      id: 002-add-default-role
      author: autodev
      description: Adds default role for existing users
      preConditions:
        - tableExists:
            tableName: users
      changes:
        - addDefaultValue:
            tableName: users
            columnName: role
            defaultValue: BUYER

  - changeSet:
      id: 003-add-email-index
      author: autodev
      description: Adds index on email column for faster search
      preConditions:
        - tableExists:
            tableName: users
      changes:
        - createIndex:
            tableName: users
            indexName: idx_users_email
            columnNames: email
```

### 3.3 JPA Сущность User

#### 3.3.1 Пакет entity
Создать пакет: `com.autodev.auth.entity`

#### 3.3.2 Класс User.java
Создать файл `src/main/java/com/autodev/auth/entity/User.java`:

```java
package com.autodev.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Сущность пользователя для аутентификации.
 * 
 * @author AutoDev Team
 */
@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
    name = "users",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_users_keycloak_user_id", columnNames = "keycloak_user_id"),
        @UniqueConstraint(name = "uk_users_email", columnNames = "email")
    },
    indexes = {
        @Index(name = "idx_users_role", columnList = "role"),
        @Index(name = "idx_users_enabled", columnList = "enabled"),
        @Index(name = "idx_users_email", columnList = "email")
    }
)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    /**
     * Уникальный идентификатор пользователя в локальной базе данных.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Уникальный идентификатор пользователя в Keycloak.
     */
    @Column(name = "keycloak_user_id", nullable = false, length = 255)
    private String keycloakUserId;
    
    /**
     * Email пользователя.
     */
    @Column(name = "email", nullable = false, length = 255)
    private String email;
    
    /**
     * Имя пользователя.
     */
    @Column(name = "first_name", length = 255)
    private String firstName;
    
    /**
     * Фамилия пользователя.
     */
    @Column(name = "last_name", length = 255)
    private String lastName;
    
    /**
     * Номер телефона пользователя.
     */
    @Column(name = "phone", length = 50)
    private String phone;
    
    /**
     * Роль пользователя.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 50)
    private UserRole role;
    
    /**
     * Флаг активности пользователя.
     */
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;
    
    /**
     * Дата и время создания записи.
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * Дата и время последнего обновления записи.
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
```

#### 3.3.3 Перечисление UserRole
Создать файл `src/main/java/com/autodev/auth/entity/UserRole.java`:

```java
package com.autodev.auth.entity;

/**
 * Перечисление ролей пользователей.
 * 
 * @author AutoDev Team
 */
public enum UserRole {
    
    /**
     * Покупатель.
     */
    BUYER,
    
    /**
     * Продавец.
     */
    SELLER,
    
    /**
     * Модератор.
     */
    MODERATOR,
    
    /**
     * Администратор.
     */
    ADMIN
}
```

### 3.4 Repository

#### 3.4.1 Пакет repository
Создать пакет: `com.autodev.auth.repository`

#### 3.4.2 Класс UserRepository.java
Создать файл `src/main/java/com/autodev/auth/repository/UserRepository.java`:

```java
package com.autodev.auth.repository;

import com.autodev.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

/**
 * Репозиторий для управления пользователями.
 * 
 * @author AutoDev Team
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Находит пользователя по идентификатору в Keycloak.
     * 
     * @param keycloakUserId идентификатор пользователя в Keycloak
     * @return опциональный объект пользователя
     */
    Optional<User> findByKeycloakUserId(String keycloakUserId);
    
    /**
     * Находит пользователя по email.
     * 
     * @param email email пользователя
     * @return опциональный объект пользователя
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Находит всех активных пользователей.
     * 
     * @return список активных пользователей
     */
    List<User> findByEnabledTrue();
    
    /**
     * Находит пользователей по роли.
     * 
     * @param role роль пользователя
     * @return список пользователей с указанной ролью
     */
    List<User> findByRole(UserRole role);
}
```

### 3.5 Конфигурация JPA

#### 3.5.1 Пакет config
Создать пакет: `com.autodev.auth.config`

#### 3.5.2 Класс JpaConfig.java
Создать файл `src/main/java/com/autodev/auth/config/JpaConfig.java`:

```java
package com.autodev.auth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Конфигурация JPA для автоматического управления датами создания и обновления.
 * 
 * @author AutoDev Team
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
    
    // Конфигурация аудита
}
```

---

## 4. Требования к коду

### 4.1 Стандарты кодирования
- Использовать Java 17
- Соблюдать принятые в проекте соглашения об именовании
- Добавлять JavaDoc ко всем публичным классам и методам
- Использовать аннотации Lombok для сокращения boilerplate кода
- Соблюдать принципы SOLID

### 4.2 Именование
- Классы: UpperCamelCase (User, UserRole, UserRepository)
- Методы: lowerCamelCase (findByEmail, getAllUsers)
- Константы: UPPER_SNAKE_CASE (MAX_LENGTH)
- Пакеты: lower snake case (com.autodev.auth.entity)

### 4.3 База данных
- Использовать PostgreSQL 15
- Все миграции должны быть идемпотентными
- Использовать liquibase для управления версиями схемы

---

## 5. Критерии приемки

- [ ] Миграция `001-create-users-table.yaml` создана и содержит все необходимые поля
- [ ] JPA сущность `User` создана с правильными аннотациями
- [ ] `UserRepository` содержит все необходимые методы
- [ ] Конфигурация JPA включает аудит дат
- [ ] Проект компилируется без ошибок: `./gradlew build`
- [ ] При запуске приложения миграции применяются автоматически
- [ ] Таблица `users` создается в базе данных PostgreSQL
- [ ] Ограничения (PK, UNIQUE) созданы корректно
- [ ] Индексы созданы корректно

---

## 6. Риски

| Риск | Влияние | Вероятность | Митигация |
|------|---------|-------------|-----------|
| Неправильная структура таблицы | Высокое | Средняя | Тщательно проверить структуру миграции |
| Неправильные аннотации JPA | Среднее | Низкая | Следовать документации Spring Data JPA |
| Проблемы с миграциями Liquibase | Высокое | Средняя | Тестировать миграции в изолированной среде |

---

## 7. Приложения

### 7.1 Проверка миграций
После создания миграций выполнить:
```bash
./gradlewliquibaseStatus
```

### 7.2 Проверка применения миграций
После запуска приложения проверить:
```sql
SELECT * FROM databasechangelog;
SELECT * FROM databasechangeloglock;
```

### 7.3 Проверка структуры таблицы
```sql
\d users
```

---

## 8. История изменений

| Версия | Дата | Автор | Описание изменений |
|--------|------|-------|-------------------|
| 1.0 | 2026-06-02 | Аналитик | Первоначальная версия |
