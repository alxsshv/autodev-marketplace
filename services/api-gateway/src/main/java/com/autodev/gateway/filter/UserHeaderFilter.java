package com.autodev.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

public class UserHeaderFilter extends AbstractGatewayFilterFactory<UserHeaderFilter> {

    @Override
    public GatewayFilter apply(UserHeaderFilter config) {
        return (exchange, chain) -> ReactiveSecurityContextHolder.getContext()
                .map(securityContext -> securityContext.getAuthentication().getPrincipal())
                .cast(Jwt.class)
                .defaultIfEmpty(null)
                .flatMap(jwt -> {
                    ServerHttpRequest.Builder builder = exchange.getRequest().mutate();

                    if (jwt != null) {
                        // X-User-ID = sub
                        String userId = jwt.getSubject();
                        builder.header("X-User-ID", userId);

                        // X-Roles = realm_access.roles
                        List<String> roles = jwt.getClaimAsStringList("realm_access.roles");
                        if (roles != null) {
                            builder.header("X-Roles", String.join(",", roles));
                        }
                    }

                    return chain.filter(exchange.mutate().request(builder.build()).build());
                });
    }
}
