/**
 * Define standard Elhub conventions for gradle version catalogs
 */
package no.elhub.devxp

import org.gradle.kotlin.dsl.maven
import org.gradle.kotlin.dsl.repositories
import org.jfrog.gradle.plugin.artifactory.dsl.PublisherConfig

plugins {
    id("version-catalog")
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
 * Publishing
 */
publishing {
    publications {
        create<MavenPublication>(project.name) {
            from(components["versionCatalog"])
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

tasks["publish"].dependsOn(tasks["artifactoryPublish"])
