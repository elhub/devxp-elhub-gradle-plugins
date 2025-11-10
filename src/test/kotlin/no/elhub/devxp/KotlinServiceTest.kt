package no.elhub.devxp

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.gradle.testfixtures.ProjectBuilder

class KotlinServiceTest : FunSpec({
    val testInstance = TestInstance()

    beforeSpec {
        testInstance.buildFile.appendText(
            """
            plugins {
                id("no.elhub.devxp.kotlin-service")
            }
            """
        )
    }

    context("When kotlin-service is built") {

        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("no.elhub.devxp.kotlin-service")

        val pluginsIncluded =
            arrayOf<String>(
                "org.jetbrains.kotlin.jvm",
                "com.github.ben-manes.versions",
                "jacoco",
                "com.adarshr.test-logger",
                "org.owasp.dependencycheck",
                "org.jetbrains.dokka"
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
                "dependencyCheckAnalyze",
                "dependencyUpdates",
                "jacocoTestReport",
                "test",
            )
        val result = testInstance.runTask("tasks")

        optionsExpected.forEach { option ->
            test("The output should list the $option task") {
                result.output shouldContain "$option -"
            }
        }
    }

    afterSpec {
        testInstance.dispose()
    }
})
