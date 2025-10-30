package com.denoise.denoiseapp.data.repository

import com.denoise.denoiseapp.domain.model.Reporte
import kotlinx.coroutines.flow.Flow

interface ReportRepository {
    fun observeAll(): Flow<List<Reporte>>
    fun observeById(id: String): Flow<Reporte?>
    suspend fun upsert(reporte: Reporte)
    suspend fun deleteById(id: String)
}
