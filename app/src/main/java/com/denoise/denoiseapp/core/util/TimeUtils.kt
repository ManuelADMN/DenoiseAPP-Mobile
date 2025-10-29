package com.denoise.denoiseapp.core.util

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.Instant

@RequiresApi(Build.VERSION_CODES.O)
fun ahora(): Instant =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        Instant.now()
    } else {
        // Convertir epochMillis (long) a Instant manualmente
        Instant.ofEpochMilli(System.currentTimeMillis())
    }
