@file:OptIn(ExperimentalMaterial3Api::class)

package com.denoise.denoiseapp.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.denoise.denoiseapp.core.util.connectivity.rememberConnectivityState
import com.denoise.denoiseapp.domain.model.Reporte
import com.denoise.denoiseapp.presentation.dashboard.DashboardViewModel
import com.denoise.denoiseapp.ui.components.ConnectivityBanner

data class DashboardUiState(
    val totalReportes: Int = 0,
    val porcentajeInfectados: Int = 0,
    val melanosis: Int = 0,
    val cracking: Int = 0,
    val gaping: Int = 0
)

@Composable
fun DashboardScreen(
    onIrALista: () -> Unit,
    onOpenSettings: () -> Unit = {},
    onOpenDashboard: () -> Unit = {},
    state: DashboardUiState = DashboardUiState()
) {
    val vm: DashboardViewModel = viewModel()
    val repoState by vm.state.collectAsState()

    val liveState = remember(repoState.items, repoState.loading) {
        if (repoState.loading) state else mapToLegacyStateFromFields(repoState.items)
    }

    val isOnline by rememberConnectivityState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Denoise — Dashboard") },
                actions = { TextButton(onClick = onOpenSettings) { Text("Ajustes") } }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            ConnectivityBanner(isOnline = isOnline)
            Column(Modifier.padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Resumen operacional", style = MaterialTheme.typography.titleMedium)

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    KpiCard("Reportes", liveState.totalReportes.toString(), Modifier.weight(1f))
                    KpiCard("% Infectados", "${liveState.porcentajeInfectados}%", Modifier.weight(1f))
                }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    KpiCard("Melanosis", "${liveState.melanosis}%", Modifier.weight(1f))
                    KpiCard("Cracking",  "${liveState.cracking}%",  Modifier.weight(1f))
                    KpiCard("Gaping",    "${liveState.gaping}%",    Modifier.weight(1f))
                }

                Spacer(Modifier.height(16.dp))
                Button(onClick = onIrALista) { Text("Ver Órdenes / Reportes") }
            }
        }
    }
}

@Composable
private fun KpiCard(title: String, value: String, modifier: Modifier = Modifier) {
    ElevatedCard(modifier = modifier) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.headlineSmall)
        }
    }
}

private fun mapToLegacyStateFromFields(reportes: List<Reporte>): DashboardUiState {
    if (reportes.isEmpty()) return DashboardUiState()
    val total = reportes.size

    fun avg(selector: (Reporte) -> Int): Int =
        reportes.map { selector(it).coerceIn(0, 100) }.average().toInt().coerceIn(0, 100)

    return DashboardUiState(
        totalReportes = total,
        porcentajeInfectados = avg { it.porcentajeInfectados },
        melanosis = avg { it.melanosis },
        cracking = avg { it.cracking },
        gaping = avg { it.gaping }
    )
}
