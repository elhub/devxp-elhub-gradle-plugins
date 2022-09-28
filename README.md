# devxp-elhub-gradle-plugins
[<img src="https://img.shields.io/badge/repo-github-blue" alt="">](https://github.com/elhub/devxp-elhub-gradle-plugins)
[<img src="https://img.shields.io/badge/issues-jira-orange" alt="">](https://jira.elhub.cloud/issues/?jql=project%20%3D%20%22Team%20Dev%22%20AND%20component%20%3D%20devxp-elhub-gradle-plugins%20AND%20status%20!%3D%20Done)
[<img src="https://teamcity.elhub.cloud/app/rest/builds/buildType:(id:DevXp_DevXpElhubGradlePlugins_AutoRelease)/statusIcon" alt="">](https://teamcity.elhub.cloud/project/DevXp_DevXpElhubGradlePlugins?mode=builds#all-projects)
[<img src="https://sonar.elhub.cloud/api/project_badges/measure?project=no.elhub.devxp%3Adevxp-elhub-gradle-plugins&metric=alert_status" alt="">](https://sonar.elhub.cloud/dashboard?id=no.elhub.devxp%3Adevxp-elhub-gradle-plugins)
[<img src="https://sonar.elhub.cloud/api/project_badges/measure?project=no.elhub.devxp%3Adevxp-elhub-gradle-plugins&metric=ncloc" alt="">](https://sonar.elhub.cloud/dashboard?id=no.elhub.devxp%3Adevxp-elhub-gradle-plugins)
[<img src="https://sonar.elhub.cloud/api/project_badges/measure?project=no.elhub.devxp%3Adevxp-elhub-gradle-plugins&metric=bugs" alt="">](https://sonar.elhub.cloud/dashboard?id=no.elhub.devxp%3Adevxp-elhub-gradle-plugins)
[<img src="https://sonar.elhub.cloud/api/project_badges/measure?project=no.elhub.devxp%3Adevxp-elhub-gradle-plugins&metric=vulnerabilities" alt="">](https://sonar.elhub.cloud/dashboard?id=no.elhub.devxp%3Adevxp-elhub-gradle-plugins)
[<img src="https://sonar.elhub.cloud/api/project_badges/measure?project=no.elhub.devxp%3Adevxp-elhub-gradle-plugins&metric=coverage" alt="">](https://sonar.elhub.cloud/dashboard?id=no.elhub.devxp%3Adevxp-elhub-gradle-plugins)

## About

devxp-elhub-gradle-plugins is a multi-module repository that contains plugins to streamline the development of products
built with gradle at Elhub. It contains the common build logic for a variety of different types of Kotlin/Jvm
components.

## Getting Started

### Prerequisites

The devxp-elhub-gradle-plugins are used together with Gradle projects at Elhub.

### Installation

The individual plugins are installed in your gradle build files by making them available in the settings.gradle.kts
and build.gradle.kts files.

In your settings.gradle.kts add a pluginManagement block to ensure the code can use your local plugin repository.

```kts
pluginManagement {
    repositories {
        maven(url = "https://jfrog.elhub.cloud/artifactory/elhub-mvn")
        gradlePluginPortal()
    }
}
```

Then add the plugin you want to use in the plugins block of the build.gradle.kts file:

```kts
plugins {
    id("no.elhub.devxp.kotlin-XYZ") version "1.0.0"
}
```

## Usage

Depending on the plugin deployed, you will find a number of useful plugins, repositories and tasks added to the project
suitable to your use-case. The plugins available are organized as follows:

* `no.elhub.devxp.kotlin-core` - Sets up generic defaults that should be common for all Kotlin/Jvm projects at Elhub.
  You will not normally apply the kotlin-core plugin directly, but rather use one of the derived plugins.
* `no.elhub.devxp.kotlin-application` - Configures the build file to enable builds of fat Jars, package applications,
  and deploying the resulting distributions to the appropriate maven repository.
* `no.elhub.devxp.kotlin-library` - Configures the build file to pack jar files and deploy them to the appropriate maven
  repository.
* `no.elhub.devxp.kotlin-services` - Configures the build file for light-weight API deployment.

Applying the appropriate plugin automatically con


## Testing

All of the plugins can be tested using Gradle:

    ./gradlew test

## Contributing

Contributing, issues and feature requests are welcome. See the
[Contributing](https://code.elhub.cloud/projects/COM/repos/devxp-build-configuration/browse/CONTRIBUTING.md) file.

## Owners

This project is developed by [Elhub](https://elhub.no). For the specific development group responsible for this
code, see the [CodeOwners](https://code.elhub.cloud/projects/COM/repos/devxp-build-configuration/browse/CODEOWNERS)
file.
