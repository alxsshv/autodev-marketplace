# AutoDev Marketplace — System Architecture Overview
**Распределённая система продажи автозапчастей на стеке Java и Spring Boot**

*Версия документа: 1.9*
*Дата обновления: 2026-06-21*
---


## 1. Обзор системы

AutoDev Marketplace — это учебная платформа для продажи автозапчастей, построенная по принципам современных распределённых систем. Проект предназначен для обучения Java backend-разработчиков best practices разработки микросервисных приложений на Spring Boot 3.x и Java 17+.


Система предоставляет ключевые бизнес-ценности: удобный поиск запчастей по VIN и артикулам, безопасную сделку через эскроу-счёт, автоматизированную загрузку прайс-листов от продавцов и надёжную интеграцию с внешними сервисами доставки и оплаты.


**Целевая аудитория:**
- Покупатели автозапчастей
- Продавцы (магазины, разборки)
- Модераторы платформы
- Администраторы системы

---


## 3. Архитектурное представление

### 3.0. Названия сервисов и директорий (MVP: 7 сервисов)

| Человекочитаемое название | Название сервиса (service name) | Директория |
|---------------------------|--------------------------------|-----------|
| API Gateway | api-gateway | services/api-gateway |
| Catalog Service | catalog-service | services/catalog-service |
| Order Service | order-service | services/order-service |
| Payment Service | payment-service | services/payment-service |
| Communication Service | communication-service | services/communication-service |
| Notification Service | notification-service | services/notification-service |
| Platform Service | platform-service | services/platform-service |

**Platform Service для MVP (строгий минимум):**
- `UserProfileService` - управление профилями пользователей (синхронизировано с Keycloak)
- `ReviewService` - управление отзывами и рейтингами


**Примечание:**
- Для MVP используется 7 консолидированных сервисов
- Подробности консолидации см. в `docs/architecture/business/services-for-mvp.md`

**Миграции базы данных:**
- Для MVP используется версия `v1.0.0` для каждого сервиса
- Master changelog файлы используют YAML формат (`master.yaml`)
- SQL файлы миграций в директории `v1.0.0/`


**Platform Service (MVP):**
- `UserProfileService` - управление профилями пользователей (синхронизировано с Keycloak)
- `ReviewService` - управление отзывами и рейтингами


### 3.1. Контейнеры (C4 Level 2) - MVP: 7 сервисов

```mermaid
graph TD
    subgraph "Клиенты"
        A[Web SPA (React)]
        B[Admin SPA (React)]
        C[Мобильное приложение]
    end
    
    subgraph "MVP Сервисы (7)"
        D[API Gateway]
        E[Catalog Service]
        F[Order Service]
        G[Payment Service]
        H[Communication Service]
        I[Notification Service]
        J[Platform Service]
    end
    
    subgraph "Хранилища"
        K[PostgreSQL]
        L[Redis]
        M[Kafka]
        N[MinIO]
    end
    
    A --> D
    B --> D
    C --> D
    
    D --> E
    D --> F
    D --> G
    D --> H
    D --> I
    D --> J
    
    E --> K
    F --> K
    G --> K
    H --> K
    I --> K
    J --> K
    
    L --> D
    L --> E
    L --> F
    L --> G
    L --> H
    L --> I
    L --> J
    
    E --> M
    F --> M
    G --> M
    H --> M
    I --> M
    J --> M
    
    N --> E
    N --> J
    
    classDef client fill:#4CAF50,stroke:#333,stroke-width:1px,color:white;
    classDef service fill:#2196F3,stroke:#333,stroke-width:1px,color:white;
    classDef storage fill:#9C27B0,stroke:#333,stroke-width:1px,color:white;
    
    class A,B,C client
    class D,E,F,G,H,I,J service
    class K,L,M,N storage
```

### 3.2. Компоненты (C4 Level 3)


**API Gateway:**
- `GatewayService` - маршрутизация запросов
- `AuthenticationFilter` - JWT валидация через Keycloak
- `RateLimitingFilter` - ограничение запросов
- `CORSFilter` - управление политиками CORS
- `ServiceTokenFilter` - межсервисная аутентификация
- `SecurityAuditFilter` - запись событий безопасности

**Catalog Service (MVP):**
- `ProductCatalogService` - базовый каталог товаров (без аналогов и TecDoc интеграций)
- `CategoryService` - категории
- `VINLookupService` - подбор по VIN
- `PriceService` - управление ценами


**Order Service (MVP):**
- `OrderService` - оформление заказов
- `CartService` - корзина
- `DeliveryService` - выбор способа доставки и календарь
- `TrackingService` - отслеживание заказа с ТК
- `DocumentService` - печать документов
- `DeliveryCostService` - расчёт стоимости доставки (базовый)
- `ReturnService` - возвраты и гарантия

