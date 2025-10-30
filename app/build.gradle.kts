// app/build.gradle.kts
import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.serialization")
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

        // Lee credenciales públicas desde local.properties / gradle.properties / env
        val lp = gradleLocalProperties(rootDir, providers)
        val supabaseUrl = lp.getProperty("SUPABASE_URL")
            ?: providers.gradleProperty("SUPABASE_URL").orNull
            ?: providers.environmentVariable("SUPABASE_URL").orNull
            ?: ""
        val supabaseAnon = lp.getProperty("SUPABASE_ANON_KEY")
            ?: providers.gradleProperty("SUPABASE_ANON_KEY").orNull
            ?: providers.environmentVariable("SUPABASE_ANON_KEY").orNull
            ?: ""

        // 👇 Nombres correctos de las constantes en BuildConfig (no pongas la URL aquí)
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions { jvmTarget = "17" }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    // ❌ Quita la línea con libs.versions.compose.compiler.get():
    // composeOptions { kotlinCompilerExtensionVersion = ... }
    // El plugin Compose ya fija la versión del compiler.

    packaging { resources.excludes += "/META-INF/{AL2.0,LGPL2.1}" }
}

dependencies {
    // ===== Supabase-kt 3.x (usa BOM para alinear módulos) =====
    implementation(platform("io.github.jan-tennert.supabase:bom:3.3.1"))
    implementation("io.github.jan-tennert.supabase:postgrest-kt")
    implementation("io.github.jan-tennert.supabase:auth-kt")
    implementation("io.github.jan-tennert.supabase:storage-kt")
    implementation("io.github.jan-tennert.supabase:realtime-kt")

    // Ktor 3 para Android + WebSockets (Realtime los usa)  ✅
    implementation("io.ktor:ktor-client-okhttp:3.3.1")
    implementation("io.ktor:ktor-client-websockets:3.3.1")

    // JSON (kotlinx-serialization)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    // ===== Tu stack del catálogo =====
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

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.2")

    // Tests
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
