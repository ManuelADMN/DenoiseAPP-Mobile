package com.denoise.denoiseapp.ui.report.form

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.denoise.denoiseapp.presentation.report.FormViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportFormScreen(
    vm: FormViewModel,
    onSaved: () -> Unit
) {
    val haptics = LocalHapticFeedback.current
    val state by vm.ui

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(if (state.id == null) "Nuevo reporte" else "Editar reporte") })
        },
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
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = state.titulo,
                onValueChange = vm::onTituloChange,
                label = { Text("Título *") },
                isError = state.error != null && state.titulo.isBlank(),
                supportingText = {
                    if (state.error != null && state.titulo.isBlank()) {
                        Text(state.error ?: "", color = MaterialTheme.colorScheme.error)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.plantaNombre,
                onValueChange = vm::onPlantaChange,
                label = { Text("Planta *") },
                isError = state.error != null && state.plantaNombre.isBlank(),
                supportingText = {
                    if (state.error != null && state.plantaNombre.isBlank()) {
                        Text(state.error ?: "", color = MaterialTheme.colorScheme.error)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.linea,
                onValueChange = vm::onLineaChange,
                label = { Text("Línea (opcional)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.lote,
                onValueChange = vm::onLoteChange,
                label = { Text("Lote (opcional)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.notas,
                onValueChange = vm::onNotasChange,
                label = { Text("Notas (opcional)") },
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider()

            Text("Métricas de calidad (Dashboard)", style = MaterialTheme.typography.titleMedium)

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = state.porcentajeInfectados,
                    onValueChange = vm::onPorcentajeChange,
                    label = { Text("% Infectados (0–100)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = state.porcentajeInfectados.toIntOrNull()?.let { it !in 0..100 } == true,
                    supportingText = {
                        if (state.porcentajeInfectados.toIntOrNull()?.let { it !in 0..100 } == true)
                            Text("Debe ser 0–100", color = MaterialTheme.colorScheme.error)
                    },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = state.melanosis,
                    onValueChange = vm::onMelanosisChange,
                    label = { Text("Melanosis (0–100)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = state.melanosis.toIntOrNull()?.let { it !in 0..100 } == true,
                    supportingText = {
                        if (state.melanosis.toIntOrNull()?.let { it !in 0..100 } == true)
                            Text("Debe ser 0–100", color = MaterialTheme.colorScheme.error)
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = state.cracking,
                    onValueChange = vm::onCrackingChange,
                    label = { Text("Cracking (0–100)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = state.cracking.toIntOrNull()?.let { it !in 0..100 } == true,
                    supportingText = {
                        if (state.cracking.toIntOrNull()?.let { it !in 0..100 } == true)
                            Text("Debe ser 0–100", color = MaterialTheme.colorScheme.error)
                    },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = state.gaping,
                    onValueChange = vm::onGapingChange,
                    label = { Text("Gaping (0–100)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = state.gaping.toIntOrNull()?.let { it !in 0..100 } == true,
                    supportingText = {
                        if (state.gaping.toIntOrNull()?.let { it !in 0..100 } == true)
                            Text("Debe ser 0–100", color = MaterialTheme.colorScheme.error)
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            HorizontalDivider()

            Text("Evidencias (prototipo)", style = MaterialTheme.typography.titleMedium)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listOf<String>()) { uri ->
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        AsyncImage(model = uri, contentDescription = null, modifier = Modifier.size(96.dp))
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
            Text("Campos con * son obligatorios", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
