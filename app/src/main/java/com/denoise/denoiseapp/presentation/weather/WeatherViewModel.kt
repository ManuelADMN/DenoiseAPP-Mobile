package com.denoise.denoiseapp.presentation.weather

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.denoise.denoiseapp.data.remote.RetrofitClient
import com.denoise.denoiseapp.data.remote.api.CurrentWeather
import com.denoise.denoiseapp.data.remote.api.DailyForecast
import com.denoise.denoiseapp.data.repository.MarkerRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint

// Enum para los tipos de marcadores
enum class MarkerType {
    DEFAULT, WARNING, CHECK, INFO
}

// Modelo de datos para un marcador en el mapa
data class MapMarker(
    val id: Long = 0, // ID 0 para que Room genere uno nuevo al insertar
    val location: GeoPoint,
    val title: String = "Punto Personalizado",
    val type: MarkerType = MarkerType.DEFAULT,
    // Campo auxiliar para guardar el clima de este marcador (no persistente en BD, solo memoria)
    val cachedWeather: CurrentWeather? = null
)

// Estado de la UI para la pantalla del clima
data class WeatherUiState(
    val loading: Boolean = false,
    val weather: CurrentWeather? = null, // Clima de tu ubicación
    val forecast: DailyForecast? = null, // Pronóstico de tu ubicación
    val error: String? = null,
    val lat: Double = -41.46,
    val lon: Double = -72.94,
    val markers: List<MapMarker> = emptyList(), // Lista de marcadores (viene de BD)

    // Estados para la UI de lista de puntos y detalles
    val isLoadingMarkersWeather: Boolean = false,
    val selectedMarkerForecast: DailyForecast? = null
)

// Heredamos de AndroidViewModel para tener acceso al 'application' y la BD
class WeatherViewModel(application: Application) : AndroidViewModel(application) {

    private val markerRepository = MarkerRepository(application)
    private val _state = MutableStateFlow(WeatherUiState())
    val state: StateFlow<WeatherUiState> = _state.asStateFlow()

    init {
        fetchWeather()
        observeMarkers() // Iniciamos la escucha de la Base de Datos
    }

    // Escucha cambios en la tabla de marcadores y actualiza el estado
    private fun observeMarkers() {
        viewModelScope.launch {
            markerRepository.getMarkers().collectLatest { markersFromDb ->
                // Lógica extra: Si ya teníamos clima cargado en memoria para estos puntos, tratamos de conservarlo
                // para que no desaparezca al actualizarse la lista desde la BD.
                val currentMarkers = _state.value.markers
                val mergedMarkers = markersFromDb.map { dbMarker ->
                    val existing = currentMarkers.find { it.id == dbMarker.id }
                    if (existing?.cachedWeather != null) {
                        dbMarker.copy(cachedWeather = existing.cachedWeather)
                    } else {
                        dbMarker
                    }
                }
                _state.update { it.copy(markers = mergedMarkers) }
            }
        }
    }

    // Carga el clima principal (Tu ubicación)
    fun fetchWeather(lat: Double? = null, lon: Double? = null) {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null, lat = lat ?: it.lat, lon = lon ?: it.lon) }
            val currentLat = lat ?: _state.value.lat
            val currentLon = lon ?: _state.value.lon

            try {
                val response = RetrofitClient.externalApi.getWeather(currentLat, currentLon)
                _state.update {
                    it.copy(
                        loading = false,
                        weather = response.currentWeather,
                        forecast = response.daily
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = "Error: ${e.message}") }
            }
        }
    }

    // Carga el clima actual para TODOS los marcadores de la lista (para el BottomSheet)
    fun loadWeatherForMarkers() {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingMarkersWeather = true) }
            try {
                // Llamadas en paralelo para mayor velocidad
                val markersWithWeather = _state.value.markers.map { marker ->
                    async {
                        try {
                            val response = RetrofitClient.externalApi.getWeather(
                                lat = marker.location.latitude,
                                lon = marker.location.longitude,
                                daily = "" // Solo clima actual para la lista rápida
                            )
                            marker.copy(cachedWeather = response.currentWeather)
                        } catch (e: Exception) {
                            marker
                        }
                    }
                }.awaitAll()

                _state.update { it.copy(markers = markersWithWeather, isLoadingMarkersWeather = false) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoadingMarkersWeather = false) }
            }
        }
    }

    // Carga el pronóstico detallado para un marcador específico
    fun loadForecastForMarker(marker: MapMarker) {
        viewModelScope.launch {
            _state.update { it.copy(selectedMarkerForecast = null, loading = true) }
            try {
                val response = RetrofitClient.externalApi.getWeather(
                    lat = marker.location.latitude,
                    lon = marker.location.longitude
                )
                _state.update { it.copy(selectedMarkerForecast = response.daily, loading = false) }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false) }
            }
        }
    }

    fun clearSelectedForecast() {
        _state.update { it.copy(selectedMarkerForecast = null) }
    }

    // --- CRUD MARCADORES (PERSISTENTE) ---

    fun addMarker(geoPoint: GeoPoint) {
        viewModelScope.launch {
            val newMarker = MapMarker(
                location = geoPoint,
                title = "Punto ${_state.value.markers.size + 1}"
            )
            markerRepository.addMarker(newMarker)
        }
    }

    fun removeMarker(marker: MapMarker) {
        viewModelScope.launch {
            markerRepository.deleteMarker(marker)
        }
    }

    fun updateMarker(original: MapMarker, newTitle: String, newType: MarkerType) {
        viewModelScope.launch {
            val updated = original.copy(title = newTitle, type = newType)
            markerRepository.updateMarker(updated)
        }
    }
}