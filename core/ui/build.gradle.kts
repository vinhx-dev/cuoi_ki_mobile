plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.example.ecom.core.ui"
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
    // api: BaseFragment.apiCallback() trả về ApiCallback (thuộc :core:data)
    api(project(":core:data"))
    api(libs.appcompat)
    api(libs.material)
}