**Payment Service (MVP):**
- `PaymentProcessingService` - базовая обработка оплаты (без эскроу и интеграций)
- `PaymentMethodService` - управление способами оплаты


**Communication Service (MVP):**
- `CommunicationService` - чат в реальном времени (WebSocket, базовый функционал)


**Notification Service (MVP):**
- `NotificationService` - email, SMS, push уведомления

**Platform Service (MVP):**
- `UserProfileService` - управление профилями пользователей (синхронизировано с Keycloak)
- `ReviewService` - управление отзывами и рейтингами


---


## 4. Ключевые архитектурные решения

### Стиль архитектуры
**Микросервисная архитектура** выбрана как наиболее подходящая для данного проекта, так как:
- Позволяет независимую разработку и развёртывание сервисов
- Обеспечивает гибкость в выборе технологий для отдельных сервисов
- Упрощает масштабирование отдельных компонентов
- Соответствует учебным целям проекта по изучению распределённых систем

### Коммуникация между сервисами
- **Синхронная коммуникация:** REST API (через Spring Cloud Gateway и OpenFeign для межсервисных вызовов) для прямых запросов с ожиданием ответа
- **Асинхронная коммуникация:** Apache Kafka для событийно-ориентированной архитектуры (Event-Driven) и обеспечения отказоустойчивости

### Паттерны проектирования
- **API Gateway:** единая точка входа для всех клиентов с маршрутизацией, аутентификацией и rate limiting
- **Service Discovery:** Consul для динамического обнаружения сервисов
- **Circuit Breaker:** Resilience4j для предотвращения каскадных сбоев
- **Saga Pattern:** для управления распределёнными транзакциями (оформление заказа)
- **CQRS:** разделение команд и запросов для оптимизации производительности
- **Event-Driven Architecture:** асинхронное взаимодействие через публикацию и потребление событий (Pub/Sub) из Kafka

### Инфраструктура высокой доступности
- **PostgreSQL Primary-Replica:** синхронная репликация между зонами отказа
- **Kafka Cluster:** 3 брокера с replication.factor=3 и min.insync.replicas=2

### Стратегия развёртывания
- **Blue-Green Deployment:** для обеспечения zero-downtime обновлений в production
- Поддержка **Canary Releases** для постепенного внедрения изменений

### Масштабирование
- **Горизонтальное масштабирование** stateless сервисов (API Gateway, платформенные сервисы)
- **Вертикальное масштабирование** баз данных при необходимости
- **Автомасштабирование** на основе метрик (CPU, memory, request rate)

---


## 5. Основные функциональные модули

### API Gateway
Реализует единую точку входа для всех клиентов. Выполняет маршрутизацию запросов к соответствующим микросервисам, аутентификацию через JWT (валидация через Keycloak напрямую, без auth-service), ограничение частоты запросов (rate limiting) и управление политиками CORS. Использует Spring Cloud Gateway с интеграцией в Keycloak для проверки токенов.

**ВАЖНО:** API Gateway проверяет JWT токены через Keycloak endpoint напрямую, без промежуточного auth-service. Это упрощает архитектуру и снижает задержки.

### Platform Service
Для MVP включает только функции управления профилями и отзывами:
- `UserProfileService` - управление профилями пользователей (синхронизировано с Keycloak)
- `ReviewService` - управление отзывами и рейтингами

**Важно:** Для MVP Platform Service НЕ включает модерацию, аналитику, маркетинг, лояльность и другие функции. Эти функции могут быть добавлены после релиза MVP.

### Catalog Service (MVP)
Предоставляет базовый функционал каталога товаров с подбором по VIN. Управляет категориями и характеристиками запчастей.

**Важно:** Для MVP Catalog Service включает только базовый каталог с поиском (без аналогов, без TecDoc интеграции). 

**Полнотекстовый поиск для MVP:** PostgreSQL FTS используется вместо отдельного search-service. Это упрощает архитектуру и снижает требования к инфраструктуре.

### Order Service
Реализует процесс оформления заказов с корзиной.

### Payment Service
Обеспечивает обработку оплаты через онлайн-оплату картой, наличные при получении и банковский перевод. 

### Notification Service
Обеспечивает отправку уведомлений через email, SMS и push. Поддерживает персонализированные подписки на события. Реализует асинхронную отправку уведомлений через Kafka для обеспечения надёжности доставки уведомлений.

### Communication Service
Предоставляет функционал внутреннего чата между покупателями и продавцами. Базовый функционал.

