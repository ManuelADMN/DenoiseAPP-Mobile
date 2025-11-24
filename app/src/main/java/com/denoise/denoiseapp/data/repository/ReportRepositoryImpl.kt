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

class ReportRepositoryImpl(
    appContext: Context
) : ReportRepository {

    private val dao = AppDatabase.getInstance(appContext).reportDao()
    private val api = RetrofitClient.denoiseApi // Tu Microservicio

    // Sincronización básica al iniciar: Trae de la API y guarda en Room
    init {
        CoroutineScope(Dispatchers.IO).launch {
            syncReports()
        }
    }

    private suspend fun syncReports() {
        try {
            val remotos = api.getAllReports()
            remotos.forEach { dao.upsert(it) }
            Log.d("Repo", "Sincronización exitosa: ${remotos.size} items")
        } catch (e: Exception) {
            Log.e("Repo", "Error al sincronizar con microservicio: ${e.message}")
            // Si falla, seguimos operando con lo que hay en Room (Offline support)
        }
    }

    override fun observeAll(): Flow<List<Reporte>> =
        dao.listAllOrderByFecha().map { list ->
            list.mapNotNull { e -> runCatching { e.toDomain() }.getOrNull() }
        }

    override fun observeById(id: String): Flow<Reporte?> =
        dao.getById(id).map { e -> runCatching { e?.toDomain() }.getOrNull() }

    override suspend fun upsert(reporte: Reporte) {
        // 1. Guardar Local (Optimistic UI)
        dao.upsert(reporte.toEntity())

        // 2. Enviar a Microservicio
        try {
            // Lógica simple: si existe ID asumimos update, si es nuevo create.
            // Para simplificar defensa, usamos POST/PUT ciego o un endpoint 'save'
            // Aquí intentamos crear:
            api.createReport(reporte.toEntity())
            Log.d("Repo", "Enviado a backend correctamente")
        } catch (e: Exception) {
            Log.e("Repo", "Fallo subida a backend (se enviará luego): ${e.message}")
            // Aquí deberías marcar un flag "dirty" para reintentar luego
        }
    }

    override suspend fun deleteById(id: String) {
        dao.deleteById(id)
        try {
            api.deleteReport(id)
        } catch (e: Exception) {
            Log.e("Repo", "Fallo borrado en backend")
        }
    }
}