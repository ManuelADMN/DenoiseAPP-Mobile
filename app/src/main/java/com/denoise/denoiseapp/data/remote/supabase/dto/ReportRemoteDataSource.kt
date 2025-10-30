package com.denoise.denoiseapp.data.remote.supabase

import android.content.ContentResolver
import android.net.Uri
import com.denoise.denoiseapp.core.di.SupabaseModule
import com.denoise.denoiseapp.data.remote.supabase.dto.ReportEvidenceRow
import com.denoise.denoiseapp.data.remote.supabase.dto.ReportRow
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

class ReportRemoteDataSource {

    private val sb = SupabaseModule.client

    // ------- CRUD Reports -------
    suspend fun listReports(): List<ReportRow> =
        sb.from("reports").select().decodeList<ReportRow>()

    suspend fun getReport(id: String): ReportRow? =
        sb.from("reports").select { filter { eq("id", id) } }.decodeSingleOrNull<ReportRow>()

    suspend fun upsertReport(row: ReportRow): ReportRow =
        sb.from("reports").upsert(row) { select() }.decodeSingle<ReportRow>()

    suspend fun deleteReport(id: String) {
        sb.from("reports").delete { filter { eq("id", id) } }
    }

    // ------- Evidencias (Storage + tabla report_evidences) -------
    suspend fun uploadEvidence(
        resolver: ContentResolver,
        uri: Uri,
        reportId: String,
        fileName: String
    ): ReportEvidenceRow {
        val bucket = sb.storage.from("evidences")
        val path = "reports/$reportId/$fileName"
        val bytes = resolver.openInputStream(uri)?.use { it.readBytes() } ?: byteArrayOf()

        // Sube el archivo (bucket privado; usamos signed URL para leer después)
        bucket.upload(path, bytes) { upsert = false }

        // Guarda metadatos en tabla
        val inserted = sb.from("report_evidences").insert(
            ReportEvidenceRow(
                reportId = reportId,
                path = path,
                mimeType = resolver.getType(uri),
                sizeBytes = bytes.size
            )
        ) { select() }.decodeSingle<ReportEvidenceRow>()

        return inserted
    }

    suspend fun getEvidenceSignedUrl(path: String, expiresSeconds: Int = 3600): String {
        val bucket = sb.storage.from("evidences")
        val signed = bucket.createSignedURL(path, expiresSeconds)
        return signed
    }
}
