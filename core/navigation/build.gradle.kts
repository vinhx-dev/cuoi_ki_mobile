plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.example.ecom.core.navigation"
    compileSdk = 37

    defaultConfig {
        minSdk = 24
        // Rule giữ tên Fragment, tự áp dụng cho app khi bật minify
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    api(libs.navigation.fragment)
}
