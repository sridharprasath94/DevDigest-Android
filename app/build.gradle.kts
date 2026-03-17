plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.navigation.safe.args)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.flash.devdigest"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.flash.devdigest"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    // Core

    // UI
    implementation(libs.material)
    implementation(libs.androidx.browser)
    implementation(libs.androidx.swiperefreshlayout)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // Navigation
    implementation(libs.android.navigation.fragment)
    implementation(libs.android.navigation.ui)

    // Dependency Injection
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Data - Network
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    // Data - Database
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Core - Paging
    implementation(libs.androidx.paging.runtime)
    implementation(libs.androidx.room.paging)

    // Utilities
    implementation(libs.dev.android.broadcast.vbpd)
    implementation(libs.dev.android.broadcast.vbpd.reflection)

    // Testing
    implementation(libs.androidx.junit.ktx)
    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)

    // Debug
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)


    // Unused / Optional dependencies (kept for reference)
    //    implementation(libs.androidx.core.ktx)
    //    implementation(libs.androidx.lifecycle.runtime.ktx)
    //    implementation(libs.androidx.activity.compose)
    //    implementation(libs.androidx.google.fonts)
    //    implementation(libs.androidx.appcompat)

    //    implementation(libs.androidx.recyclerview)
    //    implementation(libs.androidx.hilt.navigation.compose)
    //    implementation(libs.coil)

    //    androidTestImplementation(libs.androidx.junit)
    //    androidTestImplementation(libs.androidx.espresso.core)
    //    androidTestImplementation(libs.room.testing)
    //    androidTestImplementation(platform(libs.androidx.compose.bom))
    //    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}