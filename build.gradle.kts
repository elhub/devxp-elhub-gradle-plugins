import com.adarshr.gradle.testlogger.theme.ThemeType
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.withType
import org.jetbrains.dokka.gradle.DokkaTask
import org.owasp.dependencycheck.gradle.tasks.AbstractAnalyze
import org.owasp.dependencycheck.gradle.tasks.Aggregate
import org.owasp.dependencycheck.gradle.tasks.Analyze
import org.owasp.dependencycheck.reporting.ReportGenerator
import org.w3c.dom.Document
import javax.xml.parsers.DocumentBuilderFactory

plugins {
    `kotlin-dsl`
    alias(libs.plugins.version.gradle.versions)
    alias(libs.plugins.test.jacoco)
    alias(libs.plugins.test.logger)
    alias(libs.plugins.owasp.dependency.check)
    alias(libs.plugins.build.artifactory)
    alias(libs.plugins.maven.publish) apply true
    alias(libs.plugins.gradle.jib)
    alias(libs.plugins.dokka)
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
    implementation(libs.gradle.jib.plugin)
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
    }
}

jacoco {
    toolVersion = libs.versions.jacoco.get().toString()
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
        var totalCovered = 0.toBigInteger()
        var totalMissed = 0.toBigInteger()

        val projectsToCheck = if (subprojects.isEmpty()) listOf(project) else subprojects

        projectsToCheck.forEach { proj ->
            val reportFile = proj.file("build/reports/jacoco/test/jacocoTestReport.xml")
            if (!reportFile.exists()) return@forEach

            val doc = parseJacocoXml(reportFile)
            val counters = doc.getElementsByTagName("counter")

            for (i in 0 until counters.length) {
                val node = counters.item(i)
                val attrs = node.attributes
                val type = attrs.getNamedItem("type").nodeValue
                if (type == "INSTRUCTION") {
                    val covered = attrs.getNamedItem("covered").nodeValue.toBigInteger()
                    val missed = attrs.getNamedItem("missed").nodeValue.toBigInteger()
                    totalCovered += covered
                    totalMissed += missed
                }
            }
        }

        val total = totalCovered + totalMissed
        val coveragePercent = if (total > BigInteger.ZERO) {
            ((totalCovered * 100.toBigInteger()) / total).toInt()
        } else {
            0
        }

        val reset = "\u001B[0m"
        val color = when {
            coveragePercent >= 80 -> "\u001B[32m"
            else -> "\u001B[31m"
        }
        println("$color═════════════════════════════════════════$reset")
        println("$color> JaCoCO Test Report $reset")
        println("$color> Instruction Coverage: $coveragePercent% $reset")
        println("$color═════════════════════════════════════════$reset")
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
fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}

tasks.withType<DependencyUpdatesTask> {
    notCompatibleWithConfigurationCache("Unsupported task invocations.")
    rejectVersionIf {
        isNonStable(candidate.version) && !isNonStable(currentVersion)
    }
}

tasks.named("dependencyUpdates") {
    notCompatibleWithConfigurationCache("Unsupported task invocations.")
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
        ossIndex {
            username = System.getenv("SONATYPE_USERNAME")
            password = System.getenv("SONATYPE_API_TOKEN")
        }
    }
    nvd {
        datafeedUrl = "https://owasp.elhub.cloud"
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
            setPublishIvy(false)
        }
    }
}

tasks["publish"].dependsOn(tasks["artifactoryPublish"])

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
    description = "Compile the TeamCity settings"
    workingDir(".teamcity")
    commandLine("mvn", "teamcity-configs:generate")
}
