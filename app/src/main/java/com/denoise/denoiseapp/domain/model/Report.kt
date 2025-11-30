package com.denoise.denoiseapp.domain.model

import java.util.UUID

enum class TipoEvidencia { FOTO, VIDEO, NOTA, OTRO }

data class Evidencia(
    val id: String = UUID.randomUUID().toString(),
    val uriLocal: String? = null,
    val uriRemota: String? = null,
    val tipo: TipoEvidencia = TipoEvidencia.FOTO,
    val timestampMillis: Long = System.currentTimeMillis(),
    val descripcion: String? = null
)

data class Reporte(
    val id: String = UUID.randomUUID().toString(),
    val titulo: String,
    val planta: Planta,
    val linea: String? = null,
    val lote: String? = null,
    val estado: ReporteEstado = ReporteEstado.PENDIENTE,

    val fechaCreacionMillis: Long = System.currentTimeMillis(),
    val fechaObjetivoMillis: Long? = null,
    val notas: String? = null,

    // Porcentajes 0..100 (TODOS)
    val porcentajeInfectados: Int = 0,
    val melanosis: Int = 0,
    val cracking: Int = 0,
    val gaping: Int = 0,

    val evidencias: List<Evidencia> = emptyList(),
    val creadoPor: String? = null,
    val asignadoA: String? = null,
    val ultimaActualizacionMillis: Long = System.currentTimeMillis()
) {
    init {
        require(titulo.isNotBlank()) { "El título no puede estar vacío." }
        require(planta.id.isNotBlank() && planta.nombre.isNotBlank()) { "La planta no puede estar vacía." }
        fun pctOk(v: Int) = v in 0..100
        require(pctOk(porcentajeInfectados) && pctOk(melanosis) && pctOk(cracking) && pctOk(gaping)) {
            "Los porcentajes deben estar entre 0 y 100."
        }
    }

    fun conEstado(nuevo: ReporteEstado): Reporte =
        copy(estado = nuevo, ultimaActualizacionMillis = System.currentTimeMillis())

    fun agregarEvidencia(e: Evidencia): Reporte =
        copy(evidencias = evidencias + e, ultimaActualizacionMillis = System.currentTimeMillis())

    fun actualizarNotas(nuevasNotas: String?): Reporte =
        copy(notas = nuevasNotas, ultimaActualizacionMillis = System.currentTimeMillis())
}
