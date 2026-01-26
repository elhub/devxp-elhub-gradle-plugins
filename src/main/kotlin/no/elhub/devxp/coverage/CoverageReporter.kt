package no.elhub.devxp.coverage

import org.gradle.api.Project
import java.io.File
import java.math.BigInteger

class CoverageReporter(private val project: Project) {

    data class CoverageMetrics(
        var instructionCovered: BigInteger = BigInteger.ZERO,
        var instructionMissed: BigInteger = BigInteger.ZERO,
        var branchCovered: BigInteger = BigInteger.ZERO,
        var branchMissed: BigInteger = BigInteger.ZERO
    )

    fun generateReport() {
        val totalMetrics = CoverageMetrics()

        val projectsToCheck = if (project.subprojects.isEmpty()) {
            listOf(project)
        } else {
            project.subprojects
        }

        projectsToCheck.forEach { proj ->
            val reportFile = proj.file("build/reports/jacoco/test/jacocoTestReport.xml")
            if (reportFile.exists()) {
                val projectMetrics = extractCoverage(reportFile)
                totalMetrics.instructionCovered += projectMetrics.instructionCovered
                totalMetrics.instructionMissed += projectMetrics.instructionMissed
                totalMetrics.branchCovered += projectMetrics.branchCovered
                totalMetrics.branchMissed += projectMetrics.branchMissed
            }
        }

        printColoredReport(totalMetrics)
    }

    private fun extractCoverage(reportFile: File): CoverageMetrics {
        val metrics = CoverageMetrics()

        val doc = JacocoParser.parseJacocoXml(reportFile)
        val counters = doc.getElementsByTagName("counter")

        for (i in 0 until counters.length) {
            val node = counters.item(i)
            val attrs = node.attributes
            val type = attrs.getNamedItem("type").nodeValue

            when (type) {
                "INSTRUCTION" -> {
                    metrics.instructionCovered += attrs.getNamedItem("covered").nodeValue.toBigInteger()
                    metrics.instructionMissed += attrs.getNamedItem("missed").nodeValue.toBigInteger()
                }
                "BRANCH" -> {
                    metrics.branchCovered += attrs.getNamedItem("covered").nodeValue.toBigInteger()
                    metrics.branchMissed += attrs.getNamedItem("missed").nodeValue.toBigInteger()
                }
            }
        }

        return metrics
    }

    private fun printColoredReport(metrics: CoverageMetrics) {
        val instructionTotal = metrics.instructionCovered + metrics.instructionMissed
        val branchTotal = metrics.branchCovered + metrics.branchMissed

        val instructionPercent = calculatePercent(metrics.instructionCovered, instructionTotal)
        val branchPercent = calculatePercent(metrics.branchCovered, branchTotal)

        val reset = "\u001B[0m"
        val green = "\u001B[32m"
        val yellow = "\u001B[33m"
        val red = "\u001B[31m"

        val iColor = getColor(instructionPercent, green, yellow, red)
        val bColor = getColor(branchPercent, green, yellow, red)

        println("════════════════════════════════════════════════════════════")
        println("> JaCoCo Test Report")
        println("════════════════════════════════════════════════════════════")
        println(" Instructions: $iColor$instructionPercent%$reset ($instructionTotal total)")
        println(" Branches:     $bColor$branchPercent%$reset ($branchTotal total)")
        println("════════════════════════════════════════════════════════════")
    }

    private fun calculatePercent(covered: BigInteger, total: BigInteger): Int {
        return if (total > BigInteger.ZERO) {
            ((covered * BigInteger.valueOf(100)) / total).toInt()
        } else {
            0
        }
    }

    private fun getColor(percent: Int, green: String, yellow: String, red: String): String {
        return when {
            percent >= 80 -> green
            else -> red
        }
    }
}
