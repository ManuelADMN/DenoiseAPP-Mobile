package com.denoise.denoiseapp.data.remote.api

import com.denoise.denoiseapp.data.local.entity.ReportEntity
import retrofit2.Response
import retrofit2.http.*

/**
 * Interfaz para consumir tus Microservicios Spring Boot.
 * Define las operaciones CRUD que la app puede realizar contra el servidor.
 * Cumple con el requisito de integración de microservicios propios.
 */
interface DenoiseApiService {

    // Obtener todos los reportes
    @GET("/api/v1/reports")
    suspend fun getAllReports(): List<ReportEntity>

    // Crear un nuevo reporte
    @POST("/api/v1/reports")
    suspend fun createReport(@Body report: ReportEntity): ReportEntity

    // Actualizar un reporte existente
    @PUT("/api/v1/reports/{id}")
    suspend fun updateReport(@Path("id") id: String, @Body report: ReportEntity): ReportEntity

    // Eliminar un reporte
    // Se agrega el parámetro 'user' como query param para saber quién ejecutó la acción en el backend
    // Ejemplo de llamada: DELETE /api/v1/reports/123?user=admin@denoise.com
    @DELETE("/api/v1/reports/{id}")
    suspend fun deleteReport(
        @Path("id") id: String,
        @Query("user") user: String
    ): Response<Unit>
}