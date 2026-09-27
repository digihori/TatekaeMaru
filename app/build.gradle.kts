plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "tk.horiuchi.tatekaemaru"
    compileSdk = 36

    defaultConfig {
        applicationId = "tk.horiuchi.tatekaemaru"
        minSdk = 26
        targetSdk = 36
        versionCode = 4
        versionName = "1.1.2"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }

        release {
            isMinifyEnabled = false
        }
    }
}

kotlin {
    jvmToolchain(21)
}

afterEvaluate {
    val appName =
        android.defaultConfig.applicationId
            ?.substringAfterLast(".")
            ?: "app"

    val version =
        android.defaultConfig.versionName
            ?: "0.0.0"

    base {
        archivesName.set("${appName}-${version}")
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    testImplementation(libs.junit)
}
