import no.elhub.devxp.build.configuration.pipeline.constants.ArtifactoryRepository.ELHUB_PLUGINS_RELEASE_LOCAL
import no.elhub.devxp.build.configuration.pipeline.constants.Group.DEVXP
import no.elhub.devxp.build.configuration.pipeline.dsl.elhubProject
import no.elhub.devxp.build.configuration.pipeline.jobs.gradlePublish
import no.elhub.devxp.build.configuration.pipeline.jobs.gradleVerify

elhubProject(DEVXP, "devxp-elhub-gradle-plugins") {
    pipeline {
        sequential {
            val artifacts = gradleVerify {
                analyzeDependencies = false
                enablePublishMetrics = true
            }
            gradlePublish(artifacts = listOf(artifacts)) {
                repository = ELHUB_PLUGINS_RELEASE_LOCAL
            }
        }
    }
}
