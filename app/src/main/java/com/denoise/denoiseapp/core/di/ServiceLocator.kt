package com.denoise.denoiseapp.core.di

import android.content.Context
import com.denoise.denoiseapp.data.repository.ReportRepository
import com.denoise.denoiseapp.data.repository.ReportRepositoryImpl
import com.denoise.denoiseapp.domain.usecase.CreateOrUpdateReport
import com.denoise.denoiseapp.domain.usecase.DeleteReport
import com.denoise.denoiseapp.domain.usecase.GetReportById
import com.denoise.denoiseapp.domain.usecase.GetReports

object ServiceLocator {

    fun provideReportRepository(appContext: Context): ReportRepository =
        ReportRepositoryImpl(appContext)

    fun provideGetReports(appContext: Context) =
        GetReports(provideReportRepository(appContext))

    fun provideGetReportById(appContext: Context) =
        GetReportById(provideReportRepository(appContext))

    fun provideCreateOrUpdate(appContext: Context) =
        CreateOrUpdateReport(provideReportRepository(appContext))

    fun provideDeleteReport(appContext: Context) =
        DeleteReport(provideReportRepository(appContext))
}
