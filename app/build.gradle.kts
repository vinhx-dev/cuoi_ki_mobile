plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.ecom"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.example.ecom"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)

    implementation(project(":core:data"))
    implementation(project(":core:ui"))
    implementation(project(":core:navigation"))

    // :app phải biết mọi feature để đóng gói chúng vào APK
    implementation(project(":feature:splash"))
    implementation(project(":feature:auth"))
    implementation(project(":feature:home"))
    implementation(project(":feature:product"))
    implementation(project(":feature:order"))
    implementation(project(":feature:profile"))
    implementation(project(":feature:admin"))
}
