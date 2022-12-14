/**
 * Define standard Elhub conventions for java platforms (bill of materials) derived from a dependency catalog
 */
package no.elhub.devxp

import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.gradle.kotlin.dsl.maven
import org.gradle.kotlin.dsl.repositories
import org.jfrog.gradle.plugin.artifactory.dsl.PublisherConfig

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
    setContextUrl(project.findProperty("artifactoryUri") ?: "https://jfrog.elhub.cloud/artifactory")
    publish(delegateClosureOf<PublisherConfig> {
        repository(delegateClosureOf<groovy.lang.GroovyObject> {
            setProperty("repoKey", project.findProperty("artifactoryRepository") ?: "elhub-mvn-dev-local")
            setProperty("username", project.findProperty("artifactoryUsername") ?: "nouser")
            setProperty("password", project.findProperty("artifactoryPassword") ?: "nopass")
        })
        defaults(delegateClosureOf<groovy.lang.GroovyObject> {
            invokeMethod("publications", "ALL_PUBLICATIONS")
        })
    })
    resolve(delegateClosureOf<org.jfrog.gradle.plugin.artifactory.dsl.ResolverConfig> {
        setProperty("repoKey", "repo")
    })
}

tasks.withType<GenerateMavenPom> {
    tasks["build"].dependsOn(this@withType)
}

tasks.withType<GenerateModuleMetadata> {
    tasks["build"].dependsOn(this@withType)
}

tasks["publish"].dependsOn(tasks["artifactoryPublish"])
