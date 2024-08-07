import java.net.URI

plugins {
    id("org.springframework.boot") version "3.3.1"
    id("io.spring.dependency-management") version "1.1.5"
    id("io.github.surpsg.delta-coverage") version "2.1.0"
    id("io.gitlab.arturbosch.detekt") version "1.23.6"
    id("com.adarshr.test-logger") version "4.0.0"
    kotlin("jvm") version "1.9.23"
    kotlin("plugin.spring") version "1.9.23"
    `java-test-fixtures`
}

group = "io.github.anki"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
    maven {
        url = URI("https://oss.sonatype.org/content/repositories/snapshots/")
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    developmentOnly("org.springframework.boot:spring-boot-docker-compose")
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.6")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("io.kotest:kotest-assertions-core-jvm:5.0.0")
    testImplementation("io.mockk:mockk:1.13.12")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.10.0")
    testImplementation("org.mockito:mockito-core:5.12.0")
    testImplementation("org.testcontainers:testcontainers:1.20.0")
    testImplementation("org.testcontainers:junit-jupiter:1.20.0")
    testImplementation("org.testcontainers:mongodb:1.20.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testFixturesImplementation("org.springframework.boot:spring-boot-starter-test")
    testFixturesImplementation("org.testcontainers:testcontainers:1.20.0")
    testFixturesImplementation("org.testcontainers:junit-jupiter:1.20.0")
    testFixturesImplementation("org.testcontainers:mongodb:1.20.0")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

detekt {
    config.setFrom("$projectDir/config/detekt.yml")
    autoCorrect = true
}

configure<io.github.surpsg.deltacoverage.gradle.DeltaCoverageConfiguration> {
    diffSource {
        git.compareWith("refs/remotes/origin/master")
    }
    coverageBinaryFiles = allprojects.asSequence()
        .map { subproject ->
            subproject.fileTree(subproject.layout.buildDirectory) {
                setIncludes(listOf("*/**/*.exec"))
            }
        }
        .fold(files()) { all, files ->
            all.from(files)
        }
    violationRules.failIfCoverageLessThan(0.9)
    reports {
        html.set(true)
    }
}
