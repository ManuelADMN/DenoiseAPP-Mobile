plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // KSP
    alias(libs.plugins.com.google.devtools.ksp)

    // Firebase
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

android {
    namespace = "com.denoise.denoiseapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.denoise.denoiseapp"
        minSdk = 26 // Android 8.0+
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }
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
    kotlinOptions { jvmTarget = "1.8" }

    // Compose para Kotlin 1.9.x
    buildFeatures { compose = true }
    composeOptions { kotlinCompilerExtensionVersion = "1.5.8" } // compatible con Kotlin 1.9.22

    packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }
}

// Parámetros de Room para KSP (crea carpeta app/schemas si no existe)
ksp {
    arg("room.incremental", "true")
    arg("room.expandProjection", "true")
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    // --- Firebase (BoM 33.5.1 para compatibilidad con Kotlin 1.9.x) ---
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore)     // Base de datos (Cloud Firestore)
    implementation(libs.firebase.crashlytics)   // Crash reports
    // (Opcional) Realtime Database:
    // implementation(libs.firebase.database)
    // (Deja fuera Analytics para evitar el conflicto Kotlin 2.1 de measurement 23.x)

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
    implementation("androidx.compose.material:material-icons-extended:1.6.0")

    // --- NAVIGATION ---
    implementation(libs.androidx.navigation.compose)

    // --- ROOM ---
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // --- COIL ---
    implementation(libs.coil.compose)

    // --- RETROFIT / OKHTTP ---
    implementation(libs.squareup.retrofit)
    implementation(libs.squareup.retrofit.gson)
    implementation(libs.squareup.okhttp.logging)

    // --- GPS ---
    implementation(libs.play.services.location)

    // --- OSMDROID ---
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