## 6. Нефункциональные требования (NFR)


| Требование | Значение | Метрика |
|-----------|---------|--------|
| **Производительность** | Время отклика < 500 мс | p95 latency |
| **Пропускная способность** | 1000 RPS | Requests per second |
| **Доступность** | 99.9% | Uptime |
| **Масштабируемость** | Поддержка роста в 10 раз | Load testing |
| **Безопасность** | TLS 1.3, BCrypt | OWASP Top 10 |
| **Локализация** | Русский, английский | i18n support |


---


## 7. Данные и хранение

### Основные сущности
- `User`: пользователи системы
- `Seller`: продавцы и их профили
- `Product`: товарные позиции
- `Category`: категории товаров
- `Order`: заказы
- `Review`: отзывы и рейтинги
- `Message`: сообщения и переписка
- `Comparison`: сравнение товаров
- `Media`: медиафайлы (изображения, видео)
- `Notification`: уведомления и подписки
- `Inventory`: учёт наличия товаров
- `Pricing`: информация о ценах
- `Delivery`: информация о доставке
- `SearchHistory`: история поиска
- `ViewHistory`: история просмотров
- `FavoriteItem`: избранные товары
- `Promotion`: акции и скидки
- `Ad`: объявления продавцов
- `KnowledgeArticle`: статьи базы знаний
- `VideoCall`: информация о видеозвонках
- `IntegrationLog`: логи интеграции с внешними системами
- `AuditLog`: аудитные записи действий
- `SecurityEvent`: события безопасности (логины, попытки входа)

## 8. Технологический стек

### Бэкенд
- **Java**: 17 (LTS версия) - основной язык разработки
- **Spring Boot**: 3.4.5 - фреймворк для создания микросервисов
- **Spring Cloud**: 2024.0.0 - интеграция микросервисов, Service Discovery, Circuit Breaker
- **Spring Data JPA**: 3.4.5 - доступ к данным в реляционных базах данных
- **Spring Security**: 6.4.5 - аутентификация и авторизация
- **Spring Cloud Gateway**: 4.4.5 - API Gateway для маршрутизации запросов
- **Spring Cloud Consul**: 5.0.0 - Service Discovery и конфигурация
- **Spring Security OAuth2 Resource Server**: 6.4.5 - прямая валидация JWT токенов в каждом downstream-сервисе через JWK endpoint Keycloak (без промежуточных заголовков).
- **Resilience4j**: 2.1.0 - реализация паттернов Circuit Breaker, Rate Limiter
- **Liquibase**: 4.27.0 - версионирование и миграция схемы базы данных
- **MapStruct**: 1.5.2 - маппинг объектов
- **Lombok**: 1.18.34 - уменьшение boilerplate кода
- **JUnit 5**: 5.10.3 - модульное тестирование
- **Mockito**: 5.12.0 - мокирование в тестах
- **Testcontainers**: 1.19.7 - интеграционное тестирование с реальными контейнерами
- **WireMock**: 2.35.0 - мокирование HTTP сервисов в тестах
- **Gradle**: 8.8 - сборка проекта и управление зависимостями

### Базы данных и кэширование
- **PostgreSQL**: 15 - основная реляционная база данных для хранения структурированных данных
- **Redis**: 7 - кэширование часто запрашиваемых данных и хранение сессий (Redis Cluster с Sentinel для автоматического failover)
- **Apache Kafka**: 7.3.2 - асинхронная коммуникация между сервисами, event sourcing
- **MinIO**: 2023.05.14 - объектное хранилище для файлов и изображений

### Инфраструктура и мониторинг
- **Docker**: 20.10.23 - контейнеризация сервисов
- **Docker Compose**: 2.20.2 - оркестрация контейнеров в development и staging окружениях
- **Consul**: 1.15.3 - Service Discovery и управление конфигурациями
- **Keycloak**: 21.1.1 - централизованная аутентификация и авторизация (IAM)
- **Grafana**: 12.4.0 - визуализация метрик и логов
- **Prometheus**: 2.48.0 - сбор и хранение метрик
- **Loki**: 2.9.2 - централизованное логирование
- **Tempo**: 2.4.0 - трассировка распределённых запросов
- **Alloy**: 1.12.2 - агент для отправки метрик, логов и трассировок в Grafana Cloud
- **Nginx**: 1.25 - обратный прокси и балансировка нагрузки

### CI/CD и DevOps
- **GitLab CI/CD**: 16.11 - автоматизация сборки, тестирования и развёртывания
- **Git**: 2.40.1 - система контроля версий
- **Conventional Commits**: 1.0.0 - стандарт для сообщений коммитов

