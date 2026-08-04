import com.github.spotbugs.snom.SpotBugsExtension
import com.github.spotbugs.snom.Effort
import com.github.spotbugs.snom.Confidence
import org.gradle.api.plugins.quality.CheckstyleExtension
import org.gradle.testing.jacoco.tasks.JacocoReport
import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification

plugins {
    java
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("com.github.spotbugs") version "6.0.14" apply false
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "jacoco")
    apply(plugin = "checkstyle")
    apply(plugin = "com.github.spotbugs")

    group = "com.autodev"

    repositories {
        mavenCentral()
    }

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(17)
        }
    }

    // === JACOCO: включаем сбор coverage для ВСЕХ задач Test ===
    tasks.withType<Test>().configureEach {
        extensions.configure<JacocoTaskExtension> {
            isEnabled = true
        }
    }

    // === JACOCO: отчёт агрегирует данные со всех Test-задач ===
    tasks.withType<JacocoReport>().configureEach {
        val allTestTasks = tasks.withType<Test>()
        dependsOn(allTestTasks)
        executionData(fileTree("build/jacoco"))

        reports {
            xml.required = true
            html.required = true
        }
    }

    // === JACOCO: Quality Gate (покрытие считается по всем тестам) ===
    tasks.withType<JacocoCoverageVerification>().configureEach {
        val allTestTasks = tasks.withType<Test>()
        dependsOn(allTestTasks)
        executionData(fileTree("build/jacoco"))

        violationRules {
            rule {
                limit {
                    minimum = "0.60".toBigDecimal()
                }
            }
        }
    }

    // === CHECKSTYLE ===
    configure<CheckstyleExtension> {
        toolVersion = "10.17.0"
        configFile = rootProject.file("config/checkstyle/checkstyle.xml")
        isShowViolations = true
        isIgnoreFailures = false
    }

    // === SPOTBUGS ===
    configure<SpotBugsExtension> {
        effort = Effort.MAX
        reportLevel = Confidence.LOW
    }

    // Подключаем верификацию покрытия к check
    tasks.named("check") {
        dependsOn(tasks.withType<JacocoCoverageVerification>())
    }
}


