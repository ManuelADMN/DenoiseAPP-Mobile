package com.denoise.denoiseapp.domain.usecase

import com.denoise.denoiseapp.data.repository.ReportRepository
import com.denoise.denoiseapp.domain.model.Reporte
import com.denoise.denoiseapp.domain.model.ReporteEstado
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Filtra reportes por texto (titulo/planta/lote/línea), estado y rango de fechas (creación).
 */
class SearchReports(private val repo: ReportRepository) {

    operator fun invoke(
        query: String? = null,
        estado: ReporteEstado? = null,
        desdeMillis: Long? = null,
        hastaMillis: Long? = null
    ): Flow<List<Reporte>> {
        val q = query?.trim().orEmpty().lowercase()
        return repo.observarLista().map { list ->
            list.filter { rep ->
                val matchTexto = if (q.isEmpty()) true else {
                    val texto = buildString {
                        append(rep.titulo)
                        append(' ')
                        append(rep.planta.nombre)
                        rep.lote?.let { append(' '); append(it) }
                        rep.linea?.let { append(' '); append(it) }
                    }.lowercase()
                    texto.contains(q)
                }
                val matchEstado = estado?.let { rep.estado == it } ?: true
                val matchFecha = run {
                    val f = rep.fechaCreacionMillis
                    val dOk = desdeMillis?.let { f >= it } ?: true
                    val hOk = hastaMillis?.let { f <= it } ?: true
                    dOk && hOk
                }
                matchTexto && matchEstado && matchFecha
            }
        }
    }
}
