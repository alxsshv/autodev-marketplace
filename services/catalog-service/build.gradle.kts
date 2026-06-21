val versions = mapOf(
    "liquibase" to "5.0.3",
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

    //TEST
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.mockito:mockito-junit-jupiter:${versions["mockito"]}")
}

springBoot {
    mainClass = "com.autodev.platform.CatalogApplication"
}

tasks.withType<Test> {
    useJUnitPlatform()
}