### Фронтенд (для полноты картины)
- **React**: 18.2.0 - фронтенд фреймворк для веб-интерфейсов
- **TypeScript**: 5.0.4 - типизированный JavaScript
- **Redux**: 4.2.1 - управление состоянием приложения
- **Material UI**: 5.11.16 - компоненты интерфейса

### Документация API
- **OpenAPI 3.0**: спецификация для описания REST API
- **Swagger UI**: 4.15.5 - визуализация документации API

### Тестирование
- **Postman**: 10.18.7 - ручное тестирование API
- **JMeter**: 5.6.3 - нагрузочное тестирование

### Примечания по совместимости
Все указанные версии технологий совместимы между собой:
- Spring Boot 3.4.5 совместим с Java 17 и поддерживает все указанные версии Spring Cloud и Spring Data
- Liquibase 4.27.0 полностью поддерживается Spring Boot 3.4.5
- Указанные версии Grafana, Prometheus, Loki, Tempo и Alloy совместимы между собой и образуют полноценную альтернативу ELK стеку для мониторинга и логирования
- Все версии библиотек и фреймворков протестированы на совместимость в рамках Spring Boot 3.4.5

---

## 9. Безопасность

### 9.1 Общая стратегия

Документ описывает стратегию обеспечения безопасности AutoDev Marketplace, включая аутентификацию, авторизацию, шифрование данных и аудит.

### 9.2 Межсервисная аутентификация

#### Валидация JWT в API Gateway и Downstream-сервисах

Критическая архитектурная парадигма (Direct JWT Propagation):

- API Gateway выступает единым прокси-сервером и маршрутизатором. Он не извлекает claims из JWT и не генерирует кастомные заголовки (никаких X-User-Id, X-User-Roles).
- API Gateway пробрасывает стандартный заголовок Authorization: Bearer <token> в downstream-сервисы без изменений.
- Каждый downstream-сервис выступает как OAuth2 Resource Server и самостоятельно валидирует подпись и срок действия JWT токена напрямую через Keycloak (получая публичные ключи через JWK endpoint).
- После валидации Spring Security в downstream-сервисе формирует Authentication объект, из которого сервис извлекает sub (User ID), email, preferred_username и роли (realm_access.roles) для принятия решений об авторизации (RBAC).

**Схема работы**

```
Client → [Authorization: Bearer <JWT>] → API Gateway → [Authorization: Bearer <JWT>] → Downstream Service
                                         (Маршрутизация)                              (Spring Security OAuth2 Resource Server:
                                                                                      валидация JWT + извлечение ролей)
```


#### Преимущества подхода:

Отсутствие дублирующей логики по маппингу заголовков.
Строгое соответствие стандартам Spring Security.
Сервисы не доверяют Gateway в вопросах безопасности, они доверяют только криптографической подписи Keycloak.
Снижение нагрузки на API Gateway (отсутствие кастомных фильтров парсинга токенов).

#### Для MVP (без TLS)
- **Service Account Tokens через Keycloak**
  - Каждый сервис имеет свой service account в Keycloak
  - При запуске сервис получает JWT token от Keycloak
  - Для межсервисных вызовов сервисы предъявляют свои токены
  - Получающий сервис валидирует токен через Keycloak или кэш в Redis

#### Для продакшена (планируемое улучшение)
- **MTLS (mutual TLS) для service-to-service коммуникации**
  - Каждый сервис имеет сертификат
  - TLS handshake проверяет сертификаты обеих сторон
  - Высокая степень безопасности для production окружения

#### Рекомендация для MVP
Использовать Service Account Tokens через Keycloak:
- Простота реализации
- Уже интегрирован Keycloak
- Возможность отозвать токен в любой момент
- Поддержка в Spring Security

### 9.3 Стратегия шифрования

#### Для MVP
- **TLS 1.3 для внешних API** (client ↔ service)
- **Внутренняя сеть без TLS** между сервисами (Docker network isolation)
- **Шифрование данных в покое**:
  - PostgreSQL: SSL для внешних подключений
  - Регулярные бэкапы с шифрованием (MinIO server-side encryption)

#### Для продакшена (планируемое улучшение)
- **MTLS между сервисами** (внутренняя коммуникация)
- **Шифрование на уровне приложения** для чувствительных данных:
  - Платёжные данные: AES-256
  - Персональные данные: AES-256
  - Ключи хранятся в Hashicorp Vault

#### Шифрование паролей
- BCrypt для пользовательских паролей
- PBKDF2 для service account паролей

### 9.4 RBAC (Role-Based Access Control)

