package com.denoise.denoiseapp.domain.model

import java.util.UUID

enum class Rol {
    ADMIN,
    USUARIO
}

data class Usuario(
    val id: String = UUID.randomUUID().toString(),
    val email: String,
    val nombre: String,
    val passwordHash: String, // En la realidad esto iría hasheado
    val rol: Rol = Rol.USUARIO
)