/**
 * Define standard Elhub conventions for gradle version catalogs
 */
package no.elhub.devxp

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

tasks["publish"].dependsOn(tasks["artifactoryPublish"])
