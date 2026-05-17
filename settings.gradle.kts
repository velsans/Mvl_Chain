pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

/**
 * Lets Gradle download a matching JDK (e.g. 17) when none is installed locally.
 * See https://docs.gradle.org/current/userguide/toolchains.html#sub:download_repositories
 */
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "MVLChain"
include(":app")
include(":domain")
include(":data")
