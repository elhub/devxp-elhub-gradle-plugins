/**
 * Define standard Elhub conventions for Kotlin libraries
 */
package no.elhub.devxp

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar.Companion.shadowJar

plugins {
    id("no.elhub.devxp.kotlin-core")
    id("com.jfrog.artifactory")
    id("maven-publish")
    id("com.gradleup.shadow")
    id("application")
}

/*
 * Publishing
 */
publishing {
    publications {
        create<MavenPublication>("shadow") {
            from(components["shadow"])
        }
    }
}

artifactory {
    clientConfig.isIncludeEnvVars = true

    publish {
        contextUrl = project.findProperty("artifactoryUri")?.toString() ?: "https://jfrog.elhub.cloud/artifactory"
        repository {
            repoKey = project.findProperty("artifactoryRepository")?.toString() ?: "elhub-bin-dev-local"
            username = project.findProperty("artifactoryUsername")?.toString() ?: "nouser" // The publisher user name
            password = project.findProperty("artifactoryPassword")?.toString() ?: "nopass" // The publisher password
        }

        defaults {
            publications("ALL_PUBLICATIONS")
            setPublishArtifacts(true)
            setPublishIvy(false) // Publish generated Ivy descriptor files to Artifactory (true by default)
        }
    }
}

tasks["publish"].dependsOn(tasks["artifactoryPublish"])

/*
 * Executable Jar File Assembly.
 */
val applicationMainClass: String by project

application {
    mainClass = applicationMainClass
}

tasks.shadowJar {
    archiveBaseName.set(rootProject.name)
    archiveClassifier.set("")
    manifest {
        attributes(
            "Main-Class" to applicationMainClass
        )
    }
    mergeServiceFiles()
}

listOf("assemble", "distZip", "distTar", "startScripts").forEach {
    tasks[it].dependsOn(tasks.shadowJar)
}
