# Стратегия тестирования AutoDev Marketplace

**Версия документа:** 1.0  
**Дата создания:** 2026-06-04  
**Последнее обновление:** 2026-06-04

---

## Обзор

Этот документ описывает стратегию тестирования для AutoDev Marketplace MVP (8 сервисов) с акцентом на покрытие критичных бизнес-процессов, интеграционное тестирование и тестирование отказоустойчивости.

---

## Архитектура тестирования

### Типы тестов

```
+---------------------+
|    Unit Tests       |  Быстрые, изолированные тесты методов/классов
+---------------------+
|   Integration Tests |  Тесты взаимодействия сервисов через API
+---------------------+
|  Contract Tests     |  Проверка совместимости API контрактов
+---------------------+
|    E2E Tests        |  end-to-end тесты бизнес-процессов
+---------------------+
|  Chaos Engineering  |  Тестирование отказоустойчивости
+---------------------+
|    Load Tests       |  Нагрузочное тестирование производительности
+---------------------+
```

---

## Unit Tests

### Цель
Покрытие бизнес-логики каждого сервиса на уровне методов и классов.

### Покрытие
- **MVP:** 80%+ строк кода
- **Релиз 1.0:** 85%+ строк кода
- **Релиз 2.0:** 90%+ строк кода

### Технологии
- **JUnit 5** — основной фреймворк
- **Mockito** — мокирование зависимостей
- **AssertJ** — продвинутые ассерты
- **Testcontainers** — интеграция с Docker контейнерами

### Примеры

```java
// Пример unit теста для Catalog Service
@Test
void shouldCalculateDiscountForPremiumCustomer() {
    // Given
    Customer customer = new PremiumCustomer("user123");
    Product product = new Product("prod456", 1000.0);
    
    // When
    DiscountResult result = discountService.calculateDiscount(customer, product);
    
    // Then
    assertThat(result.getDiscountPercent()).isEqualTo(15);
    assertThat(result.getFinalPrice()).isEqualTo(850.0);
}

// Пример unit теста с мокированием
@Test
void shouldReturnProductFromCache() {
    // Given
    Product product = new Product("prod456", 1000.0);
    when(cacheRepository.findById("prod456")).thenReturn(product);
    when(productRepository.findById("prod456")).thenReturn(Optional.empty());
    
    // When
    Product result = catalogService.getProduct("prod456");
    
    // Then
    verify(cacheRepository, times(1)).findById("prod456");
    verify(productRepository, times(0)).findById(anyString());
    assertThat(result).isEqualTo(product);
}
```

---

## Integration Tests

### Цель
Проверка взаимодействия сервисов через API и базы данных.

### Покрытие
- **MVP:** Критичные бизнес-процессы
- **Релиз 1.0:** Все основные процессы
- **Релиз 2.0:** Все процессы + edge cases

### Технологии
- **Testcontainers** — запуск реальных БД и сервисов в Docker
- **RestAssured** — тестирование REST API
- **WireMock** — мокирование внешних зависимостей
- **PostgreSQL TestContainers** — реальная БД

### Примеры

```java
// Пример integration теста для Order Service
@Test
void shouldCreateOrderWithPayment() {
    // Given
    CreateOrderRequest request = new CreateOrderRequest();
    request.setUserId("user123");
    request.setItems(List.of(
        new OrderItem("prod456", 2),
        new OrderItem("prod789", 1)
    ));
    
    // When
    Response response = given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .post("/api/v1/orders");
    
    // Then
    response.then()
        .statusCode(201)
        .body("status", equalTo("CREATED"))
        .body("totalAmount", equalTo(2500.0));
    
    // Verify payment service was called
    verify(paymentService, times(1)).processPayment(any());
}

// Пример теста с Testcontainers
@Test
void shouldConnectToDatabase() {
    // Given
    try (PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")) {
        postgres.start();
        
        // When
        DataSource dataSource = createDataSource(postgres);
        Connection connection = dataSource.getConnection();
        
        // Then
        assertThat(connection.isValid(3)).isTrue();
    }
}
```

---

## Contract Tests

### Цель
Проверка совместимости API контрактов между сервисами.

### Технологии
- **Pact** — contract testing для микросервисов
- **Spring Cloud Contract** — для Spring Boot сервисов

### Примеры

```java
// Пример Spring Cloud Contract для Catalog Service
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class CatalogContractTest {
    
    @Rule
    public final HTTPStubsServer stubsServer = new HttpStubsServer(8080);
    
    @Test
    public void shouldProvideProductById() {
        // Given
        stubsServer.given()
            .get("/api/v1/products/123")
            .willReturn(okJson("{\n" +
                "  \"id\": \"123\",\n" +
                "  \"name\": \"Product Name\",\n" +
                "  \"price\": 1000.0\n" +
                "}"));
        
        // When
        Product product = restClient.getProduct("123");
        
        // Then
        assertThat(product.getId()).isEqualTo("123");
        assertThat(product.getName()).isEqualTo("Product Name");
        assertThat(product.getPrice()).isEqualTo(1000.0);
    }
}
```

