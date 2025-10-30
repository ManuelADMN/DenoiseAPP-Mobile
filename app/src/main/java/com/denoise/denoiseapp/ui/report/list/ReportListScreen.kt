package com.denoise.denoiseapp.ui.report.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList as FilterListIcon
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import com.denoise.denoiseapp.domain.model.Reporte
import com.denoise.denoiseapp.domain.model.ReporteEstado
import com.denoise.denoiseapp.presentation.report.ListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportListScreen(
    vm: ListViewModel,
    onAdd: () -> Unit,
    onOpen: (String) -> Unit,
    onEdit: (String) -> Unit,
    onOpenSettings: () -> Unit
) {
    val state by vm.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Órdenes Denoise") },
                actions = {
                    IconButton(onClick = vm::toggleFiltros) {
                        Icon(FilterListIcon, contentDescription = "Filtros")

                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Ajustes")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo reporte")
            }
        }
    ) { pad ->
        Column(
            Modifier
                .padding(pad)
                .fillMaxSize()
        ) {

            OutlinedTextField(
                value = state.query,
                onValueChange = vm::onQueryChange,
                label = { Text("Buscar por título, planta o lote") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            )

            AnimatedVisibility(visible = state.filtrosVisibles) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EstadoFilterChip("Todos", state.filtroEstado == null) {
                        vm.onEstadoChange(null)
                    }
                    EstadoFilterChip("Pendiente", state.filtroEstado == ReporteEstado.PENDIENTE) {
                        vm.onEstadoChange(ReporteEstado.PENDIENTE)
                    }
                    EstadoFilterChip("En proceso", state.filtroEstado == ReporteEstado.EN_PROCESO) {
                        vm.onEstadoChange(ReporteEstado.EN_PROCESO)
                    }
                    EstadoFilterChip("QA", state.filtroEstado == ReporteEstado.QA) {
                        vm.onEstadoChange(ReporteEstado.QA)
                    }
                    EstadoFilterChip("Finalizado", state.filtroEstado == ReporteEstado.FINALIZADO) {
                        vm.onEstadoChange(ReporteEstado.FINALIZADO)
                    }
                }
            }

            when {
                state.loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                state.items.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No hay reportes. Crea uno con el botón +")
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.items, key = { it.id }) { rep ->
                            ReportCard(rep, onOpen = onOpen, onEdit = onEdit)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EstadoFilterChip(text: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text) }
    )
}

@Composable
private fun ReportCard(rep: Reporte, onOpen: (String) -> Unit, onEdit: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { onOpen(rep.id) }
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                rep.titulo,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Planta: ${rep.planta.nombre}  •  Estado: ${rep.estado}",
                style = MaterialTheme.typography.bodyMedium
            )
            rep.lote?.let { Text("Lote: $it", style = MaterialTheme.typography.bodyMedium) }
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { onEdit(rep.id) }) { Text("Editar") }
            }
        }
    }
}
