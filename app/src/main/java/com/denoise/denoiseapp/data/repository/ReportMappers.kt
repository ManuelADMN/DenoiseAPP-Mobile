package com.denoise.denoiseapp.data.repository

import com.denoise.denoiseapp.data.local.entity.ReportEntity
import com.denoise.denoiseapp.domain.model.Planta
import com.denoise.denoiseapp.domain.model.Reporte
import com.denoise.denoiseapp.domain.model.ReporteEstado

private fun clampPct(v: Int?): Int = (v ?: 0).coerceIn(0, 100)
private fun safeEstado(s: String?): ReporteEstado =
    runCatching { ReporteEstado.valueOf(s ?: "PENDIENTE") }
        .getOrElse { ReporteEstado.PENDIENTE }

private fun safeStr(s: String?, fallback: String) = s?.takeIf { it.isNotBlank() } ?: fallback

/* ------------- ENTITY -> DOMAIN (a prueba de datos viejos) ------------- */
fun ReportEntity.toDomain(): Reporte = Reporte(
    id = safeStr(id, java.util.UUID.randomUUID().toString()),
    titulo = safeStr(titulo, "Reporte sin título"),
    planta = Planta(
        id = safeStr(plantaId, "PL-UNK"),
        nombre = safeStr(plantaNombre, "Desconocida")
    ),
    linea = linea?.takeIf { it.isNotBlank() },
    lote  = lote?.takeIf { it.isNotBlank() },
    estado = safeEstado(estado),

    fechaCreacionMillis = fechaCreacionMillis,
    fechaObjetivoMillis = fechaObjetivoMillis,
    notas = notas?.takeIf { it.isNotBlank() },

    // % (0..100)
    porcentajeInfectados = clampPct(porcentajeInfectados),
    melanosis            = clampPct(melanosis),
    cracking             = clampPct(cracking),
    gaping               = clampPct(gaping),

    evidencias = emptyList(),
    creadoPor = creadoPor,
    asignadoA = asignadoA,
    ultimaActualizacionMillis = ultimaActualizacionMillis
)

/* ------------- DOMAIN -> ENTITY (también clampa) ------------- */
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
    porcentajeInfectados = clampPct(porcentajeInfectados),
    melanosis            = clampPct(melanosis),
    cracking             = clampPct(cracking),
    gaping               = clampPct(gaping)
)
