/**
 * Define core Kotlin project conventions for Elhub.no
 */
package no.elhub.devxp

import com.adarshr.gradle.testlogger.theme.ThemeType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("com.github.ben-manes.versions")
    id("jacoco")
    id("com.adarshr.test-logger")
}

/** Project should use the Elhub artifactory instance
 */
repositories {
    maven(url = "https://jfrog.elhub.cloud/artifactory/elhub-mvn/")
}

/*
 * Compile setup
 */
java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "1.8"
        javaParameters = true
    }
}


/*
 * Test setup
 */
tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = true
    }
}

jacoco {
    toolVersion = "0.8.7" // Has to be the same as TeamCity
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport) // report is always generated after tests run
}

tasks.jacocoTestReport {
    dependsOn(tasks.test) // tests are required to run before generating the report
}

testlogger {
    theme = ThemeType.MOCHA
}


/*
 * TeamCity
 */
tasks.register("teamCity", Exec::class) {
    description = "Compile the TeamCity settings"
    workingDir(".teamcity")
    commandLine("mvn","compile")
}