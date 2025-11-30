plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // El plugin de KSP se aplica con alias para usar la versión correcta del catálogo
    alias(libs.plugins.com.google.devtools.ksp)
}

android {
    namespace = "com.denoise.denoiseapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.denoise.denoiseapp"
        minSdk = 26 // Mínimo Android 8.0 (Oreo) para soportar LocalDateTime y otras APIs modernas
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    // Configuración de Compose para Kotlin 1.9.x
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8" // Compatible con Kotlin 1.9.22
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // --- CORE & LIFECYCLE ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)

    // --- COMPOSE UI ---
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // Iconos extendidos (necesario para íconos como Map, Directions, etc.)
    implementation("androidx.compose.material:material-icons-extended:1.6.0")

    // --- NAVIGATION ---
    implementation(libs.androidx.navigation.compose)

    // --- ROOM (BASE DE DATOS LOCAL) ---
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler) // Procesador de anotaciones con KSP

    // --- COIL (CARGA DE IMÁGENES) ---
    implementation(libs.coil.compose)

    // --- RETROFIT (RED / API) ---
    implementation(libs.squareup.retrofit)
    implementation(libs.squareup.retrofit.gson)
    implementation(libs.squareup.okhttp.logging)

    // --- GEOLOCALIZACIÓN (GOOGLE PLAY SERVICES) ---
    implementation(libs.play.services.location)

    // --- OSMDROID (MAPAS OPENSTREETMAP) ---
    implementation(libs.osmdroid.android)

    // --- TESTING ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}