/**
 * Define core Kotlin project conventions for Elhub.no
 */
package no.elhub.devxp

import com.adarshr.gradle.testlogger.theme.ThemeType
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.owasp.dependencycheck.gradle.extension.AnalyzerExtension
import org.owasp.dependencycheck.gradle.extension.RetireJSExtension
import org.owasp.dependencycheck.gradle.tasks.AbstractAnalyze
import org.owasp.dependencycheck.gradle.tasks.Aggregate
import org.owasp.dependencycheck.gradle.tasks.Analyze
import org.owasp.dependencycheck.reporting.ReportGenerator

plugins {
    kotlin("jvm")
    id("com.github.ben-manes.versions")
    id("jacoco")
    id("com.adarshr.test-logger")
    id("org.owasp.dependencycheck")
    id("org.jetbrains.dokka")
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
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "17"
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
    reports {
        xml.required.set(true)
    }
}

testlogger {
    theme = ThemeType.MOCHA
}

/*
 * Versions dependency checker
 */
fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.toUpperCase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}

tasks.withType<DependencyUpdatesTask> {
    rejectVersionIf {
        isNonStable(candidate.version) && !isNonStable(currentVersion)
    }
}


/*
 * Dependency Check PLugin
 */
dependencyCheck {
    formats = listOf(
        ReportGenerator.Format.JSON.name,
        ReportGenerator.Format.HTML.name,
    )
    analyzers(delegateClosureOf<AnalyzerExtension> {
        retirejs(delegateClosureOf<RetireJSExtension> {
            enabled = false
        })
    })
}

tasks.withType<Analyze> {
    setCustomConfiguration()
}

tasks.withType<Aggregate> {
    setCustomConfiguration()
}

fun AbstractAnalyze.setCustomConfiguration() {
    doFirst {
        val proxyHost = project.findProperty("proxyHost")
        val proxyPort = project.findProperty("proxyPort")
        val nonProxyHosts = project.findProperty("nonProxyHosts")
        listOf("http", "https").forEach {
            if (proxyHost != null && proxyPort != null) {
                System.setProperty("$it.proxyHost", proxyHost.toString())
                System.setProperty("$it.proxyPort", proxyPort.toString())
            }
            if (nonProxyHosts != null) {
                System.setProperty("$it.nonProxyHosts", nonProxyHosts.toString())
            }
        }
    }
    doLast {
        listOf("http", "https").forEach {
            System.clearProperty("$it.proxyPort")
            System.clearProperty("$it.proxyHost")
            System.clearProperty("$it.nonProxyHosts")
        }
    }
}


/*
 * Dokka
 */
tasks.withType<DokkaTask>().configureEach {
    dokkaSourceSets {
        named("main") {
            // Files containing module documentation
            // Note files must exist (missing files break the build)
            includes.from("module.md")
            // Emit warnings for undocumented code
            reportUndocumented.set(true)
        }
    }
}


/*
 * TeamCity
 */
tasks.register("teamCity", Exec::class) {
    description = "Compile the TeamCity settings"
    workingDir(".teamcity")
    commandLine("mvn","compile")
}
