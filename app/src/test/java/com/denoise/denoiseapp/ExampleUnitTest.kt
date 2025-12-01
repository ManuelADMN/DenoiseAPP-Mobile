package com.denoise.denoiseapp

import com.denoise.denoiseapp.domain.model.Planta
import com.denoise.denoiseapp.domain.model.Reporte
import com.denoise.denoiseapp.domain.model.ReporteEstado
import com.denoise.denoiseapp.domain.model.Salmon
import org.junit.Test
import org.junit.Assert.*
import org.junit.FixMethodOrder
import org.junit.runners.MethodSorters

/**
 * Pruebas Unitarias para validar la lógica de negocio (IL3.2).
 * Cubre la lógica de la entidad Salmon y validaciones de Reporte.
 * Los nombres de las funciones indican el valor esperado de retorno o comportamiento.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class ExampleUnitTest {

    // --- GRUPO 1: PRUEBAS DE LÓGICA DE SALMÓN (Probabilidades) ---

    @Test
    fun t01_salmon_probabilidad_0_0_retorna_estado_SANO() {
        val prob = 0.0
        val salmon = Salmon.desdeProbabilidad(prob)

        println("Prueba Cero: Probabilidad $prob -> Estado: ${salmon.estado}")

        assertEquals("SANO", salmon.estado)
        assertEquals(0.0, salmon.porcentajeInfeccion, 0.01)
    }

    @Test
    fun t02_salmon_probabilidad_0_2_retorna_estado_SANO() {
        // Dado una probabilidad de 0.2 (20%)
        val prob = 0.2
        val salmon = Salmon.desdeProbabilidad(prob)

        println("Prueba Baja: Probabilidad $prob -> Estado: ${salmon.estado}, %: ${salmon.porcentajeInfeccion}")

        // Entonces el estado debe ser SANO
        assertEquals("SANO", salmon.estado)
        assertEquals(20.0, salmon.porcentajeInfeccion, 0.01)
    }

    @Test
    fun t03_salmon_limite_exacto_0_5_retorna_estado_INFECTADO() {
        // Si la probabilidad es IGUAL al umbral (0.5), se considera infectado
        val prob = 0.5
        val salmon = Salmon.desdeProbabilidad(prob, umbral = 0.5)

        println("Prueba Límite: Probabilidad $prob (Umbral 0.5) -> Estado: ${salmon.estado}")

        assertEquals("INFECTADO", salmon.estado)
    }

    @Test
    fun t04_salmon_probabilidad_0_8_retorna_estado_INFECTADO() {
        // Dado una probabilidad de 0.8 (80%) con umbral estandar 0.5
        val prob = 0.8
        val salmon = Salmon.desdeProbabilidad(prob)

        println("Prueba Alta: Probabilidad $prob -> Estado: ${salmon.estado}, %: ${salmon.porcentajeInfeccion}")

        // Entonces el estado debe ser INFECTADO y el % debe ser 80
        assertEquals("INFECTADO", salmon.estado)
        assertEquals(80.0, salmon.porcentajeInfeccion, 0.01)
    }

    @Test
    fun t05_salmon_probabilidad_1_0_retorna_estado_INFECTADO() {
        val prob = 1.0
        val salmon = Salmon.desdeProbabilidad(prob)

        println("Prueba Uno: Probabilidad $prob -> Estado: ${salmon.estado}")

        assertEquals("INFECTADO", salmon.estado)
        assertEquals(100.0, salmon.porcentajeInfeccion, 0.01)
    }

    @Test
    fun t06_salmon_umbral_personalizado_0_9_retorna_estado_SANO_con_prob_0_8() {
        // Si subimos el umbral a 0.9, un 0.8 debería ser SANO
        val prob = 0.8
        val umbral = 0.9
        val salmon = Salmon.desdeProbabilidad(prob, umbral = umbral)

        println("Prueba Umbral Personalizado: Probabilidad $prob (Umbral $umbral) -> Estado: ${salmon.estado}")

        assertEquals("SANO", salmon.estado)
    }

    // --- GRUPO 2: PRUEBAS DE VALIDACIÓN (Excepciones) ---

    @Test(expected = IllegalArgumentException::class)
    fun t07_salmon_probabilidad_negativa_lanza_IllegalArgumentException() {
        println("Prueba Error Negativo: Intentando crear con -0.1...")
        // No puede haber probabilidad negativa
        Salmon.desdeProbabilidad(-0.1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun t08_salmon_probabilidad_mayor_a_uno_lanza_IllegalArgumentException() {
        println("Prueba Error > 1: Intentando crear con 1.5...")
        // No puede haber probabilidad 1.5 (150%)
        Salmon.desdeProbabilidad(1.5)
    }

    @Test(expected = IllegalArgumentException::class)
    fun t09_salmon_umbral_invalido_lanza_IllegalArgumentException() {
        println("Prueba Error Umbral: Intentando con umbral 1.1...")
        // El umbral no puede ser mayor a 1.0
        Salmon.desdeProbabilidad(0.5, umbral = 1.1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun t10_salmon_constructor_nombre_estado_invalido_lanza_IllegalArgumentException() {
        println("Prueba Error Estado: Intentando estado 'ENFERMO'...")
        // Si intento crear un Salmón con estado "ENFERMO" (no permitido, solo SANO/INFECTADO)
        Salmon(porcentajeInfeccion = 50.0, estado = "ENFERMO")
    }

    @Test(expected = IllegalArgumentException::class)
    fun t11_salmon_constructor_porcentaje_negativo_lanza_IllegalArgumentException() {
        println("Prueba Error % Negativo: Intentando -5.0%...")
        Salmon(porcentajeInfeccion = -5.0, estado = "SANO")
    }

    // --- GRUPO 3: PRUEBAS DE ENTIDAD REPORTE ---

    @Test
    fun t12_reporte_creacion_exitosa_retorna_objeto_valido_y_estado_PENDIENTE() {
        val reporte = Reporte(
            titulo = "Test Report",
            planta = Planta("P1", "Planta Test"),
            porcentajeInfectados = 10
        )

        println("Prueba Reporte Exitoso: Creado con ID ${reporte.id}, Título: ${reporte.titulo}, Estado: ${reporte.estado}")

        assertNotNull(reporte.id) // ID se genera auto
        assertEquals("Test Report", reporte.titulo)
        assertEquals(10, reporte.porcentajeInfectados)
        assertEquals(ReporteEstado.PENDIENTE, reporte.estado) // Default
    }

    @Test(expected = IllegalArgumentException::class)
    fun t13_reporte_titulo_vacio_lanza_IllegalArgumentException() {
        println("Prueba Error Título: Intentando crear reporte sin título...")
        // El titulo no puede estar vacio
        Reporte(
            titulo = "",
            planta = Planta("P1", "Planta Test")
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun t14_reporte_porcentaje_invalido_lanza_IllegalArgumentException() {
        println("Prueba Error Porcentaje: Intentando crear con melanosis 150...")
        // Porcentajes deben ser 0-100
        Reporte(
            titulo = "Test",
            planta = Planta("P1", "Planta Test"),
            melanosis = 150 // Error
        )
    }

    @Test
    fun t15_reporte_cambio_estado_retorna_nuevo_estado_FINALIZADO_y_timestamp_actualizado() {
        val reporte = Reporte(titulo = "T1", planta = Planta("1", "P1"))
        val tiempoInicial = reporte.ultimaActualizacionMillis

        println("Tiempo inicial: $tiempoInicial")

        // Simulamos una pequeña pausa para que el reloj avance (opcional en tests reales, util aqui)
        Thread.sleep(10)

        val reporteActualizado = reporte.conEstado(ReporteEstado.FINALIZADO)

        println("Tiempo actualizado: ${reporteActualizado.ultimaActualizacionMillis}, Nuevo estado: ${reporteActualizado.estado}")

        assertEquals(ReporteEstado.FINALIZADO, reporteActualizado.estado)
        assertTrue(reporteActualizado.ultimaActualizacionMillis > tiempoInicial)
    }
}
