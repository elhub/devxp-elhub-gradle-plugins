import no.elhub.devxp.build.configuration.pipeline.ElhubProject.Companion.elhubProject
import no.elhub.devxp.build.configuration.pipeline.constants.Group.DEVXP
import no.elhub.devxp.build.configuration.pipeline.constants.ArtifactoryRepository
import no.elhub.devxp.build.configuration.pipeline.jobs.gradleAutoRelease
import no.elhub.devxp.build.configuration.pipeline.jobs.gradleVerify

elhubProject(DEVXP, "devxp-elhub-gradle-plugins") {
    pipeline {
        sequential {
            val artifacts = gradleVerify {
                analyzeDependencies = false
            }
            gradleAutoRelease(artifacts = listOf(artifacts)) {
                repository = ArtifactoryRepository.ELHUB_PLUGINS_RELEASE_LOCAL
            }
        }
    }
}
