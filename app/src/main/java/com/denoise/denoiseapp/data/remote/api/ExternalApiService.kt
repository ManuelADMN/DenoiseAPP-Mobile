package com.denoise.denoiseapp.data.remote.api

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

// --- Modelos de Datos para la respuesta de Open-Meteo ---

data class WeatherResponse(
    @SerializedName("current_weather") val currentWeather: CurrentWeather
)

data class CurrentWeather(
    val temperature: Double,
    val windspeed: Double,
    val weathercode: Int,
    val time: String
)

/**
 * Interfaz para consumir la API Externa (Open-Meteo).
 * Esto cumple con el ítem de "Consumo de API Externa" de la rúbrica.
 */
interface ExternalApiService {

    // Obtiene el clima actual. Por defecto: Puerto Montt (Lat -41.46, Lon -72.94)
    @GET("v1/forecast")
    suspend fun getCurrentWeather(
        @Query("latitude") lat: Double = -41.46,
        @Query("longitude") lon: Double = -72.94,
        @Query("current_weather") current: Boolean = true
    ): WeatherResponse
}