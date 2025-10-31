package no.elhub.devxp.coverage

import org.w3c.dom.Document
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

object JacocoParser {
    fun parseJacocoXml(file: File): Document {
        val factory = DocumentBuilderFactory.newInstance()
        factory.isValidating = false
        factory.isNamespaceAware = true
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
        val builder = factory.newDocumentBuilder()
        return builder.parse(file)
    }
}
