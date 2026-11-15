plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.example.ecom.feature.order"
    compileSdk = 37

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(project(":core:data"))
    implementation(project(":core:ui"))
    implementation(project(":core:navigation"))
    implementation(libs.constraintlayout)
}
