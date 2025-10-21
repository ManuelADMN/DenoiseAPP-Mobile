package com.denoise.denoiseapp.domain.model


import java.time.Instant
import java.util.UUID

enum class ReporteEstado { PENDIENTE, EN_PROCESO, QA, FINALIZADO, REABIERTO }

enum class EvidenciaTipo { FOTO, VIDEO, NOTA, OTRO }

data class Evidencia(
    val id: String = UUID.randomUUID().toString(),
    val uriLocal: String? = null,
    val uriRemota: String? = null,
    val tipo: EvidenciaTipo = EvidenciaTipo.FOTO,
    val timestamp: Instant = Instant.now(),
    val descripcion: String? = null
)

data class Reporte(
    val id: String = UUID.randomUUID().toString(),
    val titulo: String,
    val planta: String,
    val linea: String? = null,
    val lote: String? = null,
    val estado: ReporteEstado = ReporteEstado.PENDIENTE,
    val fechaCreacion: Instant = Instant.now(),
    val fechaObjetivo: Instant? = null,
    val notas: String? = null,
    val evidencias: List<Evidencia> = emptyList(),
    val creadoPor: String? = null,
    val asignadoA: String? = null,
    val ultimaActualizacion: Instant = Instant.now()
) {
    init {
        require(titulo.isNotBlank()) { "El título no puede estar vacío." }
        require(planta.isNotBlank()) { "La planta no puede estar vacía." }
    }

    fun conEstado(nuevo: ReporteEstado): Reporte =
        copy(estado = nuevo, ultimaActualizacion = Instant.now())

    fun agregarEvidencia(e: Evidencia): Reporte =
        copy(evidencias = evidencias + e, ultimaActualizacion = Instant.now())

    fun actualizarNotas(nuevasNotas: String?): Reporte =
        copy(notas = nuevasNotas, ultimaActualizacion = Instant.now())

    companion object {
        fun crearDemo(
            titulo: String,
            planta: String,
            linea: String? = null,
            lote: String? = null,
            fechaObjetivo: Instant? = null,
            asignadoA: String? = null,
            creadoPor: String? = null
        ): Reporte = Reporte(
            titulo = titulo,
            planta = planta,
            linea = linea,
            lote = lote,
            fechaObjetivo = fechaObjetivo,
            asignadoA = asignadoA,
            creadoPor = creadoPor
        )
    }
}
