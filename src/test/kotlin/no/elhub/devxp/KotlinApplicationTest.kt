package no.elhub.devxp

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.string.shouldContain

class KotlinApplicationTest : DescribeSpec({
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
                applicationMainClass=no.elhub.test.TestMainKt
            """.trimIndent()
        )
    }

    describe("When gradle tasks is run with this project") {

        val optionsExpected = arrayOf<String>(
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
            it ("The output should list the $option task") {
                result.output shouldContain "${option} -"
            }
        }

    }

    afterSpec {
        testInstance.dispose()
    }

})
