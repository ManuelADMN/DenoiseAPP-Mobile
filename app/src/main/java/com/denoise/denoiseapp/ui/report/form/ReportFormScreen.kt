package com.denoise.denoiseapp.ui.report.form

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.denoise.denoiseapp.presentation.report.FormViewModel
import com.denoise.denoiseapp.ui.components.MinimalSection
import com.denoise.denoiseapp.ui.components.MinimalTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportFormScreen(
    vm: FormViewModel,
    onSaved: () -> Unit
) {
    val haptics = LocalHapticFeedback.current
    val state by vm.ui

    Scaffold(
        topBar = { MinimalTopBar(if (state.id == null) "Nuevo reporte" else "Editar reporte") },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    vm.guardar {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSaved()
                    }
                }
            ) { Text("Guardar") }
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = state.titulo, onValueChange = vm::onTituloChange,
                label = { Text("Título *") },
                isError = state.error != null && state.titulo.isBlank(),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.plantaNombre, onValueChange = vm::onPlantaChange,
                label = { Text("Planta *") },
                isError = state.error != null && state.plantaNombre.isBlank(),
                modifier = Modifier.fillMaxWidth()
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = state.linea, onValueChange = vm::onLineaChange, label = { Text("Línea") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = state.lote,  onValueChange = vm::onLoteChange,  label = { Text("Lote")  }, modifier = Modifier.weight(1f))
            }
            OutlinedTextField(
                value = state.notas, onValueChange = vm::onNotasChange,
                label = { Text("Notas") }, modifier = Modifier.fillMaxWidth()
            )

            MinimalSection("Métricas (0–100)")
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = state.porcentajeInfectados, onValueChange = vm::onPorcentajeChange,
                    label = { Text("% Infectados") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = state.porcentajeInfectados.toIntOrNull()?.let { it !in 0..100 } == true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = state.melanosis, onValueChange = vm::onMelanosisChange,
                    label = { Text("Melanosis") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = state.melanosis.toIntOrNull()?.let { it !in 0..100 } == true,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = state.cracking, onValueChange = vm::onCrackingChange,
                    label = { Text("Cracking") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = state.cracking.toIntOrNull()?.let { it !in 0..100 } == true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = state.gaping, onValueChange = vm::onGapingChange,
                    label = { Text("Gaping") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = state.gaping.toIntOrNull()?.let { it !in 0..100 } == true,
                    modifier = Modifier.weight(1f)
                )
            }

            // zona de evidencias (placeholder)
            MinimalSection("Evidencias")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listOf<String>()) { uri ->
                    Surface(color = MaterialTheme.colorScheme.surfaceVariant) {
                        AsyncImage(model = uri, contentDescription = null, modifier = Modifier.size(96.dp))
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Text("Campos con * son obligatorios", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
