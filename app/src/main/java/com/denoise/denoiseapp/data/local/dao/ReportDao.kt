package com.denoise.denoiseapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.denoise.denoiseapp.data.local.entity.ReportEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReportDao {

    // Obtiene todos los reportes ordenados por fecha de creación descendente
    @Query("SELECT * FROM reports ORDER BY fechaCreacionMillis DESC")
    fun listAllOrderByFecha(): Flow<List<ReportEntity>>

    // Obtiene un reporte específico por su ID
    @Query("SELECT * FROM reports WHERE id = :id LIMIT 1")
    fun getById(id: String): Flow<ReportEntity?>

    // --- FUNCIÓN CRÍTICA PARA EL REPOSITORIO ---
    // Verifica si existe un reporte con el ID dado. Devuelve true si count > 0
    @Query("SELECT count(*) > 0 FROM reports WHERE id = :id")
    suspend fun exists(id: String): Boolean

    // Inserta o actualiza un reporte. Si ya existe (por ID), lo reemplaza.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ReportEntity): Long

    // Elimina un reporte por su ID
    @Query("DELETE FROM reports WHERE id = :id")
    suspend fun deleteById(id: String)

    // Cuenta el total de reportes
    @Query("SELECT COUNT(*) FROM reports")
    suspend fun count(): Int
}