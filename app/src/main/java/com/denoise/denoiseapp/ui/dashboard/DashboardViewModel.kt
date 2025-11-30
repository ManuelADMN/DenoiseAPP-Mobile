package com.denoise.denoiseapp.presentation.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.denoise.denoiseapp.core.di.ServiceLocator
import com.denoise.denoiseapp.domain.model.Reporte
import com.denoise.denoiseapp.domain.model.ReporteEstado
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DashboardUiState(
    val loading: Boolean = true,
    val items: List<Reporte> = emptyList(),
    val total: Int = 0,
    val porEstado: Map<ReporteEstado, Int> = emptyMap(),
    val recientes: List<Reporte> = emptyList(),
    val error: String? = null
)

class DashboardViewModel(app: Application) : AndroidViewModel(app) {

    private val getReports = ServiceLocator.provideGetReports(app)

    private val _state = MutableStateFlow(DashboardUiState())
    val state: StateFlow<DashboardUiState> = _state.asStateFlow()

    init {
        cargarReportes()
    }

    private fun cargarReportes() {
        viewModelScope.launch {
            getReports()
                .onStart { _state.update { it.copy(loading = true, error = null) } }
                .catch { e -> _state.update { it.copy(loading = false, error = e.message) } }
                .collect { list ->
                    val total = list.size
                    val porEstado = list.groupingBy { it.estado }.eachCount()

                    // CORRECCIÓN: Eliminado '?: 0L' porque fechaCreacionMillis ya es Long (no nulo)
                    val recientes = list.sortedByDescending { it.fechaCreacionMillis }.take(5)

                    _state.update {
                        it.copy(
                            loading = false,
                            items = list,
                            total = total,
                            porEstado = porEstado,
                            recientes = recientes,
                            error = null
                        )
                    }
                }
        }
    }
}