#### Уровни доступа
| Роль | Описание | Примеры сервисов |
|------|----------|-----------------|
| BUYER | Покупатель товаров | order-service, catalog-service |
| SELLER | Продавец товаров | catalog-service, order-service, platform-service |
| MODERATOR | Модератор контента | platform-service, communication-service |
| ADMIN | Администратор системы | platform-service, admin-service |

#### Применение RBAC по сервисам

**Platform Service (включает функции user-service):**
- BUYER: view own profile, manage favorites, search
- SELLER: manage store, manage own products, view analytics
- MODERATOR: moderate content, view reports
- ADMIN: system configuration, user management

**Order Service:**
- BUYER: create orders, view own orders
- SELLER: view orders for own products, update order status
- MODERATOR: view all orders
- ADMIN: full access

**Catalog Service:**
- BUYER: view products, search
- SELLER: create/edit own products
- MODERATOR: view all products, flag inappropriate
- ADMIN: full access, category management

**Payment Service:**
- BUYER: process payment for own orders
- SELLER: view payment history for own products
- ADMIN: full access, fraud detection

**Communication Service:**
- BUYER: send messages to sellers, receive messages
- SELLER: send messages to buyers, receive messages
- MODERATOR: view all messages for moderation
- ADMIN: full access

### 9.5 План аудита безопасности

#### Еженедельные проверки
- Логи аутентификации (неудачные попытки входа)
- Статистика по rate limiting
- Изменения в RBAC (новые роли, изменения прав)

#### Ежемесячные аудиты
- Ревью access logs всех сервисов
- Проверка сертификатов и ключей
- Анализ CVE для используемых библиотек
- Аудит конфигураций (application.yml, docker-compose)

#### Квартальные аудиты
- Penetration testing (внешний аудит)
- Security code review
- Аудит бэкапов и восстановления
- Тестирование disaster recovery плана

#### Аудит перед релизом
- Security checklist для каждого сервиса
- Review PR с изменениями безопасности
- Тестирование новых endpoints на инъекции

#### Инструменты аудита
- **Логирование:** Loki с алертингом на подозрительные действия
- **Мониторинг:** Prometheus метрики по безопасности (error rate, auth failures)
- **Трейсинг:** Tempo для отслеживания запросов
- **Static Analysis:** SonarQube для анализа кода
- **Dynamic Analysis:** OWASP ZAP для penetration testing

### 9.6 OWASP Top 10 compliance

#### Меры по защите
1. **A01:2021 – Broken Access Control**
   - RBAC для всех сервисов
   - JWT валидация на каждом endpoint
   - Rate limiting на API Gateway

2. **A02:2021 – Cryptographic Failures**
   - BCrypt для паролей
   - TLS 1.3 для внешних API
   - Шифрование бэкапов

3. **A03:2021 – Injection**
   - Prepared statements (JPA/Hibernate)
   - Валидация входных данных
   - SQL инъекции блокируются на уровне ORM

4. **A04:2021 – Insecure Design**
   - Security by design принцип
   - Threat modeling для критичных сервисов
   - Code review с упором на безопасность

5. **A05:2021 – Security Misconfiguration**
   - Externalized configuration
   - Secrets через environment variables
   - Нет hardcoded credentials

6. **A06:2021 – Vulnerable Components**
   - Regular dependency updates
   - Snyk/Dependabot для мониторинга CVE
   - Отказ от устаревших библиотек

7. **A07:2021 – Identification and Authentication Failures**
   - JWT expiration (12 часов для access token, 7 дней для refresh token)
   - Rate limiting на login (5 попыток в минуту)
   - Password policies (минимум 8 символов, цифры, спецсимволы)

8. **A08:2021 – Software and Data Integrity Failures**
   - Signing docker images (Notary)
   - Secure CI/CD pipeline (GitLab CI)
   - Checksum verification для зависимостей

9. **A09:2021 – Security Logging and Monitoring Failures**
   - Centralized logging (Loki)
   - Real-time monitoring (Prometheus)
   - Alerting на security events

10. **A10:2021 – Server-Side Request Forgery**
    - URL validation (белый список доменов)
    - Restricted domains
    - Internal network isolation

### 9.7 Security incident response

#### Процедура реагирования
1. **Обнаружение:** Алерт через Prometheus/Grafana
2. **Оценка:** Определение масштаба инцидента
3. **Изоляция:** Блокировка affected сервисов
4. **Устранение:** Исправление уязвимости
5. **Восстановление:** Возврат сервисов в нормальное состояние
6. **Анализ:** Post-mortem с выводами

#### Контакты в экстренной ситуации
- Security Team: #security-incidents
- Slack Alert: @security-oncall
- Emergency hotline: +7XXX-XXX-XXXX