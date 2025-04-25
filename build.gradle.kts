import com.adarshr.gradle.testlogger.theme.ThemeType
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.owasp.dependencycheck.gradle.tasks.AbstractAnalyze
import org.owasp.dependencycheck.gradle.tasks.Aggregate
import org.owasp.dependencycheck.gradle.tasks.Analyze
import org.owasp.dependencycheck.reporting.ReportGenerator

plugins {
    alias(libs.plugins.kotlin.dsl)
    alias(libs.plugins.version.gradle.versions)
    id("jacoco")
    alias(libs.plugins.test.logger)
    alias(libs.plugins.owasp.dependency.check)
    alias(libs.plugins.build.artifactory)
    id("maven-publish") apply true
}

repositories {
    maven(url = "https://jfrog.elhub.cloud/artifactory/elhub-mvn")
}

group = "no.elhub.devxp"

/** Classpaths of plugins used in the elhub-gradle-plugins need to be defined in the dependencies to ensure they
 *  are available when building the plugin Jar.
 */
dependencies {
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.version.gradle.versions.plugin)
    implementation(libs.test.logger.plugin)
    implementation(libs.build.jfrog.build.info)
    implementation(libs.build.shadow.plugin)
    implementation(libs.owasp.dependency.check)
    implementation(libs.docs.dokka.plugin)
    testImplementation(libs.test.kotest.runner.junit5)
    testImplementation(libs.apache.commons.io)
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
    toolVersion = "0.8.13" // Has to be the same as TeamCity
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
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
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
 * Dependency Check Plugin
 */
dependencyCheck {
    formats = listOf(ReportGenerator.Format.JSON.toString(), ReportGenerator.Format.HTML.toString())
    analyzers {
        retirejs {
            enabled = false
        }
    }
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
            setPublishPom(true) // Publish generated POM files to Artifactory (true by default)
            setPublishIvy(false) // Publish generated Ivy descriptor files to Artifactory (true by default)
        }
    }
}

tasks["publish"].dependsOn(tasks["artifactoryPublish"])

/*
 * TeamCity
 */
tasks.register("teamcityCheck", Exec::class) {
    group = "teamcity"
    description = "Compile the TeamCity settings"
    workingDir(".teamcity")
    commandLine("mvn", "teamcity-configs:generate")
}
