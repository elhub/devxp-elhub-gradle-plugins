/**
 * Define core Kotlin project conventions for Elhub.no
 */
package no.elhub.devxp

import com.adarshr.gradle.testlogger.theme.ThemeType
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import no.elhub.devxp.coverage.CoverageReporter
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.owasp.dependencycheck.gradle.tasks.Aggregate
import org.owasp.dependencycheck.gradle.tasks.Analyze
import org.owasp.dependencycheck.reporting.ReportGenerator
import org.w3c.dom.Document
import javax.xml.parsers.DocumentBuilderFactory

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

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
        javaParameters.set(true)
    }
}

/*
 * Test setup
 */
tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

jacoco {
    toolVersion = "0.8.14" // Has to be the same as TeamCity
}

tasks.jacocoTestReport {
    dependsOn(tasks.test) // tests are required to run before generating the report
    reports {
        xml.required.set(true)
    }
}

fun parseJacocoXml(file: File): Document {
    val factory = DocumentBuilderFactory.newInstance()
    factory.isValidating = false
    factory.isNamespaceAware = true
    factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false) // Prevent external DTD fetching
    val builder = factory.newDocumentBuilder()
    return builder.parse(file)
}

tasks.register("printCoverage") {
    doLast {
        CoverageReporter(project).generateReport()
    }
}

tasks.test {
    // Tests are always followed by jacoco report and printCoverage
    finalizedBy(tasks.jacocoTestReport, tasks["printCoverage"])
}

testlogger {
    theme = ThemeType.MOCHA
    showStandardStreams = true
    showPassedStandardStreams = false
    showSkippedStandardStreams = false
    showFailedStandardStreams = true
}

/*
 * Versions dependency checker
 */
tasks.withType<DependencyUpdatesTask> {
    rejectVersionIf {
        isNonStable(candidate.version) && !isNonStable(currentVersion)
    }
}

/*
 * Dependency Check Plugin
 */
dependencyCheck {
    formats = listOf(ReportGenerator.Format.JSON.name, ReportGenerator.Format.HTML.name)
    analyzers {
        // Teamcity agents running .NET version too old for .NET Assembly Analyzer. Needs to be disabled until agents are updated
        assemblyEnabled = false
        // Needs to be disabled until "search.maven.org" is whitelisted in squid. Owasp 12.1.0 uses this to populate artifact
        // metadata for better detection, but the effect should be minimal
        centralEnabled = false
        retirejs {
            enabled = false
        }
        ossIndex {
            username = System.getenv("SONATYPE_USERNAME")
            password = System.getenv("SONATYPE_API_TOKEN")
        }
    }
    nvd {
        // Pick up the NVD_API_KEY from the environment
        apiKey = System.getenv("NVD_API_KEY")
        // Fetch vulnerability data from Elhub's OWASP instance
        // Comment out as build config is not passing non_proxy_hosts variable which causing dependency check to fail
        datafeedUrl = "https://owasp.elhub.cloud"
    }
}

tasks.withType<Analyze> {
    setCustomConfiguration()
}

tasks.withType<Aggregate> {
    setCustomConfiguration()
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
tasks.register("teamcityCheck", Exec::class) {
    group = "teamcity"
    description = "Compile the TeamCity settings."
    workingDir(".teamcity")
    commandLine("mvn", "teamcity-configs:generate")
}
