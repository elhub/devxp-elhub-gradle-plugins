package no.elhub.devxp

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import java.io.File
import java.nio.file.Files.createTempDirectory
import java.nio.file.Path
import org.apache.commons.io.FileUtils

class TestInstance() {
    private val projectDir: Path = createTempDirectory("test")
    var settingsFile: File = File(projectDir.toString(), "settings.gradle.kts")
    var buildFile: File =  File(projectDir.toString(), "build.gradle.kts")

    init {
        settingsFile.appendText("""
            rootProject.name = "test"
        """)
    }

    fun runTask(task: String): BuildResult {
        return GradleRunner.create()
                .withProjectDir(projectDir.toFile())
                .withArguments(task, "--stacktrace")
                .withPluginClasspath()
                .build()
    }

    fun runTaskWithFailure(task: String): BuildResult {
        return GradleRunner.create()
                .withProjectDir(projectDir.toFile())
                .withArguments(task, "--stacktrace")
                .withPluginClasspath()
                .buildAndFail()
    }

    fun dispose() {
        FileUtils.deleteDirectory(projectDir.toFile())
    }
}
