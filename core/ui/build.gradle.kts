plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.example.ecom.core.ui"
    compileSdk = 37

    defaultConfig {
        minSdk = 24
    }

    // Mỗi layout dùng chung là 1 thư mục riêng: src/main/layouts/<tên>/res (layout, menu, icon, chuỗi của layout đó)
    sourceSets {
        getByName("main") {
            file("src/main/layouts").listFiles()?.filter { it.isDirectory }?.sorted()?.forEach {
                res.srcDir(it.resolve("res"))
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // api: BaseFragment / BaseViewModel dùng ApiCallback (thuộc :core:api)
    api(project(":core:api"))
    // api: layout dùng chung tự điều hướng (nút back, bottom bar, tìm kiếm) qua AppRouter
    api(project(":core:navigation"))
    api(libs.appcompat)
    api(libs.material)
    // ViewModel + LiveData: "custom hook" giữ state của mỗi trang (cùng bản appcompat đang kéo về)
    api(libs.lifecycle.viewmodel)
    api(libs.lifecycle.livedata)
}
