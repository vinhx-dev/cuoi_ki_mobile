import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { load(it) }
    }
}
val baseUrl: String = localProperties.getProperty("api.baseUrl") ?: "http://10.0.2.2:8080/api"

android {
    namespace = "com.example.ecom.core.api"
    compileSdk = 37

    defaultConfig {
        minSdk = 24
        buildConfigField("String", "BASE_URL", "\"$baseUrl\"")
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // api: lấy token đăng nhập từ bộ nhớ cục bộ (SessionManager)
    api(project(":core:data"))
}
