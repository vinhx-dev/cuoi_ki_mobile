plugins {
    alias(libs.plugins.android.library)
}

// Lưu trữ cục bộ trên máy (≈ localStorage): token, thông tin user, cài đặt...
android {
    namespace = "com.example.ecom.core.data"
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
}
