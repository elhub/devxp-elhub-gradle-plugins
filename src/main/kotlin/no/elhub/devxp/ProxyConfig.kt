package no.elhub.devxp

import org.owasp.dependencycheck.gradle.tasks.AbstractAnalyze

fun applyProxyConfig(proxyHost: String?, proxyPort: String?, nonProxyHosts: String?) {
    listOf("http", "https").forEach {
        if (!proxyHost.isNullOrBlank() && !proxyPort.isNullOrBlank()) {
            System.setProperty("$it.proxyHost", proxyHost)
            System.setProperty("$it.proxyPort", proxyPort)
        }
        if (!nonProxyHosts.isNullOrBlank()) {
            System.setProperty("$it.nonProxyHosts", nonProxyHosts)
        }
    }
}

fun clearProxyConfig() {
    listOf("http", "https").forEach {
        System.clearProperty("$it.proxyPort")
        System.clearProperty("$it.proxyHost")
        System.clearProperty("$it.nonProxyHosts")
    }
}

fun AbstractAnalyze.setCustomConfiguration() {
    doFirst {
        val proxyHost = project.findProperty("proxyHost")?.toString()
        val proxyPort = project.findProperty("proxyPort")?.toString()
        val nonProxyHosts = project.findProperty("nonProxyHosts")?.toString()
        applyProxyConfig(proxyHost, proxyPort, nonProxyHosts)
    }
    doLast {
        clearProxyConfig()
    }
}
