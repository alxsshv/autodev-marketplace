package com.autodev.gateway;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Конфигурация тестового контейнера для Redis.
 * <p>
 * Класс управляет singleton-экземпляром контейнера Redis с использованием Testcontainers
 * для целей интеграционного тестирования. Контейнер запускается один раз при инициализации
 * класса и предоставляет доступ к хосту и порту контейнера.
 * </p>
 * <p>
 * Пример использования:
 * </p>
 * <pre>{@code
 * String host = TestRedisContainer.getHost();
 * Integer port = TestRedisContainer.getPort();
 * // Используйте host и port для настройки клиента Redis в тестах
 * }</pre>
 *
 * @author AutoDev Team
 * @version 1.0
 * @see org.testcontainers.containers.GenericContainer
 * @see org.testcontainers.utility.DockerImageName
 */

public final class TestRedisContainer {

    private TestRedisContainer() {
    }

    private static final GenericContainer<?> REDIS;

    static {
        REDIS = new GenericContainer<>(DockerImageName.parse("redis:5.0.3-alpine"))
                .withExposedPorts(6379);
        REDIS.start();
    }

    /**
     * Возвращает имя хоста контейнера Redis.
     *
     * @return имя хоста контейнера
     */
    public static String getHost() {
        return REDIS.getHost();
    }

    /**
     * Возвращает сопоставленный порт контейнера Redis.
     * <p>
     * Метод возвращает динамически назначенный порт на хосте, который сопоставлен
     * с портом Redis внутри контейнера (6379).
     * </p>
     *
     * @return порт хоста, сопоставленный с портом 6379 контейнера Redis
     */
    public static Integer getPort() {
        return REDIS.getMappedPort(6379);
    }
}