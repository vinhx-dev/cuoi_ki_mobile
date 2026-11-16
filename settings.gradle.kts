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

// App: chỉ có MainActivity để hiển thị navigation
include(":app")

// Core: code dùng chung cho mọi feature
include(":core:data")        // lưu trữ cục bộ trên máy (≈ localStorage): LocalStorage, SessionManager
include(":core:api")         // 1 hàm gọi API chung cho cả app: JSON + gửi ảnh (multipart)
include(":core:navigation")  // khai báo trang (layout, cần đăng nhập) + điều hướng: AppPages, AppRouter
include(":core:ui")          // UI dùng chung: layouts/ (none, main, detail, admin), BaseFragment, BaseViewModel

// Feature: giao diện + Java + dữ liệu riêng (data/, ViewModel) của từng chức năng; mỗi người/nhóm phụ trách 1 module
include(":feature:splash")
include(":feature:auth")
include(":feature:home")
include(":feature:product")
include(":feature:order")
include(":feature:profile")
 