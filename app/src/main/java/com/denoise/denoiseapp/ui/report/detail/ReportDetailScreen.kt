package com.denoise.denoiseapp.ui.report.detail

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.denoise.denoiseapp.domain.model.ReporteEstado
import com.denoise.denoiseapp.presentation.report.DetailViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ArrowDropDown // Icono para el menú desplegable
import androidx.compose.material.icons.filled.Image
import com.denoise.denoiseapp.ui.components.MinimalSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDetailScreen(
    vm: DetailViewModel,
    onEdit: () -> Unit,
    onBack: () -> Unit,
    isAdmin: Boolean = false // Parámetro para verificar si es Admin
) {
    val state by vm.state.collectAsState()
    val haptics = LocalHapticFeedback.current

    // Estado para controlar la visibilidad del menú desplegable de estados (Solo Admin)
    var expandedStatusMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del Reporte") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    // El botón editar SOLO es visible si es Admin (o según la lógica que prefieras)
                    if (isAdmin) {
                        IconButton(onClick = onEdit) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar")
                        }
                    }
                }
            )
        }
    ) { pad ->
        when {
            state.loading -> {
                Box(Modifier.padding(pad).fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            state.reporte == null -> {
                Box(Modifier.padding(pad).fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Reporte no encontrado", style = MaterialTheme.typography.titleMedium)
                }
            }
            else -> {
                val r = state.reporte!!
                Column(
                    Modifier
                        .padding(pad)
                        .padding(16.dp)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Encabezado: Título y Planta
                    Column {
                        Text(r.titulo, style = MaterialTheme.typography.headlineMedium)
                        Spacer(Modifier.height(4.dp))
                        Text("Planta: ${r.planta.nombre}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    HorizontalDivider()

                    // Fila de Datos Clave (Lote, Línea, Estado)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Lote", style = MaterialTheme.typography.labelMedium)
                            Text(r.lote ?: "---", style = MaterialTheme.typography.bodyMedium)
                        }
                        Column {
                            Text("Línea", style = MaterialTheme.typography.labelMedium)
                            Text(r.linea ?: "---", style = MaterialTheme.typography.bodyMedium)
                        }

                        // --- SECCIÓN DE ESTADO (INTERACTIVA SOLO PARA ADMIN) ---
                        Column {
                            Text("Estado", style = MaterialTheme.typography.labelMedium)
                            Box {
                                AssistChip(
                                    onClick = {
                                        // Solo si es admin se abre el menú para cambiar estado
                                        if (isAdmin) expandedStatusMenu = true
                                    },
                                    label = { Text(r.estado.name) },
                                    // Solo mostramos la flecha si es admin para indicar interactividad
                                    trailingIcon = if (isAdmin) {
                                        { Icon(Icons.Default.ArrowDropDown, null, Modifier.size(16.dp)) }
                                    } else null,
                                    modifier = Modifier.height(28.dp)
                                )

                                // Menú desplegable con los estados posibles (Solo Admin)
                                if (isAdmin) {
                                    DropdownMenu(
                                        expanded = expandedStatusMenu,
                                        onDismissRequest = { expandedStatusMenu = false }
                                    ) {
                                        ReporteEstado.values().forEach { nuevoEstado ->
                                            DropdownMenuItem(
                                                text = { Text(nuevoEstado.name) },
                                                onClick = {
                                                    // Llamamos a la función del ViewModel para actualizar el estado
                                                    vm.actualizarEstado(r.id, nuevoEstado)
                                                    expandedStatusMenu = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Notas Adicionales
                    if (!r.notas.isNullOrBlank()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(Modifier.padding(12.dp).fillMaxWidth()) {
                                Text("Notas:", style = MaterialTheme.typography.labelSmall)
                                Text(r.notas, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }

                    // --- SECCIÓN DE EVIDENCIAS FOTOGRÁFICAS ---
                    Text("Evidencias (${r.evidencias.size})", style = MaterialTheme.typography.titleMedium)

                    if (r.evidencias.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth().height(140.dp)
                        ) {
                            items(r.evidencias) { evidencia ->
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                    modifier = Modifier.size(140.dp)
                                ) {
                                    Box(Modifier.fillMaxSize()) {
                                        AsyncImage(
                                            model = evidencia.uriLocal,
                                            contentDescription = "Evidencia Fotográfica",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )

                                        // Placeholder si la URI está vacía o falla la carga
                                        if (evidencia.uriLocal.isNullOrBlank()) {
                                            Icon(
                                                imageVector = Icons.Default.Image,
                                                contentDescription = null,
                                                modifier = Modifier.align(Alignment.Center),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        Text(
                            "No hay evidencias adjuntas.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }

                    Spacer(Modifier.weight(1f)) // Empuja los botones hacia abajo

                    // --- BOTONES DE ACCIÓN (SOLO ADMIN) ---
                    if (isAdmin) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    vm.eliminar(r.id) {
                                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onBack()
                                    }
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Eliminar")
                            }

                            Button(
                                onClick = { onEdit() },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Editar")
                            }
                        }
                    }
                }
            }
        }
    }
}