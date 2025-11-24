package com.denoise.denoiseapp.data.remote.api

import com.denoise.denoiseapp.data.local.entity.ReportEntity
import retrofit2.Response
import retrofit2.http.*

/**
 * Interfaz para consumir tus Microservicios Spring Boot.
 * Cumple con: IL3.1 y Punto 3 de la defensa.
 */
interface DenoiseApiService {

    @GET("/api/v1/reports")
    suspend fun getAllReports(): List<ReportEntity>

    @POST("/api/v1/reports")
    suspend fun createReport(@Body report: ReportEntity): ReportEntity

    @PUT("/api/v1/reports/{id}")
    suspend fun updateReport(@Path("id") id: String, @Body report: ReportEntity): ReportEntity

    @DELETE("/api/v1/reports/{id}")
    suspend fun deleteReport(@Path("id") id: String): Response<Unit>
}