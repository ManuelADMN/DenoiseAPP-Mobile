package com.denoise.denoiseapp.data.repository

import android.content.Context
import android.util.Log
import com.denoise.denoiseapp.data.local.db.AppDatabase
import com.denoise.denoiseapp.domain.model.Reporte
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ReportRepositoryImpl(
    appContext: Context
) : ReportRepository {

    private val dao = AppDatabase.getInstance(appContext).reportDao()

    override fun observeAll(): Flow<List<Reporte>> =
        dao.listAllOrderByFecha().map { list ->
            list.mapNotNull { e ->
                runCatching { e.toDomain() }
                    .onFailure { t ->
                        Log.e("ReportRepo", "Fila inválida (id=${e.id}): ${t.message}")
                    }
                    .getOrNull()
            }
        }

    override fun observeById(id: String): Flow<Reporte?> =
        dao.getById(id).map { e ->
            e?.let { ent ->
                runCatching { ent.toDomain() }
                    .onFailure { t ->
                        Log.e("ReportRepo", "Fila inválida (id=${ent.id}): ${t.message}")
                    }
                    .getOrNull()
            }
        }

    override suspend fun upsert(reporte: Reporte) {
        dao.upsert(reporte.toEntity())
    }

    override suspend fun deleteById(id: String) {
        dao.deleteById(id)
    }
}
