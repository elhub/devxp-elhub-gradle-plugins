package no.elhub.devxp

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.string.shouldContain

class GradleVersionCatalogTest : DescribeSpec({
    val testInstance = TestInstance()

    beforeSpec {
        testInstance.buildFile.appendText(
            """
            plugins {
                id("no.elhub.devxp.gradle-version-catalog")
            }
            """
        )
    }

    describe("When gradle tasks is run with this project") {

        val optionsExpected = arrayOf<String>(
            "generateCatalogAsToml",
            "artifactoryPublish",
            "publish"
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
