import com.adarshr.gradle.testlogger.theme.ThemeType
import groovy.lang.GroovyObject
import org.jfrog.gradle.plugin.artifactory.dsl.PublisherConfig
import org.jfrog.gradle.plugin.artifactory.dsl.ResolverConfig
import org.owasp.dependencycheck.gradle.extension.AnalyzerExtension
import org.owasp.dependencycheck.gradle.extension.RetireJSExtension
import org.owasp.dependencycheck.gradle.tasks.Analyze
import org.owasp.dependencycheck.reporting.ReportGenerator

plugins {
    `kotlin-dsl`
    alias(libs.plugins.version.gradle.versions)
    id("jacoco")
    alias(libs.plugins.test.logger)
    id("org.owasp.dependencycheck") version "7.4.4"
    alias(libs.plugins.build.artifactory)
    id("maven-publish") apply true
}

repositories {
    maven(url = "https://jfrog.elhub.cloud/artifactory/elhub-mvn")
    maven(url = "https://jfrog.elhub.cloud/artifactory/elhub-plugins")
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
 * Dependency Check Plugin
 */

dependencyCheck {
    format = ReportGenerator.Format.JSON
    analyzers(delegateClosureOf<AnalyzerExtension> {
        retirejs(delegateClosureOf<RetireJSExtension> {
            enabled = false
        })
    })
}

tasks.withType<Analyze> {
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
publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}

artifactory {
    setContextUrl(project.findProperty("artifactoryUri") ?: "https://jfrog.elhub.cloud/artifactory")
    publish(delegateClosureOf<PublisherConfig> {
        repository(delegateClosureOf<GroovyObject> {
            setProperty("repoKey", project.findProperty("artifactoryRepository") ?: "elhub-plugins-dev-local")
            setProperty("username", project.findProperty("artifactoryUsername") ?: "nouser")
            setProperty("password", project.findProperty("artifactoryPassword") ?: "nopass")
        })
        defaults(delegateClosureOf<GroovyObject> {
            invokeMethod("publications", "ALL_PUBLICATIONS")
        })
    })
    resolve(delegateClosureOf<ResolverConfig> {
        setProperty("repoKey", "repo")
    })
}

tasks["publish"].dependsOn(tasks["artifactoryPublish"])

/*
 * TeamCity
 */
tasks.register("teamCity", Exec::class) {
    description = "Compile the TeamCity settings"
    workingDir(".teamcity")
    commandLine("mvn", "compile")
}
