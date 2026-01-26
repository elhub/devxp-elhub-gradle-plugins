/*
 * Define standard Elhub conventions for Kotlin libraries
 */
package no.elhub.devxp

plugins {
    id("no.elhub.devxp.kotlin-core")
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

/*
 * Ensure sources and docs are published for libraries
 */
java {
    withJavadocJar()
    withSourcesJar()
}
