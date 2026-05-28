# Вторая задача: Реализация Service Discovery с использованием Consul

## Цель
Настроить централизованное обнаружение сервисов (Service Discovery) с использованием Consul для динамического обнаружения и балансировки нагрузки между микросервисами.

## Задача
Реализуйте Service Discovery согласно следующим шагам:

1. **Изучите текущую конфигурацию Consul**
   - Ознакомьтесь с конфигурацией Consul в `docker-compose.yaml`
   - Убедитесь, что сервис Consul запущен и доступен по http://localhost:8500
   - Проверьте, что переменные окружения и health check настроены корректно

2. **Добавьте зависимость Spring Cloud Consul в API Gateway**
   - Откройте `services/api-gateway/build.gradle.kts`
   - Добавьте зависимость:
   ```kotlin
   implementation("org.springframework.cloud:spring-cloud-starter-consul-discovery")
   ```
   - Синхронизируйте зависимости в IDE

3. **Настройте Service Discovery в API Gateway**
   - Откройте `services/api-gateway/src/main/resources/application.yml`
   - Добавьте конфигурацию Consul:
   ```yaml
   spring:
     cloud:
       consul:
         host: ${CONSUL_HOST:consul}
         port: ${CONSUL_PORT:8500}
         discovery:
           service-name: api-gateway
           health-check-path: /actuator/health
           health-check-interval: 15s
           prefer-ip-address: true
   ```

4. **Настройте маршрутизацию через Service Discovery**
   - В том же `application.yml` обновите конфигурацию маршрутов в API Gateway
   - Замените статические URL на логические имена сервисов:
   ```yaml
   spring:
     cloud:
       gateway:
         routes:
           - id: user-service
             uri: lb://user-service
             predicates:
               - Path=/api/users/**
             filters:
               - RewritePath=/api/users/(?<path>.*), /$\{path}

           - id: catalog-service
             uri: lb://catalog-service
             predicates:
               - Path=/api/catalog/**
             filters:
               - RewritePath=/api/catalog/(?<path>.*), /$\{path}
   ```

5. **Обновите SecurityConfig для работы с Service Discovery**
   - Убедитесь, что `SecurityConfig.java` в API Gateway корректно обрабатывает маршруты
   - При необходимости обновите правила авторизации для новых маршрутов

6. **Создайте заглушку для User Service**
   - Создайте директорию `services/user-service`
   - Создайте базовую структуру проекта с `build.gradle.kts`, `src/main/java` и `src/main/resources`
   - Добавьте зависимость от Spring Cloud Consul
   - Настройте `application.yml` для регистрации в Consul
   - Создайте простой REST контроллер для проверки работы Service Discovery

7. **Протестируйте работу Service Discovery**
   - Запустите API Gateway через Gradle: `./gradlew :services:api-gateway:bootRun`
   - Убедитесь, что API Gateway регистрируется в Consul
   - Проверьте Consul UI по адресу http://localhost:8500
   - Убедитесь, что API Gateway отображается в списке сервисов
   - Проверьте health check статус

## Критерии успешного выполнения
- Consul запущен и доступен по http://localhost:8500
- API Gateway успешно регистрируется в Consul
- Сервис отображается в Consul UI с статусом "Passing"
- Конфигурация Service Discovery добавлена в API Gateway
- Маршрутизация в API Gateway использует логические имена сервисов (lb://)
- Заглушка User Service создана и готова к дальнейшей разработке
- Приложение запускается без ошибок

## Дополнительные ресурсы
- [Официальная документация Spring Cloud Consul](https://docs.spring.io/spring-cloud-consul/docs/current/reference/html/)
- [Руководство по Consul](https://developer.hashicorp.com/consul/docs)
- [Spring Cloud Gateway с Service Discovery](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#configuring-predicates-and-filters-for-discoveryclient-routes)

## Следующий шаг
После успешного выполнения этой задачи приступите к реализации Auth Service с использованием Keycloak для централизованной аутентификации и авторизации.