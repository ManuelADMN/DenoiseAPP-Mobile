package com.denoise.denoiseapp.data.repository

import android.content.Context
import android.util.Log
import com.denoise.denoiseapp.data.local.db.AppDatabase
import com.denoise.denoiseapp.data.remote.RetrofitClient
import com.denoise.denoiseapp.domain.model.Reporte
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

// --- Firestore ---
import com.denoise.denoiseapp.data.remote.firestore.FirestoreReportesRepository

class ReportRepositoryImpl(
    appContext: Context
) : ReportRepository {

    private val dao = AppDatabase.getInstance(appContext).reportDao()
    private val api = RetrofitClient.denoiseApi

    init {
        // Sincronización inicial en segundo plano
        CoroutineScope(Dispatchers.IO).launch {
            syncReports()
        }
    }

    private suspend fun syncReports() {
        try {
            val remotos = api.getAllReports()
            remotos.forEach { dao.upsert(it) }
        } catch (e: Exception) {
            Log.e("Repo", "Error sync: ${e.message}")
        }
    }

    override fun observeAll(): Flow<List<Reporte>> =
        dao.listAllOrderByFecha().map { list ->
            list.mapNotNull { e -> runCatching { e.toDomain() }.getOrNull() }
        }

    override fun observeById(id: String): Flow<Reporte?> =
        dao.getById(id).map { e -> runCatching { e?.toDomain() }.getOrNull() }

    override suspend fun upsert(reporte: Reporte) {
        // 1) Verificar existencia local para decidir POST/PUT
        val existe = dao.exists(reporte.id)

        // 2) Guardar LOCAL (optimistic UI)
        dao.upsert(reporte.toEntity())

        // 3) Backend REST (no bloquear si falla)
        try {
            if (existe) {
                Log.d("Repo", "El reporte ya existe. Enviando PUT (Update)...")
                api.updateReport(reporte.id, reporte.toEntity())
            } else {
                Log.d("Repo", "El reporte es nuevo. Enviando POST (Create)...")
                api.createReport(reporte.toEntity())
            }
        } catch (e: Exception) {
            Log.e("Repo", "Fallo subida a backend: ${e.message}")
        }

        // 4) Firestore (best-effort, ID = id del dominio)
        try {
            FirestoreReportesRepository.upsertFromDomain(reporte)
        } catch (e: Exception) {
            Log.e("Repo", "Fallo subida a Firestore: ${e.message}")
        }
    }

    // Implementación del nuevo método de la interfaz
    suspend fun deleteWithUser(id: String, user: String) {
        // 1. Borrado local inmediato
        dao.deleteById(id)

        // 2. Borrado remoto enviando el usuario para trazabilidad
        try {
            api.deleteReport(id, user)
            Log.d("Repo", "Eliminado del backend por $user")
        } catch (e: Exception) {
            Log.e("Repo", "Fallo borrado en backend: ${e.message}")
        }

        // 3. (Opcional) Borrar también en Firestore (best-effort)
        runCatching { FirestoreReportesRepository.deleteById(id) }
            .onFailure { Log.e("Repo", "Fallo borrado en Firestore: ${it.message}") }
    }

    // Implementación de la interfaz base (por compatibilidad)
    override suspend fun deleteById(id: String) {
        deleteWithUser(id, "Desconocido")
    }
}
