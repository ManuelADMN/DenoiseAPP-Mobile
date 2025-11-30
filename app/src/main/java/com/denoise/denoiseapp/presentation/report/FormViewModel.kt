package com.denoise.denoiseapp.presentation.report

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.denoise.denoiseapp.core.di.ServiceLocator
import com.denoise.denoiseapp.core.util.SessionManager // Importante para obtener el usuario
import com.denoise.denoiseapp.domain.model.Evidencia
import com.denoise.denoiseapp.domain.model.Planta
import com.denoise.denoiseapp.domain.model.Reporte
import com.denoise.denoiseapp.domain.model.ReporteEstado
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.UUID

// Eventos de UI que el ViewModel puede disparar hacia la Screen
sealed class FormUiEvent {
    object NavigateBack : FormUiEvent()
    data class ShowError(val message: String) : FormUiEvent()
}

data class FormUiState(
    val id: String? = null,
    val titulo: String = "",
    val plantaNombre: String = "",
    val linea: String = "",
    val lote: String = "",
    val estado: ReporteEstado = ReporteEstado.PENDIENTE,
    val notas: String = "",
    val fechaCreacionMillis: Long? = null,

    val porcentajeInfectados: String = "0",
    val melanosis: String = "0",
    val cracking: String = "0",
    val gaping: String = "0",

    val fotosUris: List<String> = emptyList(),

    val guardando: Boolean = false,
    val error: String? = null,
) {
    private fun pctOk(s: String): Boolean {
        if (s.isBlank()) return true
        return s.toIntOrNull()?.let { it in 0..100 } == true
    }

    val esValido: Boolean
        get() = titulo.isNotBlank() &&
                plantaNombre.isNotBlank() &&
                pctOk(porcentajeInfectados) &&
                pctOk(melanosis) &&
                pctOk(cracking) &&
                pctOk(gaping)
}

class FormViewModel(app: Application): AndroidViewModel(app) {

    private val createOrUpdate = ServiceLocator.provideCreateOrUpdate(app)
    private val getById = ServiceLocator.provideGetReportById(app)

    // Instancia del SessionManager para obtener el usuario logueado
    private val sessionManager = SessionManager(app)

    // Canal para eventos de un solo disparo (Navegación, mensajes)
    private val _uiEvent = Channel<FormUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

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
                        notas = it.notas.orEmpty(),
                        fechaCreacionMillis = it.fechaCreacionMillis,
                        porcentajeInfectados = it.porcentajeInfectados.toString(),
                        melanosis = it.melanosis.toString(),
                        cracking = it.cracking.toString(),
                        gaping = it.gaping.toString(),
                        fotosUris = it.evidencias.mapNotNull { ev -> ev.uriLocal }
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

    private fun cleanPct(v: String) = v.filter { it.isDigit() }.take(3)
    fun onPorcentajeChange(v: String) { ui.value = ui.value.copy(porcentajeInfectados = cleanPct(v)) }
    fun onMelanosisChange(v: String)  { ui.value = ui.value.copy(melanosis = cleanPct(v)) }
    fun onCrackingChange(v: String)   { ui.value = ui.value.copy(cracking = cleanPct(v)) }
    fun onGapingChange(v: String)     { ui.value = ui.value.copy(gaping = cleanPct(v)) }

    fun agregarFoto(uri: String) {
        val listaActual = ui.value.fotosUris.toMutableList()
        listaActual.add(uri)
        ui.value = ui.value.copy(fotosUris = listaActual)
    }

    fun errorMostrado() {
        ui.value = ui.value.copy(error = null)
    }

    // Función principal para guardar el reporte
    fun guardar() {
        val s = ui.value

        // Validaciones de porcentajes
        val p = s.porcentajeInfectados.toIntOrNull() ?: 0
        val m = s.melanosis.toIntOrNull() ?: 0
        val c = s.cracking.toIntOrNull() ?: 0
        val g = s.gaping.toIntOrNull() ?: 0

        if (s.titulo.isBlank() || s.plantaNombre.isBlank()) {
            ui.value = s.copy(error = "El Título y la Planta son obligatorios.")
            return
        }

        viewModelScope.launch {
            ui.value = ui.value.copy(guardando = true, error = null)
            val id = s.id ?: UUID.randomUUID().toString()

            // OBTENEMOS EL USUARIO LOGUEADO
            val currentUser = sessionManager.getUser()
            val creatorName = currentUser?.nombre ?: "Desconocido" // Si no hay usuario, 'Desconocido'

            try {
                val reporte = Reporte(
                    id = id,
                    titulo = s.titulo.trim(),
                    planta = Planta(id = "PL-$id", nombre = s.plantaNombre.trim()),
                    linea = s.linea.ifBlank { null },
                    lote = s.lote.ifBlank { null },
                    estado = s.estado,
                    notas = s.notas.ifBlank { null },
                    fechaCreacionMillis = s.fechaCreacionMillis ?: System.currentTimeMillis(),
                    fechaObjetivoMillis = null,
                    ultimaActualizacionMillis = System.currentTimeMillis(),
                    porcentajeInfectados = p.coerceIn(0, 100),
                    melanosis = m.coerceIn(0, 100),
                    cracking = c.coerceIn(0, 100),
                    gaping = g.coerceIn(0, 100),
                    evidencias = s.fotosUris.map { uri -> Evidencia(uriLocal = uri) },
                    creadoPor = creatorName, // <--- ASIGNAMOS EL NOMBRE AQUÍ
                    asignadoA = null
                )

                // Guardamos en repositorio (BD Local + API)
                createOrUpdate(reporte)

                Log.d("FormViewModel", "Guardado exitoso. Enviando evento de navegación...")

                // Enviamos evento para que la UI navegue atrás
                _uiEvent.send(FormUiEvent.NavigateBack)

            } catch (e: Exception) {
                Log.e("FormViewModel", "Error al guardar: ${e.message}")
                // Incluso si falla la API, intentamos navegar porque probablemente se guardó en local
                _uiEvent.send(FormUiEvent.NavigateBack)
            } finally {
                ui.value = ui.value.copy(guardando = false)
            }
        }
    }
}