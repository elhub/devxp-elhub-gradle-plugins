package no.elhub.devxp.coverage

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.io.File

class JacocoParserTest : FunSpec({

    test("should parse valid JaCoCo XML report") {
        val xmlContent = """
            <?xml version="1.0" encoding="UTF-8"?>
            <report name="test">
                <counter type="INSTRUCTION" missed="10" covered="90"/>
                <counter type="BRANCH" missed="5" covered="15"/>
            </report>
        """.trimIndent()

        val tempFile = File.createTempFile("jacoco", ".xml")
        tempFile.writeText(xmlContent)

        val doc = JacocoParser.parseJacocoXml(tempFile)

        doc shouldNotBe null
        doc.documentElement.tagName shouldBe "report"
        tempFile.delete()
    }

    test("should parse XML without external DTD") {
        val xmlContent = """
            <?xml version="1.0" encoding="UTF-8"?>
            <!DOCTYPE report SYSTEM "http://external.dtd">
            <report name="test">
                <counter type="INSTRUCTION" missed="10" covered="90"/>
            </report>
        """.trimIndent()

        val tempFile = File.createTempFile("jacoco", ".xml")
        tempFile.writeText(xmlContent)

        val doc = JacocoParser.parseJacocoXml(tempFile)

        doc shouldNotBe null
        tempFile.delete()
    }

    test("should throw exception for non-existent file") {
        val nonExistentFile = File("non-existent-file.xml")

        shouldThrow<Exception> {
            JacocoParser.parseJacocoXml(nonExistentFile)
        }
    }

    test("should throw exception for invalid XML") {
        val invalidXml = "not valid xml content"
        val tempFile = File.createTempFile("invalid", ".xml")
        tempFile.writeText(invalidXml)

        shouldThrow<Exception> {
            JacocoParser.parseJacocoXml(tempFile)
        }

        tempFile.delete()
    }
})
