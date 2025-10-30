package com.denoise.denoiseapp.core.di

import com.denoise.denoiseapp.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.PropertyConversionMethod
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage
import io.ktor.client.engine.okhttp.OkHttp

object SupabaseModule {

    val client: SupabaseClient by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY
        ) {
            // Ktor 3 (OkHttp) con WebSockets para Realtime
            httpEngine = OkHttp.create()

            install(Auth)
            install(Postgrest) {
                // Usa los @SerialName de tus DTOs (snake_case en DB)
                // Valor por defecto del lib es CAMEL_CASE_TO_SNAKE_CASE
                propertyConversionMethod = PropertyConversionMethod.SERIAL_NAME
            }
            install(Storage)
            install(Realtime)
        }
    }
}
