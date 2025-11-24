package com.denoise.denoiseapp.ui.weather

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.denoise.denoiseapp.presentation.weather.WeatherViewModel
import com.denoise.denoiseapp.ui.components.MinimalTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen() {
    val vm: WeatherViewModel = viewModel()
    val state by vm.state.collectAsState()

    Scaffold(
        topBar = {
            MinimalTopBar("Clima en Planta") {
                IconButton(onClick = { vm.fetchWeather() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Actualizar")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- PARTE 1: DATOS DE API (Prueba técnica de consumo) ---
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "Datos en Tiempo Real (Open-Meteo)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Spacer(Modifier.height(8.dp))

                    if (state.loading) {
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        }
                    } else if (state.error != null) {
                        Text(
                            text = "No se pudo obtener el clima.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        val w = state.weather
                        if (w != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        "${w.temperature}°C",
                                        style = MaterialTheme.typography.displayMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text("Temperatura")
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        "${w.windspeed} km/h",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text("Viento")
                                }
                            }
                        } else {
                            Text("Sin datos disponibles.")
                        }
                    }
                }
            }

            // --- PARTE 2: MAPA VISUAL (WebView de Windy) ---
            Text(
                "Mapa de Condiciones",
                style = MaterialTheme.typography.titleMedium
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // Ocupa todo el espacio restante
                    .clip(RoundedCornerShape(12.dp)),
                shadowElevation = 4.dp,
                color = Color.LightGray // Fondo mientras carga
            ) {
                // URL de Windy centrada en Puerto Montt (-41.46, -72.94)
                val windyUrl = "https://www.windy.com/-41.469/-72.942?rain,-41.970,-72.942,9"

                AndroidView(factory = { context ->
                    WebView(context).apply {
                        settings.javaScriptEnabled = true
                        webViewClient = WebViewClient() // Abre links dentro de la misma vista
                        loadUrl(windyUrl)
                    }
                })
            }
        }
    }
}