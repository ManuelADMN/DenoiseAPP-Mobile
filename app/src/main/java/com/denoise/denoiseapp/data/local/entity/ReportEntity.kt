package com.denoise.denoiseapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.denoise.denoiseapp.domain.model.Planta
import com.denoise.denoiseapp.domain.model.Reporte
import com.denoise.denoiseapp.domain.model.ReporteEstado

@Entity(tableName = "reports")
data class ReportEntity(
    @PrimaryKey val id: String,
    val titulo: String,
    val plantaId: String,
    val plantaNombre: String,
    val linea: String?,
    val lote: String?,
    val estado: String,
    val fechaCreacionMillis: Long,
    val fechaObjetivoMillis: Long?,
    val notas: String?,
    val evidenciasCount: Int,
    val creadoPor: String?,
    val asignadoA: String?,
    val ultimaActualizacionMillis: Long,

    // Porcentajes 0..100
    val porcentajeInfectados: Int,
    val melanosis: Int,
    val cracking: Int,
    val gaping: Int
)

private fun parseEstado(s: String): ReporteEstado =
    runCatching { ReporteEstado.valueOf(s) }.getOrElse { ReporteEstado.PENDIENTE }

fun ReportEntity.toDomain(): Reporte = Reporte(
    id = id,
    titulo = titulo,
    planta = Planta(plantaId, plantaNombre),
    linea = linea,
    lote = lote,
    estado = parseEstado(estado),
    fechaCreacionMillis = fechaCreacionMillis,
    fechaObjetivoMillis = fechaObjetivoMillis,
    notas = notas,
    porcentajeInfectados = porcentajeInfectados,
    melanosis = melanosis,
    cracking = cracking,
    gaping = gaping,
    evidencias = emptyList(),
    creadoPor = creadoPor,
    asignadoA = asignadoA,
    ultimaActualizacionMillis = ultimaActualizacionMillis
)

fun Reporte.toEntity(): ReportEntity = ReportEntity(
    id = id,
    titulo = titulo,
    plantaId = planta.id,
    plantaNombre = planta.nombre,
    linea = linea,
    lote = lote,
    estado = estado.name,
    fechaCreacionMillis = fechaCreacionMillis,
    fechaObjetivoMillis = fechaObjetivoMillis,
    notas = notas,
    evidenciasCount = evidencias.size,
    creadoPor = creadoPor,
    asignadoA = asignadoA,
    ultimaActualizacionMillis = ultimaActualizacionMillis,
    porcentajeInfectados = porcentajeInfectados.coerceIn(0, 100),
    melanosis = melanosis.coerceIn(0, 100),
    cracking = cracking.coerceIn(0, 100),
    gaping = gaping.coerceIn(0, 100)
)
