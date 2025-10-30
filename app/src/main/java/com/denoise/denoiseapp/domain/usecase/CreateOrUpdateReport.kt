package com.denoise.denoiseapp.domain.usecase

import com.denoise.denoiseapp.data.repository.ReportRepository
import com.denoise.denoiseapp.domain.model.Reporte

class CreateOrUpdateReport(private val repo: ReportRepository) {
    suspend operator fun invoke(reporte: Reporte) = repo.upsert(reporte)
}
