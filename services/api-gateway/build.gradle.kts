val versions = mapOf(
    "wiremock-starter" to "3.4.0",
    "mockito" to "5.11.0"
)

plugins {
    java
    id("org.springframework.boot") version "3.4.5"
    id("io.spring.dependency-management") version "1.1.7"
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
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
}


dependencies {
    //SPRING
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.cloud:spring-cloud-starter-consul-discovery")
    implementation("org.springframework.cloud:spring-cloud-starter-gateway")

    // observability
    implementation("io.micrometer:micrometer-registry-prometheus")

    //TEST
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.mockito:mockito-junit-jupiter:${versions["mockito"]}")
    testImplementation("org.springframework.cloud:spring-cloud-starter-loadbalancer" )
    testImplementation("org.wiremock.integrations:wiremock-spring-boot:${versions["wiremock-starter"]}")

}

springBoot {
    mainClass = "com.autodev.gateway.GatewayApplication"
}

tasks.withType<Test> {
    useJUnitPlatform()
}