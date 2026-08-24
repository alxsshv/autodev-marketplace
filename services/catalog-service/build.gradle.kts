import java.time.Duration

plugins {
    java
    id("jacoco")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

version = project.findProperty("releaseVersion")?.toString() ?: "1.0.0-SNAPSHOT"


sourceSets {
    create("integrationTest") {
        java {
            srcDir("src/integration-test/java")
        }
        resources {
            srcDir("src/integration-test/resources")
        }
    }
}

val integrationTestImplementation = configurations.getByName("integrationTestImplementation") {
    extendsFrom(configurations.implementation.get())
}

val integrationTestRuntimeOnly = configurations.getByName("integrationTestRuntimeOnly") {
    extendsFrom(configurations.runtimeOnly.get())
}

val springCloudVersion = libs.versions.spring.cloud.get()

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:$springCloudVersion")
    }
}


dependencies {
    // SPRING
    implementation(libs.spring.boot.web)
    implementation(libs.spring.boot.data.jpa)
    implementation(libs.spring.boot.data.redis)
    implementation(libs.spring.boot.actuator)
    implementation(libs.spring.boot.security)
    implementation(libs.spring.boot.resourceServer)
    implementation(libs.spring.boot.validation)
    implementation(libs.spring.cloud.consul)
    implementation(libs.spring.kafka)

    // DATABASES
    implementation(libs.liquibase)
    implementation(libs.postgres)


    // RESILENCE4J


    // OBSERVABILITY


    // HELPERS
    implementation(libs.apache.commons.pool)
    implementation(libs.mapstruct)
    implementation(libs.lombok.mapstruct.binding)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    annotationProcessor(libs.mapstruct.processor)

    //TEST
    testImplementation(libs.spring.boot.test)
    testImplementation(libs.mockito.junit)
    testRuntimeOnly(libs.bundles.junit.jupiter)

    // INTEGRATION TEST
    integrationTestImplementation(project(":services:catalog-service"))
    integrationTestImplementation(libs.spring.boot.test)
    integrationTestImplementation(libs.spring.boot.security)
    integrationTestImplementation(libs.wiremock)
    integrationTestImplementation(libs.bundles.testcontainers)
    integrationTestImplementation(libs.testcontainers.postgresql)
    integrationTestImplementation(libs.testcontainers.kafka)
    integrationTestImplementation(libs.redis.testcontainers)
    integrationTestImplementation(libs.bundles.junit.jupiter)

}


tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.register<Test>("integrationTest") {
    group = "verification"
    description = "Runs integration tests..."

    useJUnitPlatform()
    shouldRunAfter(tasks.test)

    testClassesDirs = sourceSets["integrationTest"].output.classesDirs
    classpath = sourceSets["integrationTest"].runtimeClasspath

    reports {
        html.required.set(true)
        junitXml.required.set(true)
    }
    timeout.set(Duration.ofMinutes(10))
}

tasks.check {
    dependsOn("integrationTest")
}