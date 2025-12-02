package com.denoise.denoiseapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.denoise.denoiseapp.ui.navigation.AppNavGraph
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializa Firebase (el FirebaseInitProvider lo hace, pero esto es seguro)
        try {
            FirebaseApp.initializeApp(this)
        } catch (_: Throwable) { /* no-op */ }

        setContent {
            // AppNavGraph ya aplica el tema internamente
            AppNavGraph()
        }
    }
}
