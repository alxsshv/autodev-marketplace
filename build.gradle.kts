import java.time.Duration

plugins {
    java
    id("org.springframework.boot") version "3.4.5"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.autodev"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
}

// Настройка source sets для интеграционных тестов
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
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
}


dependencies {
    //STARTERS
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-registry-prometheus")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Интеграционные тесты
    "integrationTestImplementation"(project)
    "integrationTestImplementation"("org.springframework.boot:spring-boot-starter-test")
    "integrationTestRuntimeOnly"("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// Задача для интеграционного тестирования
tasks.register<Test>("integrationTest") {
    group = "verification"
    description = "Runs integration tests..."
    testClassesDirs = sourceSets["integrationTest"].output.classesDirs
    classpath = sourceSets["integrationTest"].runtimeClasspath

    useJUnitPlatform()
    shouldRunAfter(tasks.test)


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
