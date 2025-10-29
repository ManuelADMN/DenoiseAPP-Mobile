package com.denoise.denoiseapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reports")
data class ReportEntity(
    @PrimaryKey val id: String,
    val titulo: String,
    val plantaId: String,
    val plantaNombre: String,
    val linea: String?,
    val lote: String?,
    val estado: String,                 // ReporteEstado como texto
    val fechaCreacionMillis: Long,
    val fechaObjetivoMillis: Long?,
    val notas: String?,
    val evidenciasCount: Int,           // solo contador para UI
    val creadoPor: String?,
    val asignadoA: String?,
    val ultimaActualizacionMillis: Long
)
