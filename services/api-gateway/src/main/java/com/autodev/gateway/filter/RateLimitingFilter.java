package com.autodev.gateway.filter;

import com.autodev.gateway.config.RateLimitingProperties;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Глобальный фильтр для ограничения частоты запросов (rate limiting) по IP-адресу.
 * <p>
 * Реализует алгоритм скользящего окна с использованием Redis и Lua-скрипта для
 * атомарного инкремента счётчика и установки TTL. Фильтр применяется ко всем
 * входящим запросам, кроме исключений (health checks и actuator endpoints).
 * </p>
 * <p>
 * Поведение при ошибках (fail-open): если Redis недоступен, запросы пропускаются
 * дальше без ограничения, чтобы не нарушать работу сервиса.
 * </p>
 * <p>
 * Ключи в Redis имеют формат: {@code rate_limit:ip:<ip_address>}
 * </p>
 *
 * @author AutoDev Team
 * @see RateLimitingProperties
 * @see <a href="https://redis.io/commands/incr/">Redis INCR command</a>
 */
@Component
@RequiredArgsConstructor
@Order(-1)
public class RateLimitingFilter implements GlobalFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitingFilter.class);

    private final RedisTemplate<String, String> redisTemplate;
    private final RateLimitingProperties properties;
    private final MeterRegistry meterRegistry;

    private Counter rateLimitExceededCounter;

    /**
     * Lua-скрипт для атомарного увеличения счётчика запросов.
     * <p>
     * Скрипт выполняет следующие операции:
     * 1. Инкрементирует счётчик для ключа IP-адреса
     * 2. Если ключ новый (current == 1), устанавливает TTL
     * 3. Возвращает 0, если лимит не превышен, или текущее значение, если превышен
     * </p>
     */
    private static final String RATE_LIMIT_LUA = """
        local current = redis.call('INCR', KEYS[1])
        if current == 1 then
            redis.call('EXPIRE', KEYS[1], ARGV[2])
        end
        if current > tonumber(ARGV[1]) then
            return tostring(current)
        else
            return '0'
        end
        """;

    private final DefaultRedisScript<String> rateLimitScript = new DefaultRedisScript<>(RATE_LIMIT_LUA, String.class);
    private static final List<String> EXCLUDED_PATHS = List.of("/health", "/actuator");

    @PostConstruct
    public void init() {
        rateLimitExceededCounter = Counter.builder("rate_limit_exceeded")
                .description("Количество превышений лимита запросов")
                .tag("type", "ip")
                .register(meterRegistry);
    }

    /**
     * Фильтрует входящий запрос, применяя ограничение частоты по IP-адресу.
     * <p>
     * Алгоритм работы:
     * 1. Исключает path из фильтрации (health, actuator)
     * 2. Извлекает IP-адрес из заголовка X-Forwarded-For или RemoteAddress
     * 3. Выполняет Lua-скрипт в Redis для инкремента счётчика
     * 4. Если лимит превышен - возвращает 429 TOO_MANY_REQUESTS с заголовком Retry-After
     * 5. При ошибках Redis (fail-open) - пропускает запрос дальше
     * </p>
     *
     * @param exchange контекст HTTP-запроса и ответа
     * @param chain цепочка фильтров шлюза
     * @return пустой Mono при блокировке, иначе результат следующего фильтра
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().pathWithinApplication().value();

        if (isExcludedPath(path)) {
            log.debug("RateLimitingFilter: path {} excluded from rate limiting", path);
            return chain.filter(exchange);
        }

        String ip = extractTrustedIp(exchange);
        if (ip == null || ip.isBlank()) {
            log.warn("RateLimitingFilter: IP address could not be determined for path={}. Fail-open applied.", path);
            return chain.filter(exchange);
        }

        try {
            String key = "rate_limit:ip:" + ip;
            int limit = properties.limitPerIp();
            long ttlSeconds = properties.windowDuration().getSeconds();

            String exceededCountStr = redisTemplate.execute(
                    rateLimitScript,
                    Collections.singletonList(key),
                    String.valueOf(limit),
                    String.valueOf(ttlSeconds)
            );

            Long exceededCount = null;
            if (exceededCountStr != null && !exceededCountStr.isEmpty()) {
                exceededCount = Long.parseLong(exceededCountStr);
            }

            if (exceededCount != null && exceededCount > 0) {
                Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
                long retrySeconds = (ttl != null && ttl > 0) ? ttl : ttlSeconds;

                log.warn("RateLimitingFilter: rate limit exceeded for ip={}, path={}, count={}, retryAfterSeconds={}",
                        ip, path, exceededCount, retrySeconds);

                exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                exchange.getResponse()
                        .getHeaders()
                        .add(HttpHeaders.RETRY_AFTER, String.valueOf(retrySeconds));

                if (rateLimitExceededCounter != null) {
                    rateLimitExceededCounter.increment();
                }
                return Mono.empty();
            }

            log.debug("RateLimitingFilter: rate limit OK for ip={}, path={}", ip, path);
            return chain.filter(exchange);

        } catch (Exception e) {
            log.error("RateLimitingFilter: Redis error while applying rate limit for ip={}, path={}. Fail-open applied.",
                    ip, path, e);
            return chain.filter(exchange);
        }
    }

    /**
     * Проверяет, входит ли путь в список исключённых от rate limiting.
     *
     * @param path путь запроса
     * @return true, если путь исключён
     */
    private boolean isExcludedPath(String path) {
        return EXCLUDED_PATHS.stream().anyMatch(path::startsWith);
    }

    /**
     * Извлекает IP-адрес клиента из заголовка X-Forwarded-For или из RemoteAddress.
     * <p>
     * При наличии заголовка X-Forwarded-For берётся последний IP-адрес (самый ближний
     * к клиенту). Если заголовок отсутствует, используется remoteAddress запроса.
     * </p>
     *
     * @param exchange контекст HTTP-запроса
     * @return IP-адрес клиента или null, если не удалось определить
     */
    private String extractTrustedIp(ServerWebExchange exchange) {
        var forwardedForHeader = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (forwardedForHeader != null && !forwardedForHeader.isBlank()) {
            String[] ips = forwardedForHeader.split(",");
            if (ips.length > 0) {
                return ips[ips.length - 1].trim();
            }
        }

        var remoteAddress = exchange.getRequest().getRemoteAddress();
        if (remoteAddress instanceof InetSocketAddress) {
            InetSocketAddress socketAddress = remoteAddress;
            return socketAddress.getHostString();
        }
        return null;
    }
}
