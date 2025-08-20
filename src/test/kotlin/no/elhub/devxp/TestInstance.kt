package no.elhub.devxp

import org.apache.commons.io.FileUtils
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import java.io.File
import java.nio.file.Files.createTempDirectory
import java.nio.file.Path

class TestInstance {
    private val projectDir: Path = createTempDirectory("test")
    private var settingsFile: File = File(projectDir.toString(), "settings.gradle.kts")
    var buildFile: File = File(projectDir.toString(), "build.gradle.kts")
    var propertiesFile: File = File(projectDir.toString(), "gradle.properties")

    init {
        settingsFile.appendText(
            """
                rootProject.name = "test"
            """
        )
        propertiesFile.appendText(
            """
                kotlin.code.style=official
                version=0.0.0
                artifactoryUri=http://jfrog.null.void/artifactory
            """
        )
    }

    fun runTask(task: String): BuildResult =
        GradleRunner
            .create()
            .withProjectDir(projectDir.toFile())
            .withArguments(task, "--stacktrace")
            .withPluginClasspath()
            .build()

    fun dispose() {
        FileUtils.deleteDirectory(projectDir.toFile())
    }
}
