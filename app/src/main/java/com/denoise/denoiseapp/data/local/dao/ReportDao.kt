package com.denoise.denoiseapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.denoise.denoiseapp.data.local.entity.ReportEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReportDao {

    @Query("SELECT * FROM reports ORDER BY fechaCreacionMillis DESC")
    fun listAllOrderByFecha(): Flow<List<ReportEntity>>

    @Query("SELECT * FROM reports WHERE id = :id LIMIT 1")
    fun getById(id: String): Flow<ReportEntity?>

    // -- ESTA ES LA FUNCIÓN QUE TE FALTA --
    @Query("SELECT count(*) > 0 FROM reports WHERE id = :id")
    suspend fun exists(id: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ReportEntity): Long

    @Query("DELETE FROM reports WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT COUNT(*) FROM reports")
    suspend fun count(): Int
}