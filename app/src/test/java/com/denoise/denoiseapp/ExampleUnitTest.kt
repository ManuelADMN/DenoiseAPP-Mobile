package com.denoise.denoiseapp

import com.denoise.denoiseapp.domain.model.Salmon
import org.junit.Test
import org.junit.Assert.*

/**
 * Pruebas Unitarias para validar la lógica de negocio (IL3.2).
 * Se ejecutaran en tiempo real durante la defensa.
 */
class ExampleUnitTest {

    @Test
    fun salmon_probability_logic_isCorrect() {
        // Caso 1: Probabilidad alta (0.8) con umbral 0.5 -> Debe ser INFECTADO
        val salmonInfectado = Salmon.desdeProbabilidad(0.8, umbral = 0.5)
        assertEquals("INFECTADO", salmonInfectado.estado)
        assertEquals(80.0, salmonInfectado.porcentajeInfeccion, 0.01)

        // Caso 2: Probabilidad baja (0.2) -> Debe ser SANO
        val salmonSano = Salmon.desdeProbabilidad(0.2, umbral = 0.5)
        assertEquals("SANO", salmonSano.estado)
    }

    @Test(expected = IllegalArgumentException::class)
    fun salmon_validates_input_range() {
        // Debe lanzar excepción si la probabilidad es > 1.0
        Salmon.desdeProbabilidad(1.5)
    }
}