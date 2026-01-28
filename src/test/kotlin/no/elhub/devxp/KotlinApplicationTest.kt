package no.elhub.devxp

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
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
        val mainClass = "no.elhub.test.TestMainKt"
        project.extensions.extraProperties.set("applicationMainClass", mainClass)
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

        test("should configure the shadowJar task correctly") {
            val shadowTask = project.tasks.getByName("shadowJar") as ShadowJar
            // Verify it produces a classifier-less jar (replacing the default jar)
            shadowTask.archiveClassifier.get() shouldBe ""

            // Verify Manifest attributes
            val attributes = shadowTask.manifest.attributes
            attributes["Main-Class"] shouldBe mainClass
        }

        test("should disable standard distribution tasks") {
            // These tasks are redundant when using shadowJar for application distribution
            listOf("jar", "distTar", "distZip", "startScripts").forEach { taskName ->
                project.tasks.getByName(taskName).enabled shouldBe false
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

        test("shadowJar task should produce a valid jar with correct manifest and filename") {
            testInstance.propertiesFile.appendText("\nversion=1.0.0")
            val result = testInstance.runTask("shadowJar")
            result.output shouldContain ":shadowJar"
            result.output shouldContain "BUILD SUCCESSFUL"
        }
    }
    afterSpec {
        testInstance.dispose()
    }

    context("When assemble is run with this plugin") {

        test("assemble task should depend on shadowJar") {
            val result = testInstance.runTask("assemble", "--dry-run")
            result.output shouldContain ":shadowJar"
            result.output shouldContain "BUILD SUCCESSFUL"
        }
    }
})
