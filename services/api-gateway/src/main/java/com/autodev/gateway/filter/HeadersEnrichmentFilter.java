package com.autodev.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * GlobalFilter для обогащения входящих запросов данными из валидированного JWT токена.
 * <p>
 * Этот фильтр выполняется ДО стандартных фильтров маршрутизации (Order = -100) и извлекает
 * ключевые claims из JWT токена, валидированного API Gateway через Keycloak.
 * <p>
 * <b>Архитектурное примечание по безопасности:</b>
 * <p>
 * Заголовки, добавляемые этим фильтром (X-User-Id, X-User-Email, X-User-Name),
 * предназначены ТОЛЬКО для логирования, аудита и персонализации ответов.
 * <p>
 * <b>Downstream-сервисы НЕ должны доверять этим заголовкам для принятия решений о доступе!</b>
 * <p>
 * Согласно архитектуре AutoDev Marketplace:
 * <ul>
 *   <li>API Gateway выполняет аутентификацию (authentication) - проверяет, что JWT валиден</li>
 *   <li>Downstream-сервисы должны выполнять авторизацию (authorization) - проверять роли из валидированного JWT</li>
 *   <li>Каждый downstream-сервис должен самостоятельно валидировать JWT через Keycloak или Redis кэш</li>
 *   <li>Роли должны извлекаться из валидированного JWT токена, а не из заголовков X-User-Roles</li>
 * </ul>
 * <p>
 * Пример правильной проверки ролей в downstream-сервисе:
 * <pre>{@code
 * @PreAuthorize("hasRole('BUYER')")
 * @GetMapping("/products")
 * public List<Product> getProducts() {
 *     return productService.getAll();
 * }
 * }</pre>
 * <p>
 * Пример НЕПРАВИЛЬНОЙ проверки (нельзя так делать):
 * <pre>{@code
 * @GetMapping("/products")
 * public List<Product> getProducts(@RequestHeader("X-User-Roles") String roles) {
 *     if (roles.contains("BUYER")) {  // ❌ X-User-Roles может быть подделан!
 *         return productService.getAll();
 *     }
 *     throw new AccessDeniedException();
 * }
 * }</pre>
 *
 * @author Системный аналитик
 */
@Component
@Order(-100) // Выполняем ДО стандартных фильтров маршрутизации
public class HeadersEnrichmentFilter implements GlobalFilter {


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return exchange.getPrincipal()
                .filter(Jwt.class::isInstance)
                .cast(Jwt.class)
                .flatMap(jwt -> {
                    var mutatedRequest = exchange.getRequest()
                            .mutate()
                            .header("X-Request-User-Id", jwt.getSubject())
                            .header("X-Request-Email", String.valueOf(jwt.getClaimAsString("email")))
                            .header("X-Request-Name", String.valueOf(jwt.getClaimAsString("name")))
                            .build();
                    return chain.filter(exchange.mutate().request(mutatedRequest).build());
                })
                .switchIfEmpty(Mono.defer(() -> chain.filter(exchange)));

    }
}
