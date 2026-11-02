import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

val appVersion = Properties().apply {
    rootProject.file("Version.xcconfig").reader(Charsets.UTF_8).use { load(it) }
}

android {
    namespace = "tk.horiuchi.tatekaemaru"
    compileSdk = 36

    defaultConfig {
        applicationId = "tk.horiuchi.tatekaemaru"
        minSdk = 26
        targetSdk = 36
        versionCode = appVersion.getProperty("APP_BUILD").toInt()
        versionName = appVersion.getProperty("APP_VERSION")
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
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
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
    implementation(project(":shared"))
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
