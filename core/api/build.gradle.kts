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
// Emulator: 10.0.2.2 points to the host computer. For a USB device run
// adb reverse tcp:8080 tcp:8080 and set api.baseUrl=http://localhost:8080/api/
// in local.properties, then rebuild.
val baseUrl: String = localProperties.getProperty("api.baseUrl") ?: "http://10.0.2.2:8080/api/"

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
    api("com.squareup.retrofit2:retrofit:2.11.0")
    api("com.squareup.retrofit2:converter-gson:2.11.0")
    api("com.squareup.okhttp3:logging-interceptor:4.12.0")
    testImplementation("junit:junit:4.13.2")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
    // api: lấy token đăng nhập từ bộ nhớ cục bộ (SessionManager)
    api(project(":core:data"))
}
