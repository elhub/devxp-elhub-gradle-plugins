package no.elhub.devxp

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.gradle.testfixtures.ProjectBuilder
import org.owasp.dependencycheck.gradle.tasks.Analyze

class ProxyConfigTest : DescribeSpec({

    describe("setCustomConfiguration") {

        it("should set proxy system properties when project properties are present") {
            val project = ProjectBuilder.builder().build()
            project.extensions.extraProperties.set("proxyHost", "proxy.example.com")
            project.extensions.extraProperties.set("proxyPort", "8080")
            project.extensions.extraProperties.set("nonProxyHosts", "localhost|*.internal")

            val task = project.tasks.create("analyze", Analyze::class.java)
            task.setCustomConfiguration()

            // Execute doFirst action
            task.actions.first().execute(task)

            System.getProperty("http.proxyHost") shouldBe "proxy.example.com"
            System.getProperty("http.proxyPort") shouldBe "8080"
            System.getProperty("http.nonProxyHosts") shouldBe "localhost|*.internal"
            System.getProperty("https.proxyHost") shouldBe "proxy.example.com"
            System.getProperty("https.proxyPort") shouldBe "8080"
            System.getProperty("https.nonProxyHosts") shouldBe "localhost|*.internal"

            // Execute doLast action
            task.actions.last().execute(task)

            System.getProperty("http.proxyHost") shouldBe null
            System.getProperty("http.proxyPort") shouldBe null
            System.getProperty("http.nonProxyHosts") shouldBe null
            System.getProperty("https.proxyHost") shouldBe null
            System.getProperty("https.proxyPort") shouldBe null
            System.getProperty("https.nonProxyHosts") shouldBe null
        }

        it("should not set proxy host/port when only one is present") {
            val project = ProjectBuilder.builder().build()
            project.extensions.extraProperties.set("proxyHost", "proxy.example.com")
            project.extensions.extraProperties.set("nonProxyHosts", "localhost")

            val task = project.tasks.create("analyze", Analyze::class.java)
            task.setCustomConfiguration()

            task.actions.first().execute(task)

            System.getProperty("http.proxyHost") shouldBe null
            System.getProperty("http.proxyPort") shouldBe null
            System.getProperty("http.nonProxyHosts") shouldBe "localhost"
        }
    }
})
