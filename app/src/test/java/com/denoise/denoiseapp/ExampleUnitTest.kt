package com.denoise.denoiseapp

import com.denoise.denoiseapp.domain.model.Planta
import com.denoise.denoiseapp.domain.model.Reporte
import com.denoise.denoiseapp.domain.model.ReporteEstado
import com.denoise.denoiseapp.domain.model.Salmon
import org.junit.Test
import org.junit.Assert.*

/**
 * Pruebas Unitarias para validar la lógica de negocio (IL3.2).
 * Cubre la lógica de la entidad Salmon y validaciones de Reporte.
 */
class ExampleUnitTest {

    // --- GRUPO 1: PRUEBAS DE LÓGICA DE SALMÓN (Probabilidades) ---

    @Test
    fun salmon_probabilidad_alta_retorna_infectado() {
        // Dado una probabilidad de 0.8 (80%) con umbral estandar 0.5
        val salmon = Salmon.desdeProbabilidad(0.8)

        // Entonces el estado debe ser INFECTADO y el % debe ser 80
        assertEquals("INFECTADO", salmon.estado)
        assertEquals(80.0, salmon.porcentajeInfeccion, 0.01)
    }

    @Test
    fun salmon_probabilidad_baja_retorna_sano() {
        // Dado una probabilidad de 0.2 (20%)
        val salmon = Salmon.desdeProbabilidad(0.2)

        // Entonces el estado debe ser SANO
        assertEquals("SANO", salmon.estado)
        assertEquals(20.0, salmon.porcentajeInfeccion, 0.01)
    }

    @Test
    fun salmon_limite_exacto_umbral_retorna_infectado() {
        // Si la probabilidad es IGUAL al umbral (0.5), se considera infectado
        val salmon = Salmon.desdeProbabilidad(0.5, umbral = 0.5)
        assertEquals("INFECTADO", salmon.estado)
    }

    @Test
    fun salmon_umbral_personalizado_funciona() {
        // Si subimos el umbral a 0.9, un 0.8 debería ser SANO
        val salmon = Salmon.desdeProbabilidad(0.8, umbral = 0.9)
        assertEquals("SANO", salmon.estado)
    }

    @Test
    fun salmon_cero_absoluto_es_sano() {
        val salmon = Salmon.desdeProbabilidad(0.0)
        assertEquals("SANO", salmon.estado)
        assertEquals(0.0, salmon.porcentajeInfeccion, 0.01)
    }

    @Test
    fun salmon_uno_absoluto_es_infectado() {
        val salmon = Salmon.desdeProbabilidad(1.0)
        assertEquals("INFECTADO", salmon.estado)
        assertEquals(100.0, salmon.porcentajeInfeccion, 0.01)
    }

    // --- GRUPO 2: PRUEBAS DE VALIDACIÓN (Excepciones) ---

    @Test(expected = IllegalArgumentException::class)
    fun salmon_error_si_probabilidad_mayor_a_uno() {
        // No puede haber probabilidad 1.5 (150%)
        Salmon.desdeProbabilidad(1.5)
    }

    @Test(expected = IllegalArgumentException::class)
    fun salmon_error_si_probabilidad_negativa() {
        // No puede haber probabilidad negativa
        Salmon.desdeProbabilidad(-0.1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun salmon_error_si_umbral_invalido() {
        // El umbral no puede ser mayor a 1.0
        Salmon.desdeProbabilidad(0.5, umbral = 1.1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun salmon_constructor_valida_nombre_estado() {
        // Si intento crear un Salmón con estado "ENFERMO" (no permitido, solo SANO/INFECTADO)
        Salmon(porcentajeInfeccion = 50.0, estado = "ENFERMO")
    }

    @Test(expected = IllegalArgumentException::class)
    fun salmon_constructor_valida_porcentaje_negativo() {
        Salmon(porcentajeInfeccion = -5.0, estado = "SANO")
    }

    // --- GRUPO 3: PRUEBAS DE ENTIDAD REPORTE ---

    @Test
    fun reporte_creacion_exitosa_valores_correctos() {
        val reporte = Reporte(
            titulo = "Test Report",
            planta = Planta("P1", "Planta Test"),
            porcentajeInfectados = 10
        )
        assertNotNull(reporte.id) // ID se genera auto
        assertEquals("Test Report", reporte.titulo)
        assertEquals(10, reporte.porcentajeInfectados)
        assertEquals(ReporteEstado.PENDIENTE, reporte.estado) // Default
    }

    @Test(expected = IllegalArgumentException::class)
    fun reporte_error_si_titulo_vacio() {
        // El titulo no puede estar vacio
        Reporte(
            titulo = "",
            planta = Planta("P1", "Planta Test")
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun reporte_error_si_porcentaje_invalido() {
        // Porcentajes deben ser 0-100
        Reporte(
            titulo = "Test",
            planta = Planta("P1", "Planta Test"),
            melanosis = 150 // Error
        )
    }

    @Test
    fun reporte_cambio_estado_actualiza_timestamp() {
        val reporte = Reporte(titulo = "T1", planta = Planta("1", "P1"))
        val tiempoInicial = reporte.ultimaActualizacionMillis

        // Simulamos una pequeña pausa para que el reloj avance (opcional en tests reales, util aqui)
        Thread.sleep(10)

        val reporteActualizado = reporte.conEstado(ReporteEstado.FINALIZADO)

        assertEquals(ReporteEstado.FINALIZADO, reporteActualizado.estado)
        assertTrue(reporteActualizado.ultimaActualizacionMillis > tiempoInicial)
    }
}