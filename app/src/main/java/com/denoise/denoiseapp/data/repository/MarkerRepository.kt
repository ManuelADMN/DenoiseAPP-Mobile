package com.denoise.denoiseapp.data.repository

import android.content.Context
import com.denoise.denoiseapp.data.local.db.AppDatabase
import com.denoise.denoiseapp.data.local.entity.toEntity
import com.denoise.denoiseapp.data.local.entity.toMapMarker
import com.denoise.denoiseapp.presentation.weather.MapMarker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MarkerRepository(context: Context) {
    private val dao = AppDatabase.getInstance(context).markerDao()

    // Observa los cambios en la BD y los convierte a objetos de dominio
    fun getMarkers(): Flow<List<MapMarker>> {
        return dao.getAllMarkers().map { entities ->
            entities.map { it.toMapMarker() }
        }
    }

    suspend fun addMarker(marker: MapMarker) {
        dao.insertMarker(marker.toEntity())
    }

    suspend fun updateMarker(marker: MapMarker) {
        // Aseguramos que tenga un ID válido de BD para actualizar
        if (marker.id < 1000000) { // Asumiendo que IDs de timestamp son gigantes
            dao.updateMarker(marker.toEntity().copy(id = marker.id))
        }
    }

    suspend fun deleteMarker(marker: MapMarker) {
        dao.deleteMarker(marker.toEntity().copy(id = marker.id))
    }
}