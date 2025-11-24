package com.denoise.denoiseapp.presentation.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.denoise.denoiseapp.data.remote.RetrofitClient
import com.denoise.denoiseapp.data.remote.api.CurrentWeather
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Estado de la UI para la pantalla de Clima
data class WeatherUiState(
    val loading: Boolean = false,
    val weather: CurrentWeather? = null,
    val error: String? = null
)

class WeatherViewModel : ViewModel() {

    private val _state = MutableStateFlow(WeatherUiState())
    val state: StateFlow<WeatherUiState> = _state.asStateFlow()

    init {
        // Cargar clima automáticamente al iniciar el ViewModel
        fetchWeather()
    }

    fun fetchWeather() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                // Llamada a la API Externa usando Retrofit
                val response = RetrofitClient.externalApi.getCurrentWeather()
                _state.update { it.copy(loading = false, weather = response.currentWeather) }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = "Error al conectar: ${e.message}") }
            }
        }
    }
}