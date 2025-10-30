package no.elhub.devxp.coverage

import org.gradle.api.Project
import java.io.File
import java.math.BigInteger

class CoverageReporter(private val project: Project) {

    fun generateReport() {
        var totalCovered = BigInteger.ZERO
        var totalMissed = BigInteger.ZERO

        val projectsToCheck = if (project.subprojects.isEmpty()) {
            listOf(project)
        } else {
            project.subprojects
        }

        projectsToCheck.forEach { proj ->
            val reportFile = proj.file("build/reports/jacoco/test/jacocoTestReport.xml")
            if (!reportFile.exists()) return@forEach

            val (covered, missed) = extractCoverage(reportFile)
            totalCovered += covered
            totalMissed += missed
        }

        printColoredReport(totalCovered, totalMissed)
    }

    private fun extractCoverage(reportFile: File): Pair<BigInteger, BigInteger> {
        var covered = BigInteger.ZERO
        var missed = BigInteger.ZERO

        val doc = JacocoParser.parseJacocoXml(reportFile)
        val counters = doc.getElementsByTagName("counter")

        for (i in 0 until counters.length) {
            val node = counters.item(i)
            val attrs = node.attributes
            val type = attrs.getNamedItem("type").nodeValue
            if (type == "INSTRUCTION") {
                covered += attrs.getNamedItem("covered").nodeValue.toBigInteger()
                missed += attrs.getNamedItem("missed").nodeValue.toBigInteger()
            }
        }

        return Pair(covered, missed)
    }

    private fun printColoredReport(totalCovered: BigInteger, totalMissed: BigInteger) {
        val total = totalCovered + totalMissed
        val coveragePercent = if (total > BigInteger.ZERO) {
            ((totalCovered * BigInteger.valueOf(100)) / total).toInt()
        } else {
            0
        }

        val reset = "\u001B[0m"
        val color = when {
            coveragePercent >= 80 -> "\u001B[32m"
            else -> "\u001B[31m"
        }

        println("$color═════════════════════════════════════════$reset")
        println("$color> JaCoCO Test Report $reset")
        println("$color> Instruction Coverage: $coveragePercent% $reset")
        println("$color═════════════════════════════════════════$reset")
    }
}
