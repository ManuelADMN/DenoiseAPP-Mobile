package com.denoise.denoiseapp.domain.usecase

import com.denoise.denoiseapp.data.repository.ReportRepository

class GetReportById(private val repo: ReportRepository) {
    operator fun invoke(id: String) = repo.obtenerPorId(id)
}
