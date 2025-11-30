package com.denoise.denoiseapp.presentation.report

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.denoise.denoiseapp.core.di.ServiceLocator
import com.denoise.denoiseapp.core.util.SessionManager
import com.denoise.denoiseapp.data.repository.ReportRepositoryImpl
import com.denoise.denoiseapp.domain.model.Reporte
import com.denoise.denoiseapp.domain.model.ReporteEstado
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Estado de la UI para la pantalla de detalle
// Asegúrate de que NO tengas otra clase DetailUiState en otro archivo del mismo paquete
data class DetailUiState(
    val loading: Boolean = true,
    val reporte: Reporte? = null,
    val error: String? = null
)

class DetailViewModel(app: Application): AndroidViewModel(app) {

    private val getById = ServiceLocator.provideGetReportById(app)
    private val createOrUpdate = ServiceLocator.provideCreateOrUpdate(app)

    // Accedemos al repositorio concretamente para usar deleteWithUser.
    // Esto nos permite pasar el nombre del usuario que realiza la eliminación.
    // Asegúrate de que ServiceLocator devuelva la implementación correcta o ajusta según tu inyección.
    private val repository = ServiceLocator.provideReportRepository(app) as ReportRepositoryImpl
    private val sessionManager = SessionManager(app)

    private val _state = MutableStateFlow(DetailUiState())
    val state: StateFlow<DetailUiState> = _state

    // Cargar reporte por ID
    fun cargar(id: String) {
        viewModelScope.launch {
            getById(id).collect { rep ->
                _state.value = DetailUiState(loading = false, reporte = rep)
            }
        }
    }

    // Eliminar reporte (registrando quién lo borró)
    fun eliminar(id: String, onDone: () -> Unit) {
        viewModelScope.launch {
            // Obtenemos el nombre del usuario actual de la sesión. Si no hay usuario, usamos "Anonimo".
            val currentUser = sessionManager.getUser()?.nombre ?: "Anonimo"
            repository.deleteWithUser(id, currentUser)
            onDone()
        }
    }

    // Actualizar estado del reporte (Solo Admin usa esto)
    fun actualizarEstado(id: String, nuevoEstado: ReporteEstado) {
        val reporteActual = state.value.reporte ?: return
        viewModelScope.launch {
            // Creamos una copia del reporte con el nuevo estado y lo guardamos.
            val reporteActualizado = reporteActual.conEstado(nuevoEstado)
            createOrUpdate(reporteActualizado)
        }
    }
}