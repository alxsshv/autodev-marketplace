package com.autodev.gateway.config;

import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Конфигурационные свойства для настройки ограничения частоты запросов (rate limiting).
 * <p>
 * Параметры используются {@link com.autodev.gateway.filter.RateLimitingFilter} для
 * определения лимитов и временных окон для ограничения запросов по IP-адресу.
 * </p>
 * <p>
 * Пример конфигурации в application.yml:
 * </p>
 * <pre>{@code
 * app:
 *   ratelimit:
 *     limit-per-ip: 100
 *     window-duration: 60s
 * }</pre>
 *
 * @param limitPerIp максимальное количество запросов, разрешённых с одного IP-адреса за окно времени
 * @param windowDuration длительность временного окна для подсчёта запросов (по истечении счётчик сбрасывается)
 * @author AutoDev Team
 * @see com.autodev.gateway.filter.RateLimitingFilter
 * @see org.springframework.boot.context.properties.ConfigurationProperties
 */
@ConfigurationProperties(prefix = "app.ratelimit")
public record RateLimitingProperties(

        @Positive
        int limitPerIp,

        @Positive
        Duration windowDuration
) {}
