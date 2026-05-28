# Сборка проекта Gradle'ом
FROM gradle:8.14.5-jdk17-corretto AS builder
WORKDIR /home/gradle/project
COPY build.gradle.kts .
COPY src ./src
COPY gradle.properties .
RUN gradle --no-daemon clean build -x test

# Разбиваем архив по слоям используя layertools
FROM bellsoft/liberica-openjdk-alpine:17-cds AS layers
WORKDIR /application
COPY --from=builder /home/gradle/project/build/libs/*.jar application.jar
RUN java -Djarmode=layertools -jar application.jar extract

# Собираем образ
FROM bellsoft/liberica-openjre-alpine:17-cds AS runtime

WORKDIR /application

COPY --from=layers application/dependencies/ ./
COPY --from=layers application/spring-boot-loader/ ./
COPY --from=layers application/snapshot-dependencies/ ./
COPY --from=layers application/application/ ./


EXPOSE 8080
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
