package no.elhub.devxp

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.tasks.JacocoReport

class KotlinCoreTest : DescribeSpec({
    val testInstance = TestInstance()

    beforeSpec {
        testInstance.buildFile.appendText(
            """
            plugins {
                id("no.elhub.devxp.kotlin-core")
            }
            """
        )
    }

    describe("When gradle project is built") {

        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("no.elhub.devxp.kotlin-core")

        val pluginsIncluded =
            arrayOf<String>(
                "org.jetbrains.kotlin.jvm",
                "com.github.ben-manes.versions",
                "jacoco",
                "com.adarshr.test-logger",
                "org.owasp.dependencycheck",
                "org.jetbrains.dokka",
            )

        pluginsIncluded.forEach { plugin ->
            it("should include the $plugin plugin") {
                project.plugins.hasPlugin(plugin) shouldBe true
            }
        }

        it("should configure repository URL correctly") {
            val repository = project.repositories.first()
            repository.name shouldBe "maven"
        }

        it("should configure Java compile options correctly") {
            val javaExtension = project.extensions.getByName("java") as org.gradle.api.plugins.JavaPluginExtension
            javaExtension.sourceCompatibility shouldBe org.gradle.api.JavaVersion.VERSION_17
            javaExtension.targetCompatibility shouldBe org.gradle.api.JavaVersion.VERSION_17
        }

        it("should configure test logging and Jacoco correctly") {
            val testTask = project.tasks.getByName("test") as org.gradle.api.tasks.testing.Test

            val jacocoExtension = project.extensions.getByType(JacocoPluginExtension::class.java)
            jacocoExtension.toolVersion shouldBe "0.8.13"

            val jacocoTestReportTask = project.tasks.getByName("jacocoTestReport") as JacocoReport
            jacocoTestReportTask.dependsOn(testTask)
            jacocoTestReportTask.reports.xml.required.get() shouldBe true
        }
    }

    describe("When gradle tasks is run with this project") {

        val optionsExpected =
            arrayOf<String>(
                "assemble",
                "dependencyUpdates",
                "dependencyCheckAnalyze",
                "dokkaGfm",
                "dokkaHtml",
                "jacocoTestReport",
                "test",
            )
        val result = testInstance.runTask("tasks")

        optionsExpected.forEach { option ->
            it("The output should list the $option task") {
                result.output shouldContain "$option -"
            }
        }
    }

    afterSpec {
        testInstance.dispose()
    }
})
