import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)

    // KSP (versión que SÍ existe para Kotlin 2.0.21)
    id("com.google.devtools.ksp") version "2.0.21-1.0.27"
}

android {
    namespace = "com.denoise.denoiseapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.denoise.denoiseapp"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        // Si NO usas Supabase, puedes borrar todo este bloque:
        val lp = gradleLocalProperties(rootDir, providers)
        val supabaseUrl = lp.getProperty("SUPABASE_URL") ?: ""
        val supabaseAnon = lp.getProperty("SUPABASE_ANON_KEY") ?: ""
        buildConfigField("String", "SUPABASE_URL", "\"$supabaseUrl\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"$supabaseAnon\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
        isCoreLibraryDesugaringEnabled = true
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

// Parámetros de Room para KSP
ksp {
    arg("room.generateKotlin", "true")
    arg("room.incremental", "true")
}

dependencies {
    // ===== Room estable + KSP =====
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.compose.ui.unit)
    ksp(libs.androidx.room.compiler)

    // ===== Tu stack Compose del catálogo =====
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)

    implementation(libs.coil.compose)
    implementation("androidx.compose.material:material-icons-extended:<versión>")
    implementation("androidx.compose.material3:material3:<última>")
    implementation("androidx.compose.material:material-icons-extended:<última>")




    // JSON (si lo usas)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    // Desugaring
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.2")

    // Tests
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
