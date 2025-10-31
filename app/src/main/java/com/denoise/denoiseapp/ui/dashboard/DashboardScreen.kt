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
    val isOnline by rememberConnectivityState()

    val live = remember(repoState.items, repoState.loading) {
        if (repoState.loading) state else mapToLegacyStateFromFields(repoState.items)
    }

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

            val scroll = rememberScrollState()
            Column(
                Modifier
                    .padding(16.dp)
                    .verticalScroll(scroll)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Fila superior
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    AccentKpiSquare(
                        title = "Reportes",
                        value = "${live.totalReportes}",
                        container = TileYellow,
                        modifier = Modifier.weight(1f),
                        height = 160.dp
                    )
                    AccentKpiSquare(
                        title = "% Infectados",
                        value = "${live.porcentajeInfectados}%",
                        container = TilePink,
                        modifier = Modifier.weight(1f),
                        height = 160.dp
                    )
                }

                // Tile ancho con 3 KPIs
                AccentKpiWide(
                    title = "Condiciones promedio",
                    left = "Melanosis" to "${live.melanosis}%",
                    center = "Cracking" to "${live.cracking}%",
                    right = "Gaping" to "${live.gaping}%",
                    container = TilePurple,
                    height = 156.dp,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

/* ---------- Tiles ---------- */

@Composable
private fun AccentKpiSquare(
    title: String,
    value: String,
    container: Color,
    modifier: Modifier = Modifier,
    height: Dp = 160.dp
) {
    val on = Color.White // texto siempre blanco

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
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 16.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = on.copy(alpha = 0.9f),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
            )
            // (Sin botón VER)
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
    val on = Color.White // texto siempre blanco

    Surface(
        modifier = modifier.height(height),
        color = container,
        contentColor = on,
        shape = RoundedCornerShape(18.dp),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = on.copy(alpha = 0.95f)
            )
            Spacer(Modifier.height(10.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TripleKpi(on, left, Modifier.weight(1f))
                TripleKpi(on, center, Modifier.weight(1f))
                TripleKpi(on, right, Modifier.weight(1f))
            }

            // (Sin dot e indicador, ni botón VER)
        }
    }
}

@Composable
private fun TripleKpi(on: Color, pair: Pair<String, String>, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = pair.second,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = on
        )
        Text(
            text = pair.first,
            style = MaterialTheme.typography.labelLarge,
            color = on.copy(alpha = 0.95f),
            textAlign = TextAlign.Center
        )
    }
}

/* ---------- Colores de tiles ---------- */
private val TileYellow = Color(0xFFFFD54F)
private val TilePink   = Color(0xFFFF6D8D)
private val TilePurple = Color(0xFF8E8DFF)

/* ---------- KPIs (clamp 0–100) ---------- */
private fun mapToLegacyStateFromFields(reportes: List<Reporte>): DashboardUiState {
    if (reportes.isEmpty()) return DashboardUiState()

    fun avg(selector: (Reporte) -> Int): Int =
        reportes.map { selector(it).coerceIn(0, 100) }
            .average()
            .roundToInt()
            .coerceIn(0, 100)

    return DashboardUiState(
        totalReportes = reportes.size,
        porcentajeInfectados = avg { it.porcentajeInfectados },
        melanosis = avg { it.melanosis },
        cracking = avg { it.cracking },
        gaping = avg { it.gaping }
    )
}
