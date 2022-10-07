import jetbrains.buildServer.configs.kotlin.v2019_2.DslContext
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.SshAgent
import jetbrains.buildServer.configs.kotlin.v2019_2.project
import jetbrains.buildServer.configs.kotlin.v2019_2.sequential
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.VcsTrigger
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.vcs
import jetbrains.buildServer.configs.kotlin.v2019_2.version
import no.elhub.devxp.build.configuration.AutoRelease
import no.elhub.devxp.build.configuration.CodeReview
import no.elhub.devxp.build.configuration.ProjectType
import no.elhub.devxp.build.configuration.SonarScan
import no.elhub.devxp.build.configuration.UnitTest
import no.elhub.devxp.build.configuration.constants.GlobalTokens

version = "2022.04"

project {
    val projectName = "devxp-elhub-gradle-plugins"
    val projectId = "no.elhub.devxp:$projectName"
    val projectType = ProjectType.GRADLE
    val artifactoryRepository = "elhub-mvn-release-local"

    params {
        param("teamcity.ui.settings.readOnly", "true")
    }

    val unitTest = UnitTest(
        UnitTest.Config(
            vcsRoot = DslContext.settingsRoot,
            type = projectType,
            generateAllureReport = false,
        )
    )

    val sonarScanConfig = SonarScan.Config(
        vcsRoot = DslContext.settingsRoot,
        type = projectType,
        sonarId = projectId,
        sonarProjectSources = "src/main/resources",
    )

    val sonarScan = SonarScan(sonarScanConfig) {
        dependencies {
            snapshot(unitTest) { }
        }
    }

    val autoRelease = AutoRelease(
        AutoRelease.Config(
            vcsRoot = DslContext.settingsRoot,
            type = projectType,
            repository = artifactoryRepository
        )
    ) {
        dependencies {
            snapshot(sonarScan) { }
        }
        triggers {
            vcs {
                branchFilter = "+:<default>"
                quietPeriodMode = VcsTrigger.QuietPeriodMode.USE_DEFAULT
            }
        }
    }

    listOf(unitTest, sonarScan, autoRelease).forEach { buildType(it) }

    buildType(
        CodeReview(
            CodeReview.Config(
                vcsRoot = DslContext.settingsRoot,
                type = projectType,
                sonarScanConfig = sonarScanConfig,
            )
        )
    )

}
