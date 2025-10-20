pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com.android.*")
                includeGroupByRegex("com.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "LQR"
include(":core")
include(":renderScript")
include(":base")
include(":mobile")
include(":clip")
include(":fileChooser")
include(":colorRes")
include(":widget")
include(":pigment")
include(":privacy")
include(":faceIcon")
include(":palette")
include(":fragmentHelper")
include(":insets")
