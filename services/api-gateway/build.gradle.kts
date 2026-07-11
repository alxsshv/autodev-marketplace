import java.time.Duration

plugins {
    java
    id("jacoco")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}




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
    implementation(libs.spring.boot.actuator)
    implementation(libs.spring.boot.security)
    implementation(libs.spring.boot.resourceServer)
    implementation(libs.spring.boot.data.redis)
    implementation(libs.spring.cloud.consul)
    implementation(libs.spring.cloud.gateway)

    // RATE LIMITING
    implementation(libs.bundles.resilence4j)
    implementation(libs.resilence4j.ratelimiter)

    // OBSERVABILITY
    implementation(libs.micrometer.prometheus)

    // HELPERS
    implementation(libs.apache.commons.pool)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    //TEST
    testImplementation(libs.spring.boot.test)
    testImplementation(libs.spring.cloud.loadbalancer)
    testImplementation(libs.mockito.junit)
    testImplementation(libs.reactor.test)
    testImplementation(libs.wiremock.spring)
    testRuntimeOnly(libs.bundles.junit.jupiter)

    // INTEGRATION TEST
    integrationTestImplementation(project(":services:api-gateway"))
    integrationTestImplementation(libs.spring.boot.test)
    integrationTestImplementation(libs.spring.boot.security)
    integrationTestImplementation(libs.spring.cloud.stubrunner)
    integrationTestImplementation(libs.spring.cloud.gateway)
    integrationTestImplementation(libs.wiremock.spring)
    integrationTestImplementation(libs.nimbus.jwt)
    integrationTestImplementation(libs.bundles.testcontainers)
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

tasks.register<JacocoReport>("jacocoAggregatedReport") {
    group = "verification"
    description = "Generates aggregated JaCoCo coverage report for all subprojects"

    dependsOn(tasks.test, tasks.named("integrationTest"))

    // Собираем execution data из всех подпроектов
    executionData(fileTree(project.rootDir.absolutePath).include("**/build/jacoco/*.exec"))

    // Указываем исходники и классы для анализа (можно перечислить все подпроекты)
    subprojects.forEach { subproject ->
        subproject.plugins.withType<JavaPlugin> {
            sourceSets(subproject.sourceSets.main.get())
        }
    }

    reports {
        xml.required.set(true)   // для GitLab нужен XML
        html.required.set(true)
        csv.required.set(false)
    }
}
