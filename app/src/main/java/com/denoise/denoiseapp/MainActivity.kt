package com.denoise.denoiseapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.denoise.denoiseapp.core.ui.theme.DenoiseTheme
import com.denoise.denoiseapp.ui.navigation.AppNavGraph

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DenoiseTheme {
                AppNavGraph()
            }
        }
    }
}
