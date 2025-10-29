@file:OptIn(ExperimentalMaterial3Api::class)

package com.denoise.denoiseapp.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class DashboardUiState(
    val totalReportes: Int = 1,
    val porcentajeInfectados: Int = 0, // 0..100
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
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Denoise — Dashboard") },
                actions = { TextButton(onClick = onOpenSettings) { Text("Ajustes") } }
            )
        },
        bottomBar = {
            NavigationBar {
                // Izquierda: Reportes
                NavigationBarItem(
                    selected = false,
                    onClick = onIrALista,
                    icon = { Text("📋") },
                    label = { Text("Reportes") }
                )
                // Centro: Dashboard (estamos aquí)
                NavigationBarItem(
                    selected = true,
                    onClick = onOpenDashboard,
                    icon = { Text("📊") },
                    label = { Text("Dashboard") }
                )
                // Derecha: Ajustes
                NavigationBarItem(
                    selected = false,
                    onClick = onOpenSettings,
                    icon = { Text("⚙️") },
                    label = { Text("Ajustes") }
                )
            }
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Resumen operacional", style = MaterialTheme.typography.titleMedium)

            // Fila 1: total reportes + % infectados
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                KpiCard(
                    title = "Reportes",
                    value = state.totalReportes.toString(),
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    title = "% Infectados",
                    value = "${state.porcentajeInfectados}%",
                    modifier = Modifier.weight(1f)
                )
            }

            // Fila 2: melanosis / cracking / gaping
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                KpiCard(
                    title = "Melanosis",
                    value = state.melanosis.toString(),
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    title = "Cracking",
                    value = state.cracking.toString(),
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    title = "Gaping",
                    value = state.gaping.toString(),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(16.dp))

            Button(onClick = onIrALista) {
                Text("Ver Órdenes / Reportes")
            }
        }
    }
}

@Composable
private fun KpiCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    ElevatedCard(modifier = modifier) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                value,
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }
}
