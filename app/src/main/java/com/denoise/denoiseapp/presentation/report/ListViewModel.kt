package com.denoise.denoiseapp.presentation.report

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.denoise.denoiseapp.core.di.ServiceLocator
import com.denoise.denoiseapp.domain.model.Reporte
import com.denoise.denoiseapp.domain.model.ReporteEstado
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ListUiState(
    val loading: Boolean = true,
    val source: List<Reporte> = emptyList(), // lista cruda de BD
    val items: List<Reporte> = emptyList(),  // lista filtrada para UI
    val query: String = "",
    val filtroEstado: ReporteEstado? = null,
    val error: String? = null,
    val filtrosVisibles: Boolean = false
)

class ListViewModel(app: Application): AndroidViewModel(app) {

    private val getReports = ServiceLocator.provideGetReports(app)

    private val _state = MutableStateFlow(ListUiState())
    val state: StateFlow<ListUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            getReports()
                .onStart { _state.update { it.copy(loading = true) } }
                .catch { e -> _state.update { it.copy(loading = false, error = e.message) } }
                .collect { list ->
                    _state.update { s ->
                        val filtered = aplicarFiltros(list, s.query, s.filtroEstado)
                        s.copy(loading = false, source = list, items = filtered)
                    }
                }
        }
    }

    fun onQueryChange(q: String) {
        _state.update { s ->
            val filtered = aplicarFiltros(s.source, q, s.filtroEstado)
            s.copy(query = q, items = filtered)
        }
    }

    fun onEstadoChange(estado: ReporteEstado?) {
        _state.update { s ->
            val filtered = aplicarFiltros(s.source, s.query, estado)
            s.copy(filtroEstado = estado, items = filtered)
        }
    }

    fun toggleFiltros() {
        _state.update { it.copy(filtrosVisibles = !it.filtrosVisibles) }
    }

    private fun aplicarFiltros(
        base: List<Reporte>,
        query: String,
        estado: ReporteEstado?
    ): List<Reporte> {
        return base.filter { r ->
            val q = query.trim().lowercase()
            val matchQ = if (q.isBlank()) true else
                r.titulo.lowercase().contains(q) ||
                        r.lote?.lowercase()?.contains(q) == true ||
                        r.planta.nombre.lowercase().contains(q)

            val matchEstado = estado?.let { r.estado == it } ?: true
            matchQ && matchEstado
        }
    }
}
