package com.denoise.denoiseapp.domain.usecase

import com.denoise.denoiseapp.data.repository.ReportRepository

class GetReports(private val repo: ReportRepository) {
    operator fun invoke() = repo.observeAll()
}
