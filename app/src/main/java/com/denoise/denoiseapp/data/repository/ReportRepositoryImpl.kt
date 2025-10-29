package com.denoise.denoiseapp.data.repository

import android.content.Context
import com.denoise.denoiseapp.data.local.db.AppDatabase
import com.denoise.denoiseapp.data.local.entity.ReportEntity
import com.denoise.denoiseapp.domain.model.Planta
import com.denoise.denoiseapp.domain.model.Reporte
import com.denoise.denoiseapp.domain.model.ReporteEstado
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ReportRepositoryImpl(
    context: Context
) : ReportRepository {

    private val dao = AppDatabase.getInstance(context).reportDao()

    override fun observarLista(): Flow<List<Reporte>> =
        dao.listAllOrderByFecha().map { list -> list.map { it.toDomain() } }

    override fun obtenerPorId(id: String): Flow<Reporte?> =
        dao.getById(id).map { it?.toDomain() }

    override suspend fun guardar(reporte: Reporte) {
        dao.upsert(reporte.toEntity())
    }

    override suspend fun eliminar(id: String) {
        dao.deleteById(id)
    }

    // --- Mapeos ---

    private fun ReportEntity.toDomain(): Reporte =
        Reporte(
            id = id,
            titulo = titulo,
            planta = Planta(plantaId, plantaNombre),
            linea = linea,
            lote = lote,
            estado = ReporteEstado.valueOf(estado),
            fechaCreacionMillis = fechaCreacionMillis,
            fechaObjetivoMillis = fechaObjetivoMillis,
            notas = notas,
            // evidencias se manejan en memoria/UI para EP2/EP3
            evidencias = emptyList(),
            creadoPor = creadoPor,
            asignadoA = asignadoA,
            ultimaActualizacionMillis = ultimaActualizacionMillis
        )

    private fun Reporte.toEntity(): ReportEntity =
        ReportEntity(
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
}
