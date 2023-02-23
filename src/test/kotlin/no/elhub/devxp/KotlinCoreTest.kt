package no.elhub.devxp

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.string.shouldContain

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

    describe("When gradle tasks is run with this project") {

        val optionsExpected = arrayOf<String>(
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
            it ("The output should list the $option task") {
                result.output shouldContain "${option} -"
            }
        }

    }

    afterSpec {
        testInstance.dispose()
    }

})
