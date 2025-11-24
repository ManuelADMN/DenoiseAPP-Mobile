package com.denoise.denoiseapp.ui.report.detail

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.denoise.denoiseapp.presentation.report.DetailViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack // Icono correcto para evitar warnings
import androidx.compose.material.icons.filled.Edit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDetailScreen(
    vm: DetailViewModel,
    onEdit: () -> Unit,
    onBack: () -> Unit
) {
    val state by vm.state.collectAsState()
    val haptics = LocalHapticFeedback.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle") },
                navigationIcon = {
                    // Usamos AutoMirrored para soportar idiomas RTL (Right-To-Left) correctamente
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar")
                    }
                }
            )
        }
    ) { pad ->
        when {
            state.loading -> {
                Box(Modifier.padding(pad).fillMaxSize()) {
                    CircularProgressIndicator()
                }
            }
            state.reporte == null -> {
                Box(Modifier.padding(pad).fillMaxSize()) {
                    Text("No encontrado")
                }
            }
            else -> {
                val r = state.reporte
                Column(
                    Modifier
                        .padding(pad)
                        .padding(16.dp)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(r!!.titulo, style = MaterialTheme.typography.headlineSmall)
                    Text("Planta: ${r.planta.nombre}")

                    r.lote?.let { Text("Lote: $it") }
                    r.linea?.let { Text("Línea: $it") }

                    AnimatedContent(targetState = r.estado, label = "estadoAnim") { est ->
                        AssistChip(onClick = {}, label = { Text("Estado: $est") })
                    }

                    r.notas?.let { Text("Notas: $it") }

                    Spacer(Modifier.height(16.dp))

                    // Botones de acción
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { onEdit() }
                        ) {
                            Text("Editar")
                        }

                        OutlinedButton(
                            onClick = {
                                vm.eliminar(r.id) {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onBack()
                                }
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Eliminar")
                        }
                    }
                }
            }
        }
    }
}