package com.denoise.denoiseapp.data.remote.api

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

// Estructura principal de la respuesta de la API
data class WeatherResponse(
    @SerializedName("current_weather") val currentWeather: CurrentWeather? = null,
    @SerializedName("daily") val daily: DailyForecast? = null
)

// Datos del clima actual (Temperatura, Viento, Código de clima)
data class CurrentWeather(
    val temperature: Double,
    val windspeed: Double,
    val weathercode: Int,
    val time: String
)

// Datos del pronóstico diario
data class DailyForecast(
    val time: List<String>, // Lista de fechas
    @SerializedName("temperature_2m_max") val temperatureMax: List<Double>,
    @SerializedName("temperature_2m_min") val temperatureMin: List<Double>,

    // --- CAMPO CRÍTICO AGREGADO ---
    // Lista de códigos de clima para cada día (0=Soleado, 1=Nublado, etc.)
    // Es opcional (nullable) por seguridad, pero la API lo enviará si lo pedimos.
    @SerializedName("weathercode") val weatherCode: List<Int>? = null
)

interface ExternalApiService {

    // Llamada a la API Open-Meteo
    // Agregamos 'weathercode' a la lista de parámetros solicitados en 'daily'
    @GET("v1/forecast")
    suspend fun getWeather(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("current_weather") current: Boolean = true,
        // Aquí pedimos explícitamente: Max Temp, Min Temp y WeatherCode
        @Query("daily") daily: String = "temperature_2m_max,temperature_2m_min,weathercode",
        @Query("timezone") timezone: String = "auto"
    ): WeatherResponse
}