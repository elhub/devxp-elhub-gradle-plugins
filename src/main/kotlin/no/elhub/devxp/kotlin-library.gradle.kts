/**
 * Define standard Elhub conventions for Kotlin libraries
 */
package no.elhub.devxp

import org.jfrog.gradle.plugin.artifactory.dsl.PublisherConfig

plugins {
    id("no.elhub.devxp.kotlin-core")
    id("com.jfrog.artifactory")
    id("maven-publish")
}

/*
 * Publishing
 */
val repository = project.findProperty("artifactoryRepository") ?: "elhub-mvn-dev-local"

publishing {
    repositories {
        maven {
            url = uri(
                "https://jfrog.elhub.cloud/artifactory/" + repository
            )
        }
    }
}

artifactory {
    setContextUrl("https://jfrog.elhub.cloud/artifactory")
    publish(delegateClosureOf<PublisherConfig> {
        repository(delegateClosureOf<groovy.lang.GroovyObject> {
            setProperty("repoKey", repository)
            setProperty("username", project.findProperty("artifactoryUsername") ?: "nouser")
            setProperty("password", project.findProperty("artifactoryPassword") ?: "nopass")
        })
        defaults(delegateClosureOf<groovy.lang.GroovyObject> {
            setProperty("publishArtifacts", true)
            setProperty("publishPom", false)
        })
    })
    resolve(delegateClosureOf<org.jfrog.gradle.plugin.artifactory.dsl.ResolverConfig> {
        setProperty("repoKey", "repo")
    })
}

tasks["publish"].dependsOn(tasks["artifactoryPublish"])
