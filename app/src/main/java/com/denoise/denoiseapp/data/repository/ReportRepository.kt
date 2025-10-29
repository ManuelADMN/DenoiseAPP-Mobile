package com.denoise.denoiseapp.data.repository

import com.denoise.denoiseapp.domain.model.Reporte
import kotlinx.coroutines.flow.Flow

interface ReportRepository {
    fun observarLista(): Flow<List<Reporte>>
    fun obtenerPorId(id: String): Flow<Reporte?>
    suspend fun guardar(reporte: Reporte)
    suspend fun eliminar(id: String)
}
