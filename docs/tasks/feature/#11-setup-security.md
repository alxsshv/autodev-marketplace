# Задача 11: Настроить Spring Security с JWT

## Название задачи
Настроить Spring Security с JWT аутентификацией и RBAC

## Описание
Настроить Spring Security для защиты API с использованием JWT токенов и Role-Based Access Control (RBAC).

**ПОЧЕМУ:** Security фильтр цепочка должна проверять JWT токены на каждом защищенном endpoint и проверять права доступа.

## Критерии выполнения

- [ ] Создан класс `SecurityConfig` с `@Configuration` и `@EnableWebSecurity`
- [ ] Создан `SecurityFilterChain` bean с настройкой фильтров
- [ ] Настроена JWT Authentication Filter
- [ ] Настроен JWT Access Token валидатор
- [ ] Настроен RBAC (BUYER, SELLER, MODERATOR, ADMIN)
- [ ] Защита Admin endpoints (`hasRole('ADMIN')`)
- [ ] Защита Moderator endpoints (`hasAnyRole('ADMIN', 'MODERATOR')`)
- [ ] Конфигурация openAPI endpoints как `.permitAll()`
- [ ] Настройка черного списка токенов (logout)
- [ ] Обработка исключений (401, 403)

## Конфигурация Security
```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(authz -> authz
            .requestMatchers("/api/v1/auth/login", "/api/v1/auth/refresh").permitAll()
            .requestMatchers("/api/v1/auth/roles/**").hasRole("ADMIN")
            .requestMatchers("/api/v1/auth/users/**").hasAnyRole("ADMIN", "MODERATOR")
            .anyRequest().authenticated()
        )
        .oauth2ResourceServer(oauth2 -> oauth2
            .jwt(jwt -> jwt
                .jwtAuthenticationConverter(jwtAuthenticationConverter())
            )
        );
    return http.build();
}
```

## Ссылки
- [docs/architecture/security-strategy.md](../../docs/architecture/security-strategy.md) - стратегия безопасности
- [docs/architecture/glossary.md](../../docs/architecture/glossary.md) - RBAC определение

## Приоритет
Высокий

## Метки
backend, security, jwt, auth-service
