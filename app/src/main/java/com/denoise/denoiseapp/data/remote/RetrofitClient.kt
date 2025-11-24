package com.denoise.denoiseapp.data.remote

import com.denoise.denoiseapp.data.remote.api.DenoiseApiService
import com.denoise.denoiseapp.data.remote.api.ExternalApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // URL 1: Tus Microservicios (Spring Boot)
    // Usa 10.0.2.2 si es emulador, o tu IP local (ej. 192.168.1.X) si es celular físico.
    private const val BASE_URL_MICROSERVICE = "http://192.168.31.204:8080/"

    // URL 2: API Externa (Open-Meteo)
    private const val BASE_URL_EXTERNAL = "https://api.open-meteo.com/"

    // Cliente HTTP con logs para ver qué pasa en la consola
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    // Instancia Retrofit para TUS Microservicios
    val denoiseApi: DenoiseApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_MICROSERVICE)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DenoiseApiService::class.java)
    }

    // Instancia Retrofit para la API Externa (Clima)
    val externalApi: ExternalApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_EXTERNAL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ExternalApiService::class.java)
    }
}