package no.elhub.devxp

import java.util.Locale

fun isStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any {
        version.uppercase(Locale.ROOT).contains(it)
    }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    return stableKeyword || regex.matches(version)
}

fun isNonStable(version: String): Boolean = isStable(version).not()
