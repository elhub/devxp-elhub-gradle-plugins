package no.elhub.devxp

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.gradle.testfixtures.ProjectBuilder

class KotlinApplicationTest : FunSpec({
    val testInstance = TestInstance()

    beforeSpec {
        testInstance.buildFile.appendText(
            """
            plugins {
                id("no.elhub.devxp.kotlin-application")
            }
            """
        )
        testInstance.propertiesFile.appendText(
            """
                applicationMainClass=no.elhub.test.MainKt
            """.trimIndent()
        )
    }

    context("When kotlin-application is built") {

        val project = ProjectBuilder.builder().build()
        project.extensions.extraProperties.set("applicationMainClass", "no.elhub.test.TestMainKt")
        project.pluginManager.apply("no.elhub.devxp.kotlin-application")

        val pluginsIncluded =
            arrayOf<String>(
                "org.jetbrains.kotlin.jvm",
                "com.github.ben-manes.versions",
                "jacoco",
                "com.adarshr.test-logger",
                "org.owasp.dependencycheck",
                "org.jetbrains.dokka",
                "application",
                "com.github.johnrengelman.shadow",
                "com.jfrog.artifactory",
                "maven-publish"
            )

        pluginsIncluded.forEach { plugin ->
            test("The project should include the $plugin plugin") {
                project.plugins.hasPlugin(plugin) shouldBe true
            }
        }
    }

    context("When gradle tasks is run with this plugin") {

        val optionsExpected =
            arrayOf<String>(
                "assemble",
                "dependencyUpdates",
                "dependencyCheckAnalyze",
                "jacocoTestReport",
                "test",
                "publish",
                "artifactoryPublish",
                "shadowJar",
                "run",
            )
        val result = testInstance.runTask("tasks")

        optionsExpected.forEach { option ->
            test("The output should list the $option task") {
                result.output shouldContain "$option -"
            }
        }
    }

    context("When shadowJar is run with this plugin") {

        test("shadowJar task should run successfully") {
            val result = testInstance.runTask("shadowJar")
            result.output shouldContain ":shadowJar"
            result.output shouldContain "BUILD SUCCESSFUL"
        }
    }

    afterSpec {
        testInstance.dispose()
    }
})
