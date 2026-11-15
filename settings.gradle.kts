pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
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

rootProject.name = "ecom_app"

// App shell: MainActivity, header, bottom nav, theme
include(":app")

// Core: code dùng chung cho mọi feature
include(":core:data")        // gọi API, model, repository, session
include(":core:ui")          // BaseFragment, AppShell, màu & chuỗi dùng chung
include(":core:navigation")  // nav_graph, AppRouter, LayoutType

// Feature: mỗi màn hình/nhóm màn hình là 1 module, mỗi người/nhóm phụ trách 1 module
include(":feature:splash")
include(":feature:auth")
include(":feature:home")
include(":feature:product")
include(":feature:order")
include(":feature:profile")
 