package com.autodev.platformservice.client.keycloak;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * REST-интерцептор для автоматического добавления токена аутентификации Keycloak
 * в заголовки исходящих HTTP-запросов.
 * <p>
 * Используется как компонент в {@link org.springframework.web.client.RestTemplate}
 * или {@link org.springframework.web.client.RestClient} для проксирования запросов
 * к административному API Keycloak. При каждом запросе интерцептор получает
 * сервисный Bearer-токен через {@link KeycloakTokenProvider} и добавляет его
 * в заголовок {@code Authorization}.
 * <p>
 * <b>Пример использования:</b>
 * <pre>{@code
 * RestClient.builder()
 *     .requestFactory(new JdkClientHttpRequestFactory())
 *     .requestInterceptors(List.of(keycloakAuthInterceptor))
 *     .build();
 * }</pre>
 *
 * @see KeycloakTokenProvider
 * @see ClientHttpRequestInterceptor
 */
@Component
@RequiredArgsConstructor
public class KeycloakAuthInterceptor implements ClientHttpRequestInterceptor {

    /**
     * Провайдер для получения сервисного Bearer-токена от Keycloak.
     * Инъектируется через Spring.
     */
    private final KeycloakTokenProvider keycloakTokenProvider;

    /**
     * Выполняет перехват запроса, добавляя Bearer-токен в заголовок Authorization.
     * <p>
     * <b>Алгоритм:</b>
     * <ol>
     *   <li>Получает сервисный клиент-токен через {@link KeycloakTokenProvider#getServiceClientAccessToken()}.</li>
     *   <li>Добавляет токен в заголовок {@code Authorization} запроса методом {@code Bearer}.</li>
     *   <li>Передаёт управление следующему интерцептору или исполнителю запроса.</li>
     * </ol>
     *
     * @param request HTTP-запрос, к которому добавляется токен
     * @param body тело запроса (байтовый массив)
     * @param execution объект для выполнения запроса (передаёт управление следующему интерцептору)
     * @return HTTP-ответ от Keycloak
     * @throws IOException если возникла ошибка ввода-вывода при выполнении запроса
     */
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        String bearerAuthToken = keycloakTokenProvider.getServiceClientAccessToken();
        request.getHeaders().setBearerAuth(bearerAuthToken);
        return execution.execute(request, body);
    }
}
