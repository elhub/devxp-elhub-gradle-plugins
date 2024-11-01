/**
 * Define standard Elhub conventions for java platforms (bill of materials) derived from a dependency catalog
 */
package no.elhub.devxp

import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import java.util.Locale

plugins {
    id("java-platform")
    id("com.github.ben-manes.versions")
    id("com.jfrog.artifactory")
    id("maven-publish")
}

/** Project should use the Elhub artifactory instance
 */
repositories {
    maven(url = "https://jfrog.elhub.cloud:443/artifactory/elhub-mvn/")
}

/*
 * Prevent Gradle warnings
 */
tasks.withType<Test> {
    // Empty test task
}

tasks.withType<Jar> {
    enabled = false
}

/*
 * Versions update checker
 */
fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase(Locale.getDefault()).contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}

tasks.withType<DependencyUpdatesTask> {
    rejectVersionIf {
        isNonStable(candidate.version) && !isNonStable(currentVersion)
    }
}

tasks.named<DependencyUpdatesTask>("dependencyUpdates").configure {
    checkConstraints = true
    checkBuildEnvironmentConstraints = true
}

/*
 * Publishing
 */
publishing {
    publications {
        create<MavenPublication>(project.name) {
            from(components["javaPlatform"])
        }
    }
}

artifactory {
    clientConfig.isIncludeEnvVars = true

    publish {
        contextUrl = project.findProperty("artifactoryUri")?.toString() ?: "https://jfrog.elhub.cloud/artifactory"
        repository {
            repoKey = project.findProperty("artifactoryRepository")?.toString() ?: "elhub-mvn-dev-local"
            username = project.findProperty("artifactoryUsername")?.toString() ?: "nouser" // The publisher username
            password = project.findProperty("artifactoryPassword")?.toString() ?: "nopass" // The publisher password
        }

        defaults {
            publications("mavenJava")
            setPublishArtifacts(true)
            setPublishPom(true) // Publish generated POM files to Artifactory (true by default)
            setPublishIvy(false) // Publish generated Ivy descriptor files to Artifactory (true by default)
        }
    }
}

tasks.withType<GenerateMavenPom> {
    tasks["build"].dependsOn(this@withType)
}

tasks.withType<GenerateModuleMetadata> {
    tasks["build"].dependsOn(this@withType)
}

tasks["publish"].dependsOn(tasks["artifactoryPublish"])
