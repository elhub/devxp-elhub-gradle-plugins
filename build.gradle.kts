import org.jfrog.gradle.plugin.artifactory.dsl.PublisherConfig

plugins {
    `kotlin-dsl`
    id("com.github.ben-manes.versions") version "0.41.0"
    id("jacoco")
    id("com.adarshr.test-logger") version "3.2.0"
    id("com.jfrog.artifactory") version "4.29.0"
    id("maven-publish") apply true
}

repositories {
    maven(url = "https://jfrog.elhub.cloud/artifactory/elhub-mvn")
    maven(url = "https://jfrog.elhub.cloud/artifactory/elhub-plugins")
}

val allureVersion = "2.19.0"
val kotestVersion = "5.4.2"

group = "no.elhub.devxp"

/** Classpaths of plugins used in the elhub-gradle-plugins need to be defined in the dependencies to ensure they
 *  are available when building the plugin Jar.
 */
dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.20")
    implementation("com.github.ben-manes:gradle-versions-plugin:0.41.0")
    implementation("com.adarshr:gradle-test-logger-plugin:3.2.0")
    implementation("org.jfrog.buildinfo:build-info-extractor-gradle:4.29.0")
    implementation("gradle.plugin.com.github.johnrengelman:shadow:7.1.2")
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest:kotest-extensions-allure-jvm:4.4.3")
    implementation("commons-io:commons-io:2.11.0")
}

/*
 * Test setup
 */
tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = true
    }
}

jacoco {
    toolVersion = "0.8.7" // Has to be the same as TeamCity
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport) // report is always generated after tests run
}

tasks.jacocoTestReport {
    dependsOn(tasks.test) // tests are required to run before generating the report
}

testlogger {
    theme = com.adarshr.gradle.testlogger.theme.ThemeType.MOCHA
}

/*
 * Publishing
 */
val publishUri = project.findProperty("artifactoryUri") ?: "https://jfrog.elhub.cloud/artifactory/"
val repository = project.findProperty("artifactoryRepository") ?: "elhub-mvn-dev-local"

publishing {
    repositories {
        maven {
            url = uri("$artifactory/$repository")
        }
    }
}

artifactory {
    setContextUrl(publishUri)
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

/*
 * TeamCity
 */
tasks.register("teamCity", Exec::class) {
    description = "Compile the TeamCity settings"
    workingDir(".teamcity")
    commandLine("mvn", "compile")
}
