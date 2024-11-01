package no.elhub.devxp

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.gradle.testfixtures.ProjectBuilder

class JavaPlatformTest : DescribeSpec({
    val testInstance = TestInstance()

    beforeSpec {
        testInstance.buildFile.appendText(
            """
            plugins {
                id("no.elhub.devxp.java-platform")
            }
            """
        )
    }

    describe("When java-platform is built") {

        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("no.elhub.devxp.java-platform")

        val pluginsIncluded =
            arrayOf<String>(
                "java-platform",
                "com.github.ben-manes.versions",
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
                "artifactoryPublish",
                "dependencyUpdates",
                "publish"
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
