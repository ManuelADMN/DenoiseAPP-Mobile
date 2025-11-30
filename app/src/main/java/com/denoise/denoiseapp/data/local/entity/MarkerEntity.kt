package com.denoise.denoiseapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.denoise.denoiseapp.presentation.weather.MapMarker
import com.denoise.denoiseapp.presentation.weather.MarkerType
import org.osmdroid.util.GeoPoint

@Entity(tableName = "markers")
data class MarkerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val lat: Double,
    val lon: Double,
    val title: String,
    val typeName: String // Guardamos el nombre del enum (DEFAULT, WARNING, etc.)
)

// Mapper de Entidad a Dominio
fun MarkerEntity.toMapMarker(): MapMarker {
    return MapMarker(
        id = this.id,
        location = GeoPoint(this.lat, this.lon),
        title = this.title,
        type = try {
            MarkerType.valueOf(this.typeName)
        } catch (e: Exception) {
            MarkerType.DEFAULT
        }
    )
}

// Mapper de Dominio a Entidad
fun MapMarker.toEntity(): MarkerEntity {
    return MarkerEntity(
        // Si el ID es temporal (del sistema), dejamos que Room genere uno nuevo (0)
        // o usamos el existente si estamos actualizando
        id = if (this.id > 1000000) 0 else this.id,
        lat = this.location.latitude,
        lon = this.location.longitude,
        title = this.title,
        typeName = this.type.name
    )
}