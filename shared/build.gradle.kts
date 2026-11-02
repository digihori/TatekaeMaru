plugins {
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
}

// Single source of truth for both apps; changes to the Markdown invalidate this task.
val policySource = rootProject.layout.projectDirectory.file("PRIVACY.ja.md")
val generatedPolicyDirectory = layout.buildDirectory.dir("generated/privacyPolicy/kotlin")
val generatePrivacyPolicy by tasks.registering {
    inputs.file(policySource).withPathSensitivity(PathSensitivity.RELATIVE)
    outputs.dir(generatedPolicyDirectory)
    doLast {
        val text = policySource.asFile.readText(Charsets.UTF_8)
        require(text.isNotBlank()) { "PRIVACY.ja.md must not be empty" }
        val literal = buildString {
            append('"')
            text.forEach { character ->
                when (character) {
                    '\\' -> append("\\\\")
                    '"' -> append("\\\"")
                    '\n' -> append("\\n")
                    '\r' -> append("\\r")
                    '\t' -> append("\\t")
                    '$' -> append('\\').append('$')
                    else -> append(character)
                }
            }
            append('"')
        }
        val output = generatedPolicyDirectory.get().file(
            "tk/horiuchi/tatekaemaru/ui/BundledPrivacyPolicy.kt"
        ).asFile
        output.parentFile.mkdirs()
        output.writeText(
            "// Generated from PRIVACY.ja.md. Do not edit.\n" +
                "package tk.horiuchi.tatekaemaru.ui\n\n" +
                "object BundledPrivacyPolicy {\n    const val Text: String = $literal\n}\n",
            Charsets.UTF_8
        )
    }
}

kotlin {
    androidTarget()
    iosArm64 {
        binaries.framework { baseName = "TatekaeShared"; isStatic = true }
    }
    iosSimulatorArm64 {
        binaries.framework { baseName = "TatekaeShared"; isStatic = true }
    }
    jvmToolchain(21)
    sourceSets {
        commonMain { kotlin.srcDir(generatePrivacyPolicy) }
        androidMain.dependencies { implementation(libs.androidx.activity.compose) }
        commonMain.dependencies {
            api(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.json)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            api(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
        }
    }
}

android {
    namespace = "tk.horiuchi.tatekaemaru.shared"
    compileSdk = 36
    defaultConfig { minSdk = 26 }
}
