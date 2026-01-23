package no.elhub.devxp

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ProxyConfigTest : FunSpec({

    test("should set both http and https proxy properties when all values are present") {
        applyProxyConfig("proxy.example.com", "8080", "localhost|*.internal")
        System.getProperty("http.proxyHost") shouldBe "proxy.example.com"
        System.getProperty("http.proxyPort") shouldBe "8080"
        System.getProperty("http.nonProxyHosts") shouldBe "localhost|*.internal"

        clearProxyConfig()
    }

    test("should not set proxy host/port if either is missing") {
        applyProxyConfig("proxy.example.com", null, "localhost")
        System.getProperty("http.proxyHost") shouldBe null
        System.getProperty("http.proxyPort") shouldBe null
        System.getProperty("http.nonProxyHosts") shouldBe "localhost"

        clearProxyConfig()

        applyProxyConfig(null, "8080", "localhost")
        System.getProperty("https.proxyHost") shouldBe null
        System.getProperty("https.proxyPort") shouldBe null
        System.getProperty("https.nonProxyHosts") shouldBe "localhost"

        clearProxyConfig()
    }

    test("should not set proxy host/port if either is blank") {
        applyProxyConfig("proxy.example.com", "", "localhost")
        System.getProperty("http.proxyHost") shouldBe null
        System.getProperty("http.proxyPort") shouldBe null
        System.getProperty("http.nonProxyHosts") shouldBe "localhost"

        clearProxyConfig()

        applyProxyConfig("", "8080", "localhost")
        System.getProperty("https.proxyHost") shouldBe null
        System.getProperty("https.proxyPort") shouldBe null
        System.getProperty("https.nonProxyHosts") shouldBe "localhost"

        clearProxyConfig()
    }

    test("should not set nonproxy if missing") {
        applyProxyConfig("proxy.example.com", "8080", null)
        System.getProperty("http.nonProxyHosts") shouldBe null

        clearProxyConfig()

        applyProxyConfig("proxy.example.com", "8080", "")
        System.getProperty("http.nonProxyHosts") shouldBe null

        clearProxyConfig()
    }

    test("should clear all proxy properties") {
        applyProxyConfig("proxy.example.com", "8080", "localhost")
        clearProxyConfig()
        System.getProperty("http.proxyHost") shouldBe null
        System.getProperty("http.proxyPort") shouldBe null
        System.getProperty("http.nonProxyHosts") shouldBe null
    }

    test("should not set any properties if all arguments are null or blank") {
        applyProxyConfig(null, null, null)

        System.getProperty("http.proxyHost") shouldBe null
        System.getProperty("http.proxyPort") shouldBe null
        System.getProperty("http.nonProxyHosts") shouldBe null
        System.getProperty("https.proxyHost") shouldBe null
        System.getProperty("https.proxyPort") shouldBe null
        System.getProperty("https.nonProxyHosts") shouldBe null
    }
})
