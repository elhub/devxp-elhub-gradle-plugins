package no.elhub.devxp

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.UnexpectedBuildFailure

class KotlinLibraryTest : DescribeSpec({
    val testInstance = TestInstance()

    beforeSpec {
        testInstance.buildFile.appendText(
            """
            plugins {
                id("no.elhub.devxp.kotlin-library")
            }
            """
        )
    }

    describe("When kotlin-application is built") {

        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("no.elhub.devxp.kotlin-library")

        val pluginsIncluded =
            arrayOf<String>(
                "org.jetbrains.kotlin.jvm",
                "com.github.ben-manes.versions",
                "jacoco",
                "com.adarshr.test-logger",
                "org.owasp.dependencycheck",
                "org.jetbrains.dokka",
                "com.jfrog.artifactory",
                "maven-publish"
            )

        pluginsIncluded.forEach { plugin ->
            it("The project should include the $plugin plugin") {
                project.plugins.hasPlugin(plugin) shouldBe true
            }
        }
    }

    describe("When gradle tasks is run with this plugin") {

        val optionsExpected =
            arrayOf<String>(
                "assemble",
                "dependencyCheckAnalyze",
                "dependencyUpdates",
                "jacocoTestReport",
                "test",
                "publish",
                "artifactoryPublish"
            )
        val result = testInstance.runTask("tasks")

        optionsExpected.forEach { option ->
            it("The output should list the $option task") {
                result.output shouldContain "$option -"
            }
        }
    }

    describe("When the project publishes artifacts") {

        it("should fail on the artifactoryPublish task if host does not exist") {
            shouldThrow<UnexpectedBuildFailure> {
                testInstance.runTask("artifactoryPublish")
            }
        }
    }

    afterSpec {
        testInstance.dispose()
    }
})
