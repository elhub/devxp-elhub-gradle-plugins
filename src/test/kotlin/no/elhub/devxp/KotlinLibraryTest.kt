package no.elhub.devxp

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.string.shouldContain

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

    describe("When gradle tasks is run with this project") {

        val optionsExpected = arrayOf<String>(
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
            it ("The output should list the $option task") {
                result.output shouldContain "${option} -"
            }
        }

    }

    afterSpec {
        testInstance.dispose()
    }

})
