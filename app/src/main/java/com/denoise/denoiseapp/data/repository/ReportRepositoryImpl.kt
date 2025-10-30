package com.denoise.denoiseapp.data.repository

import android.content.Context
import com.denoise.denoiseapp.data.local.db.AppDatabase
import com.denoise.denoiseapp.domain.model.Reporte
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ReportRepositoryImpl(
    appContext: Context
) : ReportRepository {

    private val dao = AppDatabase.getInstance(appContext).reportDao()

    override fun observeAll(): Flow<List<Reporte>> =
        dao.listAllOrderByFecha().map { list -> list.map { it.toDomain() } }

    override fun observeById(id: String): Flow<Reporte?> =
        dao.getById(id).map { it?.toDomain() }

    override suspend fun upsert(reporte: Reporte) {
        dao.upsert(reporte.toEntity())
    }



    override suspend fun deleteById(id: String) {
        dao.deleteById(id) // devuelve Int; no necesitamos usarlo
    }
}
