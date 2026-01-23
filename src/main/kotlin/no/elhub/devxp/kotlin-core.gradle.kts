/* Define core Kotlin project conventions for Elhub.no */
package no.elhub.devxp

import com.adarshr.gradle.testlogger.theme.ThemeType
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import no.elhub.devxp.coverage.CoverageReporter
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jfrog.gradle.plugin.artifactory.dsl.ArtifactoryPluginConvention
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType
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
    id("org.jlleitschuh.gradle.ktlint")
    id("org.owasp.dependencycheck")
    id("org.jetbrains.dokka")
    id("com.jfrog.artifactory")
    id("maven-publish")
}

/* Project should use the Elhub artifactory instance  */
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

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
}

jacoco {
    toolVersion = "0.8.14" // Has to be the same as TeamCity
}

tasks.jacocoTestReport {
    dependsOn(tasks.test) // tests are required to run before generating the report
    finalizedBy(tasks["printCoverage"])
    reports {
        xml.required.set(true)
    }
}

tasks.register("printCoverage") {
    dependsOn(tasks.jacocoTestReport)
    doLast {
        CoverageReporter(project).generateReport()
    }
}

testlogger {
    theme = ThemeType.MOCHA
    showStandardStreams = true
    showPassedStandardStreams = false
    showSkippedStandardStreams = false
    showFailedStandardStreams = true
}

/*
 * Publishing
 */
artifactory {
    clientConfig.isIncludeEnvVars = true

    publish {
        contextUrl = project.findProperty("artifactoryUri")?.toString() ?: "https://jfrog.elhub.cloud/artifactory"
        repository {
            repoKey = project.findProperty("artifactoryRepository")?.toString() ?: "elhub-mvn-dev-local"
            username = project.findProperty("artifactoryUsername")?.toString() ?: "nouser" // The publisher user name
            password = project.findProperty("artifactoryPassword")?.toString() ?: "nopass" // The publisher password
        }

        defaults {
            publications("ALL_PUBLICATIONS")
            setPublishArtifacts(true)
            setPublishIvy(false) // Publish generated Ivy descriptor files to Artifactory (true by default)
        }
    }
}

tasks["publish"].dependsOn(tasks["artifactoryPublish"])

/*
 * Versions dependency checker
 */
tasks.withType<DependencyUpdatesTask> {
    rejectVersionIf {
        isNonStable(candidate.version) && !isNonStable(currentVersion)
    }
}

/*
 * Ktlint
 */
configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
    version.set("1.8.0") // Pick a recent compatible version
    debug.set(false)
    verbose.set(true)
    android.set(false)
    outputToConsole.set(true)
    outputColorName.set("RED")
    ignoreFailures.set(false) // Fail build if style violations are found
    enableExperimentalRules.set(false)

    reporters {
        reporter(ReporterType.PLAIN)
        reporter(ReporterType.CHECKSTYLE) // Useful for CI/TeamCity parsing
    }
}

/*
 * Dependency Check Plugin
 */
dependencyCheck {
    formats = listOf(ReportGenerator.Format.JSON.name, ReportGenerator.Format.HTML.name)
    // if (project.hasProperty("dependencyCheck.suppressionFile")) {
    //    suppressionFile = project.property("dependencyCheck.suppressionFile").toString()
    // }
    analyzers {
        // Teamcity agents running .NET version too old for .NET Assembly Analyzer. Needs to be disabled until agents are updated
        assemblyEnabled = false
        // Needs to be disabled until "search.maven.org" is whitelisted in squid. Owasp 12.1.0 uses this to populate artifact
        // metadata for better detection, but the effect should be minimal
        centralEnabled = false
        nodeAudit {
            yarnEnabled = false
        }
        retirejs {
            enabled = false
        }
        ossIndex {
            username = System.getenv("SONATYPE_USERNAME")
            password = System.getenv("SONATYPE_API_TOKEN")
        }
    }
    nvd {
        apiKey = System.getenv("NVD_API_KEY")
        // Only use custom datafeed URL in CI/CD environments
        if (System.getenv("CI") != null || System.getenv("TEAMCITY_VERSION") != null) {
            datafeedUrl = "https://owasp.elhub.cloud"
        }
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
dokka {
    dokkaSourceSets.main {
        includes.from("module.md")
        sourceLink {
            localDirectory.set(file("src/main/kotlin"))
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
