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
    compileSdk = 35

    defaultConfig {
        applicationId = "com.denoise.denoiseapp"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        // ⚙️ Lee primero de local.properties (AGP 8.6+ requiere 'providers'),
        // luego de gradle.properties y por último de variables de entorno.
        val lp = gradleLocalProperties(rootDir, providers)
        val supabaseUrl = lp.getProperty("SUPABASE_URL")
            ?: providers.gradleProperty("SUPABASE_URL").orNull
            ?: providers.environmentVariable("SUPABASE_URL").orNull
            ?: ""
        val supabaseAnon = lp.getProperty("SUPABASE_ANON_KEY")
            ?: providers.gradleProperty("SUPABASE_ANON_KEY").orNull
            ?: providers.environmentVariable("SUPABASE_ANON_KEY").orNull
            ?: ""

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
        debug {
            // Para dev HTTP puedes habilitar cleartext en el Manifest.
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        // Debe coincidir con libs.versions.compose-compiler
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Supabase-kt (BOM + módulos)
    implementation(platform("io.github.jan-tennert.supabase:bom:2.5.6"))
    implementation("io.github.jan-tennert.supabase:postgrest-kt")
    implementation("io.github.jan-tennert.supabase:auth-kt")
    implementation("io.github.jan-tennert.supabase:storage-kt")
    implementation("io.github.jan-tennert.supabase:realtime-kt")

    // Ktor engine para Android
    implementation("io.ktor:ktor-client-android:2.3.9")

    // JSON (kotlinx-serialization)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    // Compose (BOM del catálogo)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // Core / Lifecycle / Activity / Navigation
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)

    // Room (KSP)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Imágenes
    implementation(libs.coil.compose)

    // Tests
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
