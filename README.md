# devxp-elhub-gradle-plugins
[<img src="https://img.shields.io/badge/repo-github-blue" alt="">](https://github.com/elhub/devxp-elhub-gradle-plugins)
[<img src="https://img.shields.io/badge/issues-jira-orange" alt="">](https://jira.elhub.cloud/issues/?jql=project%20%3D%20%22Team%20Dev%22%20AND%20component%20%3D%20devxp-elhub-gradle-plugins%20AND%20status%20!%3D%20Done)
[<img src="https://teamcity.elhub.cloud/app/rest/builds/buildType:(id:DevXp_DevXpElhubGradlePlugins_AutoRelease)/statusIcon" alt="">](https://teamcity.elhub.cloud/project/DevXp_DevXpElhubGradlePlugins?mode=builds#all-projects)

## About

devxp-elhub-gradle-plugins is a repository that contains plugins to streamline the development of products
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
        maven(url = "https://jfrog.elhub.cloud/artifactory/elhub-plugins")
    }
}
```

Then add the plugin you want to use in the plugins block of the build.gradle.kts file:

```kts
plugins {
    id("no.elhub.devxp.kotlin-XYZ") version "0.2.1"
}
```

## Usage

Depending on the plugin deployed, you will find a number of useful plugins, repositories and tasks added to the project
suitable to your use-case. The plugins available are organized as follows:

* `no.elhub.devxp.kotlin-core` - Sets up generic defaults that should be common for all Kotlin/Jvm projects at Elhub.
  You will not normally apply the kotlin-core plugin directly, but rather use one of the derived plugins mentioned below.
* `no.elhub.devxp.kotlin-application` - Configures the build file to enable builds of fat Jars, package applications,
  and deploying the resulting distributions to the appropriate maven repository.
* `no.elhub.devxp.kotlin-library` - Configures the build file to pack jar files and deploy them to the appropriate maven
  repository.
* `no.elhub.devxp.kotlin-service` - Configures the build file for light-weight API deployment.
* `no.elhub.devxp.java-platform` - Configures the build file for java platform.

Applying the appropriate plugin automatically configures build file.
OWASP dependency check comes with all the above plugins, You can run dependency-check by executing:

```html
./gradlew dependencyCheckAnalyze
```

## Testing

All the plugins can be tested using Gradle:

```bash
    ./gradlew test
```

## Contributing

Contributing, issues and feature requests are welcome! See the [Contributing](https://github.com/elhub/devxp/blob/main/.github/CONTRIBUTING) file.

## Owners

This project is developed by [Elhub](https://www.elhub.no). For the specific development group responsible for this
code, see the [Codeowners](https://github.com/elhub/devxp/blob/main/.github/CODEOWNERS) file.
