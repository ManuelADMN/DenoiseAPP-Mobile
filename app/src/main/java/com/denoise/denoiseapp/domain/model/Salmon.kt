package com.denoise.denoiseapp.domain.model

data class Salmon(
    val id: String,
    val porcentajeInfeccion: Double,
    val etiqueta: String // "SANO" o "INFECTADO"
)
