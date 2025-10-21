package com.denoise.denoiseapp.domain.model

import java.util.UUID

data class Salmon(
    val id: String = UUID.randomUUID().toString(),
    val porcentajeInfeccion: Double,
    val estado: String
) {
    init {
        require(porcentajeInfeccion in 0.0..100.0) {
            "porcentajeInfeccion debe estar entre 0 y 100."
        }
        val up = estado.uppercase()
        require(up == "SANO" || up == "INFECTADO") {
            "estado debe ser 'SANO' o 'INFECTADO'."
        }
    }

    companion object {
        /**
         * Crea un Salmon desde una probabilidad [0..1], con umbral (por defecto 0.5).
         * prob >= umbral -> "INFECTADO", si no -> "SANO".
         */
        fun desdeProbabilidad(prob: Double, umbral: Double = 0.5): Salmon {
            require(prob in 0.0..1.0) { "prob debe estar entre 0 y 1." }
            require(umbral in 0.0..1.0) { "umbral debe estar entre 0 y 1." }
            val pct = (prob * 100.0).coerceIn(0.0, 100.0)
            val estado = if (prob >= umbral) "INFECTADO" else "SANO"
            return Salmon(porcentajeInfeccion = pct, estado = estado)
        }
    }
}
