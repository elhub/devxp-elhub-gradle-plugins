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
