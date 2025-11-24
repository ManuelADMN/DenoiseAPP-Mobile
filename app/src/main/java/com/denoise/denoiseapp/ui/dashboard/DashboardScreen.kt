@file:OptIn(ExperimentalMaterial3Api::class)

package com.denoise.denoiseapp.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.denoise.denoiseapp.core.util.connectivity.rememberConnectivityState
import com.denoise.denoiseapp.domain.model.Reporte
import com.denoise.denoiseapp.presentation.dashboard.DashboardViewModel
import com.denoise.denoiseapp.ui.components.ConnectivityBanner
import com.denoise.denoiseapp.ui.components.MinimalTopBar
import kotlin.math.roundToInt

@Composable
fun DashboardScreen(
    onIrALista: () -> Unit,
    onOpenSettings: () -> Unit = {},
    onOpenDashboard: () -> Unit = {}
) {
    val vm: DashboardViewModel = viewModel()
    val state by vm.state.collectAsState() // Estado real del ViewModel
    val isOnline by rememberConnectivityState()

    Scaffold(
        topBar = {
            MinimalTopBar("Denoise") {
                IconButton(onClick = onOpenSettings) {
                    Icon(Icons.Outlined.Settings, contentDescription = "Ajustes")
                }
            }
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            ConnectivityBanner(isOnline = isOnline)

            // (Sección de API Externa eliminada aquí porque se movió a WeatherScreen)

            val scroll = rememberScrollState()
            Column(
                Modifier
                    .padding(16.dp)
                    .verticalScroll(scroll)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                // Calculamos KPIs en vivo basados en el estado del VM
                val avgInfectados = calcAvg(state.items) { it.porcentajeInfectados }
                val avgMelanosis = calcAvg(state.items) { it.melanosis }
                val avgCracking = calcAvg(state.items) { it.cracking }
                val avgGaping = calcAvg(state.items) { it.gaping }

                // Fila superior
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    AccentKpiSquare(
                        title = "Reportes",
                        value = "${state.total}",
                        container = TileYellow,
                        modifier = Modifier.weight(1f),
                        height = 160.dp
                    )
                    AccentKpiSquare(
                        title = "% Infectados",
                        value = "$avgInfectados%",
                        container = TilePink,
                        modifier = Modifier.weight(1f),
                        height = 160.dp
                    )
                }

                // Tile ancho con 3 KPIs
                AccentKpiWide(
                    title = "Condiciones promedio",
                    left = "Melanosis" to "$avgMelanosis%",
                    center = "Cracking" to "$avgCracking%",
                    right = "Gaping" to "$avgGaping%",
                    container = TilePurple,
                    height = 156.dp,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

private fun calcAvg(items: List<Reporte>, selector: (Reporte) -> Int): Int {
    if (items.isEmpty()) return 0
    return items.map { selector(it).coerceIn(0, 100) }.average().roundToInt()
}

/* ---------- Tiles y Componentes visuales (Mismos de antes) ---------- */
@Composable
private fun AccentKpiSquare(
    title: String,
    value: String,
    container: Color,
    modifier: Modifier = Modifier,
    height: Dp = 160.dp
) {
    val on = Color.White
    Surface(
        modifier = modifier.height(height),
        color = container,
        contentColor = on,
        shape = RoundedCornerShape(18.dp),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Box(Modifier.fillMaxSize()) {
            Text(
                text = value,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = on,
                modifier = Modifier.align(Alignment.CenterStart).padding(start = 16.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = on.copy(alpha = 0.9f),
                modifier = Modifier.align(Alignment.TopStart).padding(16.dp)
            )
        }
    }
}

@Composable
private fun AccentKpiWide(
    title: String,
    left: Pair<String, String>,
    center: Pair<String, String>,
    right: Pair<String, String>,
    container: Color,
    height: Dp,
    modifier: Modifier = Modifier
) {
    val on = Color.White
    Surface(
        modifier = modifier.height(height),
        color = container,
        contentColor = on,
        shape = RoundedCornerShape(18.dp),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = on.copy(alpha = 0.95f))
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TripleKpi(on, left, Modifier.weight(1f))
                TripleKpi(on, center, Modifier.weight(1f))
                TripleKpi(on, right, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun TripleKpi(on: Color, pair: Pair<String, String>, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = pair.second, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold, color = on)
        Text(text = pair.first, style = MaterialTheme.typography.labelLarge, color = on.copy(alpha = 0.95f), textAlign = TextAlign.Center)
    }
}

private val TileYellow = Color(0xFFFFD54F)
private val TilePink   = Color(0xFFFF6D8D)
private val TilePurple = Color(0xFF8E8DFF)