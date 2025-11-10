/**
 * Define standard Elhub conventions for Kotlin libraries
 */
package no.elhub.devxp

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar.Companion.shadowJar

plugins {
    id("no.elhub.devxp.kotlin-core")
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

// Shadow has its own tasks for this, so we use those instead
listOf("jar", "distTar", "distZip", "startScripts").forEach {
    tasks.named(it) {
        enabled = false
    }
}

// This ensures that the jar is built when we run ./gradlew build, and is therefore available when we run publish afterward
tasks.assemble {
    dependsOn(tasks.shadowJar)
}
