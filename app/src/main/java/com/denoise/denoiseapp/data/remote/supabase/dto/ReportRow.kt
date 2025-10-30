package com.denoise.denoiseapp.data.remote.supabase.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReportRow(
    val id: String? = null,
    @SerialName("user_id") val userId: String? = null,   // auth.uid()
    val titulo: String,
    @SerialName("planta_id") val plantaId: String,
    @SerialName("linea_id") val lineaId: String? = null,
    val lote: String? = null,
    val estado: String, // enum 'PENDIENTE'|'EN_PROCESO'|'QA'|'FINALIZADO'
    @SerialName("infectados_pct") val infectadosPct: Int? = null, // 0..100
    val melanosis: Int? = null,
    val cracking: Int? = null,
    val gaping: Int? = null,
    @SerialName("notas") val notas: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
data class ReportEvidenceRow(
    val id: Long? = null,
    @SerialName("report_id") val reportId: String,
    @SerialName("path") val path: String,       // "reports/{reportId}/{fileName}"
    @SerialName("mime_type") val mimeType: String? = null,
    @SerialName("size_bytes") val sizeBytes: Int? = null,
    @SerialName("created_at") val createdAt: String? = null
)
