package com.denoise.denoiseapp.core.util

import com.google.android.gms.tasks.Task
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Espera un Task<T> de Google Play Services/Firebase sin usar *-ktx.
 * No intenta cancelar el Task (no existe cancelación pública); solo respeta
 * la cancelación de la corrutina.
 */
suspend fun <T> Task<T>.awaitResult(): T = suspendCancellableCoroutine { cont ->
    addOnSuccessListener { result ->
        if (!cont.isCompleted) cont.resume(result)
    }
    addOnFailureListener { e ->
        if (!cont.isCompleted) cont.resumeWithException(e)
    }
    // Al cancelar la corrutina no hay API pública para cancelar el Task, así que no hacemos nada.
    cont.invokeOnCancellation {
        // no-op
    }
}
