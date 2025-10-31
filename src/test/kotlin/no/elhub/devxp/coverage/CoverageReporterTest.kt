package no.elhub.devxp.coverage

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.string.shouldContain
import org.gradle.testfixtures.ProjectBuilder
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream

class CoverageReporterTest : FunSpec({

    val originalOut = System.out

    fun captureOutput(block: () -> Unit): String {
        val outputStream = ByteArrayOutputStream()
        System.setOut(PrintStream(outputStream))
        try {
            block()
            return outputStream.toString()
        } finally {
            System.setOut(originalOut)
        }
    }

    context("Coverage Reporting") {

        test("should report 100% coverage for fully covered code") {
            val project = ProjectBuilder.builder().build()
            val reportDir = File(project.projectDir, "build/reports/jacoco/test")
            reportDir.mkdirs()

            val reportFile = File(reportDir, "jacocoTestReport.xml")
            reportFile.writeText(
                """
            <?xml version="1.0" encoding="UTF-8"?>
            <report name="test">
                <counter type="INSTRUCTION" missed="0" covered="100"/>
            </report>
                """.trimIndent()
            )

            val output = captureOutput {
                CoverageReporter(project).generateReport()
            }

            output shouldContain "100%"
            output shouldContain "JaCoCO Test Report"
        }

        test("should report 0% coverage when no instructions covered") {
            val project = ProjectBuilder.builder().build()
            val reportDir = File(project.projectDir, "build/reports/jacoco/test")
            reportDir.mkdirs()

            val reportFile = File(reportDir, "jacocoTestReport.xml")
            reportFile.writeText(
                """
            <?xml version="1.0" encoding="UTF-8"?>
            <report name="test">
                <counter type="INSTRUCTION" missed="100" covered="0"/>
            </report>
                """.trimIndent()
            )

            val output = captureOutput {
                CoverageReporter(project).generateReport()
            }

            output shouldContain "0%"
        }

        test("should aggregate coverage from multiple subprojects") {
            val rootProject = ProjectBuilder.builder().build()
            val subproject1 = ProjectBuilder.builder().withParent(rootProject).withName("sub1").build()
            val subproject2 = ProjectBuilder.builder().withParent(rootProject).withName("sub2").build()

            listOf(subproject1, subproject2).forEach { proj ->
                val reportDir = File(proj.projectDir, "build/reports/jacoco/test")
                reportDir.mkdirs()
                File(reportDir, "jacocoTestReport.xml").writeText(
                    """
                <?xml version="1.0" encoding="UTF-8"?>
                <report name="test">
                    <counter type="INSTRUCTION" missed="10" covered="90"/>
                </report>
                    """.trimIndent()
                )
            }

            val output = captureOutput {
                CoverageReporter(rootProject).generateReport()
            }

            output shouldContain "90%"
        }

        test("should handle missing report files gracefully") {
            val project = ProjectBuilder.builder().build()

            val output = captureOutput {
                CoverageReporter(project).generateReport()
            }

            output shouldContain "0%"
        }

        test("should use green color for coverage >= 80%") {
            val project = ProjectBuilder.builder().build()
            val reportDir = File(project.projectDir, "build/reports/jacoco/test")
            reportDir.mkdirs()

            val reportFile = File(reportDir, "jacocoTestReport.xml")
            reportFile.writeText(
                """
            <?xml version="1.0" encoding="UTF-8"?>
            <report name="test">
                <counter type="INSTRUCTION" missed="15" covered="85"/>
            </report>
                """.trimIndent()
            )

            val output = captureOutput {
                CoverageReporter(project).generateReport()
            }

            output shouldContain "\u001B[32m"
            output shouldContain "85%"
        }

        test("should use red color for coverage < 80%") {
            val project = ProjectBuilder.builder().build()
            val reportDir = File(project.projectDir, "build/reports/jacoco/test")
            reportDir.mkdirs()

            val reportFile = File(reportDir, "jacocoTestReport.xml")
            reportFile.writeText(
                """
            <?xml version="1.0" encoding="UTF-8"?>
            <report name="test">
                <counter type="INSTRUCTION" missed="70" covered="30"/>
            </report>
                """.trimIndent()
            )

            val output = captureOutput {
                CoverageReporter(project).generateReport()
            }

            output shouldContain "\u001B[31m"
            output shouldContain "30%"
        }
    }
})
