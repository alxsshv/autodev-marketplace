

rootProject.name = "autodev-marketplace"
include("services:api-gateway")
include("services:platform-service")
include("services:catalog-service")
include("services:order-service")
include("services:payment-service")
include("services:communication-service")
include("services:listing-service")
include("services:notification-service")


// Управление версиями плагинов
pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
    // Централизуем версии плагинов здесь
    plugins {
        id("org.springframework.boot") version "3.4.5"
        id("io.spring.dependency-management") version "1.1.7"
    }
}

// Управление версиями зависимостей
//dependencyResolutionManagement {
//    versionCatalogs {
//        create("libs") {
//            from(files("gradle/libs.versions.toml"))
//        }
//    }
//}
