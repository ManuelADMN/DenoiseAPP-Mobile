// app/build.gradle.kts
import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
    // Necesario para supabase-kt (usa kotlinx-serialization por defecto)
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.denoise.denoiseapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.denoise.denoiseapp"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        // Lee las credenciales públicas (no las subas al repo)
        val lp = gradleLocalProperties(rootDir, providers)
        val supabaseUrl = lp.getProperty("SUPABASE_URL")
            ?: providers.gradleProperty("SUPABASE_URL").orNull
            ?: providers.environmentVariable("SUPABASE_URL").orNull
            ?: ""
        val supabaseAnon = lp.getProperty("SUPABASE_ANON_KEY")
            ?: providers.gradleProperty("SUPABASE_ANON_KEY").orNull
            ?: providers.environmentVariable("SUPABASE_ANON_KEY").orNull
            ?: ""

        buildConfigField("String", "https://emszmcrrhfjnszqmgsdo.supabase.co", "\"$supabaseUrl\"")
        buildConfigField("String", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImVtc3ptY3JyaGZqbnN6cW1nc2RvIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjE3NjQxMDAsImV4cCI6MjA3NzM0MDEwMH0.Tq7JrDKFPlGSFXBej0vmvYYvbli-CoAap0Mo7INykpM", "\"$supabaseAnon\"")
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

    // Recomendado si minSdk < 26 (ver docs de supabase-kt)
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions { jvmTarget = "17" }

    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

dependencies {
    // ===== Supabase-kt (BOM 3.x) =====
    implementation(platform("io.github.jan-tennert.supabase:bom:3.2.5"))
    implementation("io.github.jan-tennert.supabase:postgrest-kt")
    implementation("io.github.jan-tennert.supabase:auth-kt")
    implementation("io.github.jan-tennert.supabase:storage-kt")
    implementation("io.github.jan-tennert.supabase:realtime-kt")

    // Ktor 3 para Android + WebSockets (necesario para Realtime)
    implementation("io.ktor:ktor-client-okhttp:3.0.3")
    implementation("io.ktor:ktor-client-websockets:3.0.3")

    // JSON (kotlinx-serialization)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    // ===== Tu stack existente (catálogo) =====
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

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.coil.compose)

    // Desugaring (porque minSdk = 24)
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.2")

    // Tests
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
