import java.time.Duration

val versions = mapOf(
    "liquibase" to "5.0.3",
    "lombok" to "1.18.46",
    "mockito" to "5.11.0",
    "postgresql" to "42.7.3",
    "spring-cloud" to "2024.0.1",
    "spring-boot" to "3.4.5",
    "wiremock-starter" to "3.4.0"
)

plugins {
    java
    id("org.springframework.boot") version "3.4.5"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.liquibase.gradle") version "2.2.1"
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}


sourceSets {
    create("integrationTest") {
        compileClasspath += sourceSets["main"].output + sourceSets["test"].output
        runtimeClasspath += output + compileClasspath

        java {
            srcDir("src/integration-test/java")
        }
        resources {
            srcDir("src/integration-test/resources")
        }
    }
}

configurations {
    named("integrationTestImplementation") {
        extendsFrom(configurations["testImplementation"])
    }
    named("integrationTestRuntimeOnly") {
        extendsFrom(configurations["testRuntimeOnly"])
    }
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${versions["spring-cloud"]}")
    }
}


dependencies {
    //SPRING
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.cloud:spring-cloud-starter-consul-discovery")
    implementation("org.springframework.cloud:spring-cloud-starter-consul-config")
    implementation("org.springframework.kafka:spring-kafka")

    // observability
    implementation("io.micrometer:micrometer-registry-prometheus")

    //DATABASE
    implementation("org.postgresql:postgresql:${versions["postgresql"]}")
    implementation("org.liquibase:liquibase-core:${versions["liquibase"]}")

    //HELPERS
    implementation("org.projectlombok:lombok:${versions["lombok"]}")
    annotationProcessor("org.projectlombok:lombok:${versions["lombok"]}")

    //TEST
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.mockito:mockito-junit-jupiter:${versions["mockito"]}")


    "integrationTestImplementation"("org.testcontainers:testcontainers:1.17.6")
    "integrationTestImplementation"("org.assertj:assertj-core:3.24.2")
    "integrationTestImplementation"("org.testcontainers:testcontainers-postgresql:2.0.3")
    "integrationTestImplementation"("org.testcontainers:junit-jupiter:1.21.4")

}

springBoot {
    mainClass = "com.autodev.auth.AuthApplication"
}

tasks.withType<Test> {
    useJUnitPlatform()
}



tasks.register<Test>("integrationTest") {
    description = "Run integration tests"
    group = "verification"

    // Указываем классы и classpath для интеграционных тестов
    testClassesDirs = sourceSets["integrationTest"].output.classesDirs
    classpath = sourceSets["integrationTest"].runtimeClasspath

    // Запускаем после юнит‑тестов
    shouldRunAfter(tasks.test)

    // Настройка JUnit 5
    useJUnitPlatform()

    // Отчёты о тестировании
    // Исправленная настройка отчётов — используем современный синтаксис Gradle
    reports {
        html.required.set(true)
        junitXml.required.set(true)
    }


    // Таймаут для медленных интеграционных тестов (10 минут)
    timeout.set(Duration.ofMinutes(10))
}

tasks.check {
    dependsOn("integrationTest")
}
