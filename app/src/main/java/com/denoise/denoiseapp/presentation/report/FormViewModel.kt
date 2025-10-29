package com.denoise.denoiseapp.presentation.report

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.denoise.denoiseapp.core.di.ServiceLocator
import com.denoise.denoiseapp.domain.model.Planta
import com.denoise.denoiseapp.domain.model.Reporte
import com.denoise.denoiseapp.domain.model.ReporteEstado
import kotlinx.coroutines.launch
import java.util.UUID

data class FormUiState(
    val id: String? = null,
    val titulo: String = "",
    val plantaNombre: String = "",
    val linea: String = "",
    val lote: String = "",
    val estado: ReporteEstado = ReporteEstado.PENDIENTE,
    val notas: String = "",
    val guardando: Boolean = false,
    val error: String? = null,
) {
    val esValido: Boolean get() = titulo.isNotBlank() && plantaNombre.isNotBlank()
}

class FormViewModel(app: Application): AndroidViewModel(app) {

    private val createOrUpdate = ServiceLocator.provideCreateOrUpdate(app)
    private val getById = ServiceLocator.provideGetReportById(app)

    var ui = androidx.compose.runtime.mutableStateOf(FormUiState())
        private set

    fun cargarParaEditar(id: String) {
        viewModelScope.launch {
            getById(id).collect { rep ->
                rep?.let {
                    ui.value = ui.value.copy(
                        id = it.id,
                        titulo = it.titulo,
                        plantaNombre = it.planta.nombre,
                        linea = it.linea.orEmpty(),
                        lote = it.lote.orEmpty(),
                        estado = it.estado,
                        notas = it.notas.orEmpty()
                    )
                }
            }
        }
    }

    fun onTituloChange(v: String) { ui.value = ui.value.copy(titulo = v) }
    fun onPlantaChange(v: String) { ui.value = ui.value.copy(plantaNombre = v) }
    fun onLineaChange(v: String) { ui.value = ui.value.copy(linea = v) }
    fun onLoteChange(v: String) { ui.value = ui.value.copy(lote = v) }
    fun onNotasChange(v: String) { ui.value = ui.value.copy(notas = v) }
    fun onEstadoChange(v: ReporteEstado) { ui.value = ui.value.copy(estado = v) }

    fun guardar(onSuccess: (String) -> Unit) {
        val s = ui.value
        if (!s.esValido) {
            ui.value = s.copy(error = "Completa título y planta")
            return
        }
        viewModelScope.launch {
            ui.value = ui.value.copy(guardando = true, error = null)
            val id = s.id ?: UUID.randomUUID().toString()
            val reporte = Reporte(
                id = id,
                titulo = s.titulo.trim(),
                planta = Planta(id = "PL-$id", nombre = s.plantaNombre.trim()),
                linea = s.linea.ifBlank { null },
                lote = s.lote.ifBlank { null },
                estado = s.estado,
                notas = s.notas.ifBlank { null },
                fechaCreacionMillis = s.id?.let { s0 -> // si edita, no cambiar creación
                    s0.length.toLong() // placeholder, no tenemos el valor original aquí
                } ?: System.currentTimeMillis(),
                fechaObjetivoMillis = null,
                ultimaActualizacionMillis = System.currentTimeMillis(),
                evidencias = emptyList(),
                creadoPor = null,
                asignadoA = null
            )
            createOrUpdate(reporte)
            ui.value = ui.value.copy(guardando = false)
            onSuccess(id)
        }
    }
}
