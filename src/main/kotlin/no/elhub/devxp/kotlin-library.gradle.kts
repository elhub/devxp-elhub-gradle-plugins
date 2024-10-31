/**
 * Define standard Elhub conventions for Kotlin libraries
 */
package no.elhub.devxp

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
    clientConfig.isIncludeEnvVars = true

    publish {
        contextUrl = project.findProperty("artifactoryUri")?.toString() ?: "https://jfrog.elhub.cloud/artifactory"
        repository {
            repoKey = project.findProperty("artifactoryRepository")?.toString() ?: "elhub-plugins-dev-local"
            username = project.findProperty("artifactoryUsername")?.toString() ?: "nouser" // The publisher user name
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

tasks["publish"].dependsOn(tasks["artifactoryPublish"])
