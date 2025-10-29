package com.denoise.denoiseapp.presentation.report

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.denoise.denoiseapp.core.di.ServiceLocator
import com.denoise.denoiseapp.domain.model.Reporte
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class DetailUiState(
    val loading: Boolean = true,
    val reporte: Reporte? = null,
    val error: String? = null
)

class DetailViewModel(app: Application): AndroidViewModel(app) {

    private val getById = ServiceLocator.provideGetReportById(app)
    private val deleteReport = ServiceLocator.provideDeleteReport(app)

    private val _state = MutableStateFlow(DetailUiState())
    val state: StateFlow<DetailUiState> = _state

    fun cargar(id: String) {
        viewModelScope.launch {
            getById(id).collect { rep ->
                _state.value = DetailUiState(loading = false, reporte = rep)
            }
        }
    }

    fun eliminar(id: String, onDone: () -> Unit) {
        viewModelScope.launch {
            deleteReport(id)
            onDone()
        }
    }
}
