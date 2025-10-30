package com.denoise.denoiseapp.data.repository

import com.denoise.denoiseapp.data.local.entity.ReportEntity
import com.denoise.denoiseapp.domain.model.Planta
import com.denoise.denoiseapp.domain.model.Reporte
import com.denoise.denoiseapp.domain.model.ReporteEstado

fun ReportEntity.toDomain() = Reporte(
    id = id,
    titulo = titulo,
    planta = Planta(id = plantaId, nombre = plantaNombre),
    linea = linea,
    lote = lote,
    estado = ReporteEstado.valueOf(estado),
    fechaCreacionMillis = fechaCreacionMillis,
    fechaObjetivoMillis = fechaObjetivoMillis,
    notas = notas,
    evidencias = emptyList(), // ajusta cuando tengas tabla Evidencias
    creadoPor = creadoPor,
    asignadoA = asignadoA,
    ultimaActualizacionMillis = ultimaActualizacionMillis
)

fun Reporte.toEntity() = ReportEntity(
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
    ultimaActualizacionMillis = ultimaActualizacionMillis
)
