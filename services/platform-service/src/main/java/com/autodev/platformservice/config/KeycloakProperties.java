package com.autodev.platformservice.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "keycloak.admin")
public record KeycloakProperties(
    String serverUrl,
    String realm,
    String clientId,
    @JsonIgnore
    String clientSecret


) {
    @Override
    public String toString() {
        return "KeycloakAdminProperties{" +
                "serverUrl='" + serverUrl + '\'' +
                ", realm='" + realm + '\'' +
                ", clientId='" + clientId + '\'' +
                '}';
    }
}
