package com.denoise.denoiseapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.denoise.denoiseapp.domain.model.Rol
import com.denoise.denoiseapp.domain.model.Usuario

@Entity(tableName = "usuarios")
data class UsuarioEntity(
    @PrimaryKey val id: String,
    val email: String,
    val nombre: String,
    val passwordHash: String,
    val rolName: String // Guardamos el nombre del Enum (ADMIN, USUARIO)
)

// Mappers
fun UsuarioEntity.toDomain(): Usuario {
    return Usuario(
        id = this.id,
        email = this.email,
        nombre = this.nombre,
        passwordHash = this.passwordHash,
        rol = try { Rol.valueOf(this.rolName) } catch (e: Exception) { Rol.USUARIO }
    )
}

fun Usuario.toEntity(): UsuarioEntity {
    return UsuarioEntity(
        id = this.id,
        email = this.email,
        nombre = this.nombre,
        passwordHash = this.passwordHash,
        rolName = this.rol.name
    )
}