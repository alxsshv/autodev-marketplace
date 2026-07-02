plugins {
    java
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

subprojects {
    apply(plugin = "java")

    group = "com.autodev"
    version = "1.0.0"

    repositories {
        mavenCentral()
    }

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(17)
        }
    }

}


