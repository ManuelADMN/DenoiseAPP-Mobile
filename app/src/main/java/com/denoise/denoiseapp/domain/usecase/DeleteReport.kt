package com.denoise.denoiseapp.domain.usecase

import com.denoise.denoiseapp.data.repository.ReportRepository

class DeleteReport(private val repo: ReportRepository) {
    suspend operator fun invoke(id: String) = repo.eliminar(id)
}