---

## E2E Tests

### Цель
Тестирование полных бизнес-процессов с точки зрения пользователя.

### Тестовые сценарии

#### MVP E2E Scenarios
1. **Регистрация и аутентификация**
   - Пользователь регистрируется
   - Пользователь авторизуется
   - Получает JWT токен

2. **Поиск и просмотр товара**
   - Пользователь ищет товар по названию
   - Просматривает детальную информацию
   - Смотрит доступность на складе

3. **Оформление заказа**
   - Пользователь добавляет товар в корзину
   - Оформляет заказ
   - Проходит оплату

4. **Отслеживание заказа**
   - Пользователь видит статус заказа
   - Получает уведомления
   - Отслеживает доставку

### Технологии
- **Cypress** — для фронтенд-интеграции
- **Playwright** — альтернатива Cypress
- **Serenity BDD** — для повествовательных тестов

### Примеры

```javascript
// Пример Cypress E2E теста
describe('Order Flow', () => {
    beforeEach(() => {
        cy.visit('/')
        cy.login('user@example.com', 'password')
    })
    
    it('should create and track an order', () => {
        // Search for product
        cy.get('[data-test="search-input"]')
            .type('product name')
            .get('[data-test="search-submit"]')
            .click()
        
        // Add to cart
        cy.get('[data-test="product-card"]')
            .first()
            .get('[data-test="add-to-cart"]')
            .click()
        
        // Checkout
        cy.get('[data-test="cart-icon"]')
            .click()
            .get('[data-test="checkout-btn"]')
            .click()
        
        // Payment
        cy.get('[data-test="payment-form"]')
            .should('be.visible')
        
        // Track order
        cy.get('[data-test="order-confirmation"]')
            .should('be.visible')
            .get('[data-test="track-btn"]')
            .click()
            .get('[data-test="tracking-info"]')
            .should('be.visible')
    })
})
```

---

## Chaos Engineering

### Цель
Проверка отказоустойчивости системы и её способности восстанавливаться после сбоев.

### Scenarios для MVP

1. **Service Failure**
   - Остановить один из подов сервиса
   - Проверить, что трафик перенаправляется на другой под
   - Убедиться, что клиент получает ответ (даже если медленный)

2. **Database Failure**
   - Остановить PostgreSQL Primary
   - Проверить автоматический failover на Replica
   - Убедиться, что приложение обрабатывает ошибку корректно

3. **Network Partition**
   - Отделить один из узлов кластера
   - Проверить сохранность данных
   - Убедиться, что приложение обрабатывает сетевые ошибки

4. **Resource Exhaustion**
   - Увеличить нагрузку до исчерпания ресурсов
   - Проверить поведение при нехватке памяти/CPU
   - Убедиться в корректной работе circuit breakers

### Технологии
- **Chaos Monkey** — автоматизация chaos experiments
- **Gremlin** — управляемая chaos engineering платформа
- **Litmus** — chaos engineering для Kubernetes

### Примеры

```yaml
# Пример Chaos Monkey конфигурации
chaos-monkey:
  enabled: true
  assault:
    level: 1
    patterns:
      - "com.autodev.*"
    exceptions:
      - "org.springframework.boot.*"
  limits:
    min-recovery-time: 30s
    max-recovery-time: 60s
```

```java
// Пример chaos теста
@Test
void shouldHandleDatabaseFailover() {
    // Given
    String serviceToKill = "postgresql-primary";
    
    // When
    kubernetesClient.pods()
        .withName(serviceToKill)
        .delete();
    
    // Wait for failover
    Thread.sleep(10000);
    
    // Try to access database
    try {
        dataSource.getConnection();
        fail("Should have thrown exception");
    } catch (SQLException e) {
        // Expected - database is unavailable
    }
    
    // Verify failover completed
    assertThat(postgresPrimary.isUp()).isFalse();
    assertThat(postgresReplica.isUp()).isTrue();
    
    // Database should be available again
    dataSource.getConnection(); // Should succeed
}
```

---

## Load Tests

### Цель
Оценка производительности системы под нагрузкой.

### Метрики MVP
- **API Gateway:** 1000+ RPS, p95 < 500мс
- **Auth Service:** 500+ RPS, p95 < 200мс
- **Catalog Service:** 2000+ RPS, p95 < 300мс
- **Order Service:** 500+ RPS, p95 < 1000мс
- **Payment Service:** 200+ RPS, p95 < 2000мс
- **Search Service:** 3000+ RPS, p95 < 100мс

