package no.elhub.devxp

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.tasks.JacocoReport
import org.gradle.testkit.runner.UnexpectedBuildFailure

class KotlinCoreTest : FunSpec({
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

    context("When gradle project is built") {

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
            test("should include the $plugin plugin") {
                project.plugins.hasPlugin(plugin) shouldBe true
            }
        }

        test("should configure repository URL correctly") {
            val repository = project.repositories.first()
            repository.name shouldBe "maven"
        }

        test("should configure Java compile options correctly") {
            val javaExtension = project.extensions.getByName("java") as org.gradle.api.plugins.JavaPluginExtension
            javaExtension.sourceCompatibility shouldBe org.gradle.api.JavaVersion.VERSION_17
            javaExtension.targetCompatibility shouldBe org.gradle.api.JavaVersion.VERSION_17
        }

        test("should configure test logging and Jacoco correctly") {
            val testTask = project.tasks.getByName("test") as org.gradle.api.tasks.testing.Test

            val jacocoExtension = project.extensions.getByType(JacocoPluginExtension::class.java)
            jacocoExtension.toolVersion shouldBe "0.8.14"

            val jacocoTestReportTask = project.tasks.getByName("jacocoTestReport") as JacocoReport
            jacocoTestReportTask.dependsOn(testTask)
            jacocoTestReportTask.reports.xml.required.get() shouldBe true
        }
    }

    context("When gradle tasks is run with this project") {

        val optionsExpected =
            arrayOf<String>(
                "assemble",
                "dependencyUpdates",
                "dependencyCheckAnalyze",
                "dokkaGenerate",
                "dokkaGenerateHtml",
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

    context("When jacocoTestReport is run with this plugin") {

        test("should run test and jacocoTestReport tasks") {
            val result = testInstance.runTask("jacocoTestReport", "--dry-run")
            result.output shouldContain ":test"
            result.output shouldContain ":jacocoTestReport"
            result.output shouldContain "BUILD SUCCESSFUL"
        }
    }

    context("When printCoverage is run with this plugin") {

        test("should run the printCoverage task") {
            val result = testInstance.runTask("printCoverage", "--dry-run")
            result.output shouldContain ":printCoverage"
            result.output shouldContain "BUILD SUCCESSFUL"
        }

        test("printCoverage also include jacocoTestReport and test") {
            val result = testInstance.runTask("printCoverage", "--dry-run")
            result.output shouldContain ":test"
            result.output shouldContain ":jacocoTestReport"
            result.output shouldContain ":printCoverage"
        }
    }

    context("When publish is run with this plugin") {

        test("publish --dry-run task should run successfully") {
            val result = testInstance.runTask("publish", "--dry-run")
            result.output shouldContain ":artifactoryPublish"
            result.output shouldContain "artifactoryDeploy"
            result.output shouldContain ":publish"
            result.output shouldContain "BUILD SUCCESSFUL"
        }

        test("publish --dry-run task should run successfully with variables") {
            val result = testInstance.runTask(
                "publish",
                "--dry-run",
                environment = mapOf(
                    "artifactoryRepository" to "elhub-mvn-test-local",
                    "artifactoryUsername" to "user",
                    "artifactoryPassword" to "password"
                )
            )
            result.output shouldContain ":artifactoryPublish"
            result.output shouldContain "artifactoryDeploy"
            result.output shouldContain ":publish"
            result.output shouldContain "BUILD SUCCESSFUL"
        }
    }

    context("When dependencyCheckAnalyze is run with this plugin") {

        test("should exercise the dependency check") {
            val result = testInstance.runTask("dependencyCheckAnalyze", "--dry-run")
            result.output shouldContain ":dependencyCheckAnalyze"
            result.output shouldContain "BUILD SUCCESSFUL"
        }

        test("should work with -PdependencyCHeck.suppressionFile") {
            val result = testInstance.runTask("dependencyCheckAnalyze", "-PdependencyCheck.suppressionFile=suppressions.xml", "--dry-run")
            result.output shouldContain ":dependencyCheckAnalyze"
            result.output shouldContain "BUILD SUCCESSFUL"
        }

        test("should configure dependencyCheck suppression file via Project property") {
            val projectWithSuppression = ProjectBuilder.builder().build()
            projectWithSuppression.extensions.extraProperties["dependencyCheck.suppressionFile"] = "suppressions.xml"
            projectWithSuppression.pluginManager.apply("no.elhub.devxp.kotlin-core")
            val extension =
                projectWithSuppression.extensions.getByName("dependencyCheck") as org.owasp.dependencycheck.gradle.extension.DependencyCheckExtension
            extension.suppressionFile.get() shouldBe "suppressions.xml"
        }
    }

    context("When dokka is run with this plugin") {

        test("dokkaGfm task should run successfully") {
            val result = testInstance.runTask("dokkaGenerate", "--dry-run")
            result.output shouldContain ":dokkaGenerate"
            result.output shouldContain "BUILD SUCCESSFUL"
        }

        test("dokkaHtml task should run successfully") {
            val result = testInstance.runTask("dokkaGenerateHtml", "--dry-run")
            result.output shouldContain ":dokkaGenerateHtml"
            result.output shouldContain "BUILD SUCCESSFUL"
        }
    }

    context("When teamCityCheck is run with this plugin") {

        test("should work on a --dry-run") {
            val result = testInstance.runTask("teamcityCheck", "--dry-run")
            result.output shouldContain ":teamcityCheck"
            result.output shouldContain "BUILD SUCCESSFUL"
        }

        test("should fail if run without a .teamcity directory") {
            shouldThrow<UnexpectedBuildFailure> {
                testInstance.runTask("teamcityCheck")
            }
        }
    }

    afterSpec {
        testInstance.dispose()
    }
})
