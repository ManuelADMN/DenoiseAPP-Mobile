package com.denoise.denoiseapp.domain.usecase

import android.content.Context
import com.denoise.denoiseapp.core.di.ServiceLocator
import com.denoise.denoiseapp.domain.model.Reporte
import com.denoise.denoiseapp.domain.model.ReporteEstado
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Búsqueda local (client-side) sobre la lista de Reportes.
 * - query: busca en título, planta, lote, línea
 * - estado: filtra por estado si no es null
 */
class SearchReports(appContext: Context) {

    private val getReports = ServiceLocator.provideGetReports(appContext)

    operator fun invoke(
        query: String,
        estado: ReporteEstado?
    ): Flow<List<Reporte>> {
        val q = query.trim().lowercase()
        return getReports().map { base ->
            base.filter { r ->
                val matchQ =
                    if (q.isBlank()) true
                    else r.titulo.lowercase().contains(q) ||
                            r.planta.nombre.lowercase().contains(q) ||
                            (r.lote?.lowercase()?.contains(q) == true) ||
                            (r.linea?.lowercase()?.contains(q) == true)

                val matchEstado = estado?.let { r.estado == it } ?: true
                matchQ && matchEstado
            }
        }
    }
}
