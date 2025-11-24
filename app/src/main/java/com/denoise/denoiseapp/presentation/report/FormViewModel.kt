package com.denoise.denoiseapp.presentation.report

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.denoise.denoiseapp.core.di.ServiceLocator
import com.denoise.denoiseapp.domain.model.Evidencia
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
    val fechaCreacionMillis: Long? = null,

    // Métricas
    val porcentajeInfectados: String = "0",
    val melanosis: String = "0",
    val cracking: String = "0",
    val gaping: String = "0",

    // --- NUEVO: Lista de URIs de las fotos ---
    val fotosUris: List<String> = emptyList(),

    val guardando: Boolean = false,
    val error: String? = null,
) {
    private fun pctOk(s: String) = s.toIntOrNull()?.let { it in 0..100 } == true
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
                        // Cargamos fotos si existieran (Mapeo simple)
                        fotosUris = it.evidencias.mapNotNull { ev -> ev.uriLocal }
                    )
                }
            }
        }
    }

    // Setters
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

    // --- NUEVO: Función para agregar foto ---
    fun agregarFoto(uri: String) {
        val listaActual = ui.value.fotosUris.toMutableList()
        listaActual.add(uri)
        ui.value = ui.value.copy(fotosUris = listaActual)
    }

    fun guardar(onSuccess: (String) -> Unit) {
        val s = ui.value
        val p = s.porcentajeInfectados.toIntOrNull() ?: -1
        val m = s.melanosis.toIntOrNull() ?: -1
        val c = s.cracking.toIntOrNull() ?: -1
        val g = s.gaping.toIntOrNull() ?: -1

        val allOk = listOf(p, m, c, g).all { it in 0..100 }
        if (!s.esValido || !allOk) {
            ui.value = s.copy(error = "Ingresa porcentajes válidos entre 0 y 100.")
            return
        }

        viewModelScope.launch {
            ui.value = ui.value.copy(guardando = true, error = null)
            val id = s.id ?: UUID.randomUUID().toString()

            // Convertimos strings URIs a Objetos Evidencia
            val evidenciasObj = s.fotosUris.map { uri ->
                Evidencia(uriLocal = uri)
            }

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
                // Aquí asignamos las evidencias
                evidencias = evidenciasObj,
                creadoPor = null,
                asignadoA = null
            )
            createOrUpdate(reporte)
            ui.value = ui.value.copy(guardando = false)
            onSuccess(id)
        }
    }
}