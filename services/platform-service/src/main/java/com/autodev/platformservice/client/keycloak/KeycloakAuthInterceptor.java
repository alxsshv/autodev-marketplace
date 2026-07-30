package com.autodev.platformservice.client.keycloak;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class KeycloakAuthInterceptor implements ClientHttpRequestInterceptor {

    private final KeycloakTokenProvider keycloakTokenProvider;

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        String bearerAuthToken = keycloakTokenProvider.getServiceClientAccessToken();
        request.getHeaders().setBearerAuth(bearerAuthToken);
        return execution.execute(request, body);
    }
}
