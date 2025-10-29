package com.denoise.denoiseapp.data.repository

import com.denoise.denoiseapp.domain.model.Reporte
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * Repositorio en memoria: no persistente (dura la sesión).
 * Útil como fallback para demos rápidas.
 */
class InMemoryReportRepository : ReportRepository {

    private val state = MutableStateFlow<List<Reporte>>(emptyList())

    override fun observarLista(): Flow<List<Reporte>> = state

    override fun obtenerPorId(id: String): Flow<Reporte?> =
        state.map { list -> list.find { it.id == id } }

    override suspend fun guardar(reporte: Reporte) {
        val current = state.value.toMutableList()
        val idx = current.indexOfFirst { it.id == reporte.id }
        if (idx >= 0) current[idx] = reporte else current.add(0, reporte)
        state.value = current
    }

    override suspend fun eliminar(id: String) {
        state.value = state.value.filterNot { it.id == id }
    }
}
