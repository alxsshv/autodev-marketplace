# === Этап 1: Извлечение слоёв Spring Boot ===
FROM bellsoft/liberica-openjdk-alpine:17-cds AS layers
WORKDIR /application

# Копируем СОБРАННЫЙ bootJar из CI (артефакт)
# Убедитесь, что в CI сначала выполнена ./gradlew :service-a:bootJar
COPY services/api-gateway/build/libs/*-boot.jar application.jar
RUN java -Djarmode=layertools -jar application.jar extract

# === Этап 2: Runtime (только JRE) ===
FROM bellsoft/liberica-openjre-alpine:17-cds AS runtime
WORKDIR /application

# Создаём непривилегированного пользователя
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Копируем слои отдельно — Docker кэширует их независимо
COPY --from=layers application/dependencies/ ./
COPY --from=layers application/spring-boot-loader/ ./
COPY --from=layers application/snapshot-dependencies/ ./
COPY --from=layers application/application/ ./

# Меняем владельца и переключаемся на непривилегированного пользователя
RUN chown -R appuser:appgroup /application
USER appuser

EXPOSE 8080

# Healthcheck (требует spring-boot-starter-actuator)
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]