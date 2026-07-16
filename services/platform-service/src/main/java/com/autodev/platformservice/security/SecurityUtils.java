package com.autodev.platformservice.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.List;

public class SecurityUtils {
    private static final String EMAIL_CLAIM_NAME = "email";

    private SecurityUtils() {
    }


    /**
     * Возвращает идентификатор (subject) текущего аутентифицированного пользователя из JWT.
     *
     * @return subject из JWT токена
     * @throws IllegalStateException если пользователь не аутентифицирован или токен не является JWT
     */
    public static String getCurrentUserId() {
        return getCurrentJwt().getSubject();
    }

    /**
     * Возвращает email текущего аутентифицированного пользователя из JWT.
     *
     * @return значение claim "email" из JWT токена
     * @throws IllegalStateException если пользователь не аутентифицирован или токен не является JWT
     */
    public static String getCurrentUserEmail() {
        return getCurrentJwt().getClaimAsString(EMAIL_CLAIM_NAME);
    }

    /**
     * Возвращает список ролей текущего пользователя без префикса "ROLE_".
     *
     * @return список названий ролей в верхнем регистре
     */
    public static List<String> getCurrentUserRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority().substring(5)) // Отрезаем "ROLE_"
                .toList();
    }

    /**
     * Проверяет, имеет ли текущий пользователь указанную роль (без префикса "ROLE_").
     * Удобно для использования в сервисах, где нельзя поставить {@code @PreAuthorize}.
     *
     * @param role название роли без префикса (регистр не учитывается)
     * @return true, если пользователь обладает данной ролью
     */
    public static boolean hasRole(String role) {
        return getCurrentUserRoles().contains(role.toUpperCase());
    }

    /**
     * Извлекает JWT токен из контекста безопасности.
     *
     * @return текущий JWT токен аутентифицированного пользователя
     * @throws IllegalStateException если аутентификация отсутствует или токен не является JWT
     */
    private static Jwt getCurrentJwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
            return jwtAuthenticationToken.getToken();
        }

        throw new IllegalStateException("Пользователь не аутентифицирован или токен не является JWT");

    }


}