### Технологии
- **JMeter** — основной инструмент нагрузочного тестирования
- **k6** — современный альтернативный инструмент
- **Gatling** — для интеграционных нагрузочных тестов

### Примеры

```jmx
<!-- Пример JMeter тест-план для API Gateway -->
<TestPlan>
    <ThreadGroup>
        <numThreads>1000</numThreads>
        <rampUp>60</rampUp>
        <duration>300</duration>
    </ThreadGroup>
    <HTTPSampler>
        <path>/api/v1/products</path>
        <method>GET</method>
    </HTTPSampler>
    <ResultCollector>
        <fileName>results/api-gateway-load.jtl</fileName>
    </ResultCollector>
</TestPlan>
```

```k6
// Пример k6 скрипта
import http from 'k6/http';
import { sleep } from 'k6';

export const options = {
    vus: 1000,
    duration: '5m',
};

export default function () {
    http.get('http://localhost:8081/api/v1/products');
    sleep(0.1);
}
```

---

## CI/CD Интеграция

### Pipeline stages

```
1. Build
   └─./gradlew build

2. Unit Tests
   └─./gradlew test
   └─Сохранить отчёты

3. Integration Tests
   └─./gradlew integrationTest
   └─Использовать Testcontainers

4. Contract Tests
   └─./gradlew pactVerify
   └─Проверить совместимость

5. Security Scan
   └─./gradlew spotbugsMain
   └─./gradlew checkstyleMain

6. Code Coverage
   └─./gradlew jacocoTestReport
   └─Сохранить отчёт

7. Deploy to Staging
   └─docker-compose -f docker-compose-staging.yml up -d

8. E2E Tests (Staging)
   └─cypress run --env environment=staging

9. Deploy to Production (manual)
```

### Code Quality Gates
- Unit test coverage: > 80%
- Integration test coverage: > 70%
- Security scan: no critical issues
- Performance: p95 < SLA

---

## Test Data Management

### Стратегия
- **Unit Tests:** Мокированные/Stubbed данные
- **Integration Tests:** Real databases с миграциями
- **E2E Tests:** Real databases с фикстурами
- **Load Tests:** Генерация тестовых данных

### Фикстуры
```java
@SpringBootTest
@Sql(
    scripts = "/sql/fixtures/users.sql",
    executionPhase = BEFORE_TEST_METHOD
)
@Sql(
    scripts = "/sql/fixtures/cleanup.sql",
    executionPhase = AFTER_TEST_METHOD
)
public class OrderServiceTest {
    // Test methods...
}
```

---

## Test Environment

### CI/CD Environment
- **Build Agents:** 4 CPU, 8 GB RAM
- **Test Containers:** Изолированные сети
- **Parallel Execution:** Включено (max 4 parallel jobs)

### Local Development
- **Testcontainers:** Включены по умолчанию
- **Database:** PostgreSQL via Docker
- **Redis:** Redis via Docker

---

## Success Metrics

### Coverage Targets
| Тип тестов | MVP | Релиз 1.0 | Релиз 2.0 |
|-----------|-----|-----------|-----------|
| Unit Tests | 80% | 85% | 90% |
| Integration Tests | 70% | 80% | 90% |
| E2E Tests | 50% | 70% | 85% |
| Contract Tests | 60% | 80% | 95% |

### Performance Targets
| Метрика | MVP | Релиз 1.0 |
|---------|-----|-----------|
| API Gateway p95 | < 500ms | < 300ms |
| Auth Service p95 | < 200ms | < 150ms |
| Catalog Service p95 | < 300ms | < 200ms |
| Order Service p95 | < 1000ms | < 500ms |
| Payment Service p95 | < 2000ms | < 1000ms |
| Search Service p95 | < 100ms | < 50ms |

---

## Заключение

Стратегия тестирования обеспечивает покрытие критичных бизнес-процессов, интеграционное тестирование всех сервисов и тестирование отказоустойчивости.

**Ключевые принципы:**
- Быстрые unit тесты для регулярного запуска
- Интеграционные тесты для проверки взаимодействия
- E2E тесты для проверки бизнес-процессов
- Chaos Engineering для проверки отказоустойчивости
- Load Tests для обеспечения производительности
- CI/CD интеграция для автоматизации

---

**Ответственные:**
- **QA Team:** Тестирование и качество
- **DevOps:** CI/CD и тестовые окружения
- **Dev Team:** Покрытие кода тестами

**Обновления:**
- Еженедельное ревью покрытия тестами
- Ежемесячное обновление стратегии
