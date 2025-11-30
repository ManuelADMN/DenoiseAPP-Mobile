package com.denoise.denoiseapp.ui.weather

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.preference.PreferenceManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.denoise.denoiseapp.presentation.weather.MapMarker
import com.denoise.denoiseapp.presentation.weather.MarkerType
import com.denoise.denoiseapp.presentation.weather.WeatherViewModel
import com.denoise.denoiseapp.ui.components.MinimalTopBar
import com.google.android.gms.location.LocationServices
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapListener // <--- IMPORTANTE: Agregado
import org.osmdroid.events.ScrollEvent // <--- IMPORTANTE: Agregado
import org.osmdroid.events.ZoomEvent   // <--- IMPORTANTE: Agregado
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    isAdmin: Boolean = false
) {
    val vm: WeatherViewModel = viewModel()
    val state by vm.state.collectAsState()
    val context = LocalContext.current

    // Estados locales
    var markerToEdit by remember { mutableStateOf<MapMarker?>(null) }
    var isAddingMarker by remember { mutableStateOf(false) }
    var currentMapCenter by remember { mutableStateOf<GeoPoint?>(null) }

    // Estado para el BottomSheet (Lista de puntos)
    var showPointsList by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    // Configuración inicial de osmdroid (requerido para cargar el mapa)
    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context))
    }

    // Cliente de ubicación de Google Play Services
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // Launcher para pedir permiso de ubicación
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            obtenerUbicacion(context, fusedLocationClient) { lat, lon ->
                vm.fetchWeather(lat, lon)
            }
        }
    }

    Scaffold(
        topBar = {
            MinimalTopBar("Mapa y Clima") {
                // Botón para centrar en mi ubicación
                IconButton(onClick = {
                    locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }) {
                    Icon(Icons.Default.MyLocation, contentDescription = "Mi Ubicación")
                }
                // Botón para refrescar datos del clima
                IconButton(onClick = { vm.fetchWeather() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Actualizar")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            // --- PARTE 1: CLIMA (API EXTERNA) ---
            WeatherInfoCard(
                state = state,
                onClick = {
                    vm.loadWeatherForMarkers() // Cargamos el clima de todos los puntos
                    showPointsList = true
                }
            )

            Spacer(Modifier.height(16.dp))

            // --- PARTE 2: MAPA INTERACTIVO ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    OsmMapView(
                        lat = state.lat,
                        lon = state.lon,
                        markers = state.markers,
                        onMarkerClick = { markerData ->
                            if (isAdmin && !isAddingMarker) {
                                markerToEdit = markerData
                            }
                        },
                        onMapCenterChange = { center ->
                            currentMapCenter = center
                        }
                    )

                    if (isAdmin) {
                        if (isAddingMarker) {
                            // Modo Agregando: Pin Central + Botones
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(48.dp).offset(y = (-24).dp)
                                )
                            }
                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 24.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                FloatingActionButton(
                                    onClick = { isAddingMarker = false },
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                ) {
                                    Icon(Icons.Default.Close, "Cancelar")
                                }
                                ExtendedFloatingActionButton(
                                    onClick = {
                                        currentMapCenter?.let { center ->
                                            vm.addMarker(center)
                                            isAddingMarker = false
                                        }
                                    },
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary,
                                    icon = { Icon(Icons.Default.Check, null) },
                                    text = { Text("Fijar Punto") }
                                )
                            }
                        } else {
                            // Modo Normal: Botón Agregar
                            FloatingActionButton(
                                onClick = { isAddingMarker = true },
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
                            ) {
                                Icon(Icons.Default.AddLocationAlt, "Agregar Punto")
                            }
                        }
                    }
                }
            }
        }

        // Diálogos y Sheets
        if (markerToEdit != null) {
            EditMarkerDialog(
                marker = markerToEdit!!,
                onDismiss = { markerToEdit = null },
                onSave = { title, type ->
                    vm.updateMarker(markerToEdit!!, title, type)
                    markerToEdit = null
                },
                onDelete = {
                    vm.removeMarker(markerToEdit!!)
                    markerToEdit = null
                }
            )
        }

        if (showPointsList) {
            ModalBottomSheet(
                onDismissRequest = {
                    showPointsList = false
                    vm.clearSelectedForecast()
                },
                sheetState = sheetState
            ) {
                if (state.selectedMarkerForecast != null) {
                    ForecastDetailView(
                        forecast = state.selectedMarkerForecast!!,
                        onBack = { vm.clearSelectedForecast() }
                    )
                } else {
                    MarkersListView(
                        markers = state.markers,
                        isLoading = state.isLoadingMarkersWeather,
                        onMarkerClick = { marker -> vm.loadForecastForMarker(marker) }
                    )
                }
            }
        }
    }
}

@Composable
fun ForecastDetailView(
    forecast: com.denoise.denoiseapp.data.remote.api.DailyForecast,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp).padding(bottom = 40.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.Close, "Cerrar detalle") }
            Text("Pronóstico 7 Días", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(forecast.time.size) { index ->
                val date = forecast.time[index]
                val max = forecast.temperatureMax.getOrNull(index) ?: 0.0
                val min = forecast.temperatureMin.getOrNull(index) ?: 0.0
                val wCode = forecast.weatherCode?.getOrNull(index) ?: 0

                // Obtenemos el icono y color según el código
                val (weatherIcon, weatherColor, label) = getWeatherAttributes(wCode)

                Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Fecha + Icono Clima
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = weatherIcon,
                                contentDescription = label,
                                tint = weatherColor,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(date, fontWeight = FontWeight.Bold)
                                Text(label, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                        }

                        // Temperaturas
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Min
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Min", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                Text("$min°", color = Color(0xFF1976D2), fontWeight = FontWeight.Medium)
                            }
                            Spacer(Modifier.width(16.dp))
                            // Max
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Max", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                Text("$max°", color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- HELPER PARA ICONOS DEL CLIMA ---
fun getWeatherAttributes(code: Int): Triple<ImageVector, Color, String> {
    return when (code) {
        0 -> Triple(Icons.Default.WbSunny, Color(0xFFFFA000), "Soleado")
        1, 2, 3 -> Triple(Icons.Default.Cloud, Color(0xFF78909C), "Nublado")
        45, 48 -> Triple(Icons.Default.Cloud, Color.Gray, "Niebla")
        51, 53, 55 -> Triple(Icons.Default.Grain, Color(0xFF42A5F5), "Llovizna")
        61, 63, 65 -> Triple(Icons.Default.Grain, Color(0xFF1E88E5), "Lluvia")
        71, 73, 75 -> Triple(Icons.Default.AcUnit, Color(0xFF29B6F6), "Nieve")
        80, 81, 82 -> Triple(Icons.Default.Grain, Color(0xFF1565C0), "Chubascos")
        95, 96, 99 -> Triple(Icons.Default.Thunderstorm, Color(0xFF5E35B1), "Tormenta")
        else -> Triple(Icons.Default.Info, Color.Gray, "Desconocido")
    }
}

@Composable
fun MarkersListView(
    markers: List<MapMarker>,
    isLoading: Boolean,
    onMarkerClick: (MapMarker) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp).padding(bottom = 40.dp)) {
        Text("Puntos Monitoreados", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        if (isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(16.dp))
        }

        if (markers.isEmpty()) {
            Text("No hay puntos marcados en el mapa.", color = Color.Gray)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(markers) { marker ->
                    Card(
                        onClick = { onMarkerClick(marker) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(getIconVector(marker.type), null, tint = getIconColor(marker.type))
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(marker.title, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        "${marker.location.latitude.toString().take(6)}, ${marker.location.longitude.toString().take(6)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }
                            }
                            if (marker.cachedWeather != null) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("${marker.cachedWeather.temperature}°C", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Text("Ver pronóstico >", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                }
                            } else if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EditMarkerDialog(
    marker: MapMarker,
    onDismiss: () -> Unit,
    onSave: (String, MarkerType) -> Unit,
    onDelete: () -> Unit
) {
    var title by remember { mutableStateOf(marker.title) }
    var selectedType by remember { mutableStateOf(marker.type) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Punto") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nombre del punto") },
                    singleLine = true
                )

                Column {
                    Text("Tipo de Ícono:", style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                        MarkerType.values().forEach { type ->
                            val isSelected = type == selectedType
                            val color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray

                            IconButton(
                                onClick = { selectedType = type },
                                modifier = Modifier
                                    .border(
                                        width = if (isSelected) 2.dp else 0.dp,
                                        color = color,
                                        shape = CircleShape
                                    )
                                    .background(color.copy(alpha = 0.1f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = getIconVector(type),
                                    contentDescription = type.name,
                                    tint = getIconColor(type)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(title, selectedType) }) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDelete, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                Text("Eliminar")
            }
        }
    )
}

@Composable
fun WeatherInfoCard(
    state: com.denoise.denoiseapp.presentation.weather.WeatherUiState,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Clima Actual (Ver Lista de Puntos)",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
            }

            if (state.loading && state.weather == null) {
                Box(Modifier.fillMaxWidth().height(60.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            } else if (state.error != null) {
                Text("Error: ${state.error}", color = MaterialTheme.colorScheme.error)
            } else {
                val w = state.weather
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "${w?.temperature ?: "--"}°C",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            "Lat: ${state.lat.toString().take(6)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "${w?.windspeed ?: "--"} km/h",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text("Viento", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

fun getIconVector(type: MarkerType): ImageVector {
    return when(type) {
        MarkerType.DEFAULT -> Icons.Default.LocationOn
        MarkerType.WARNING -> Icons.Default.Warning
        MarkerType.CHECK -> Icons.Default.CheckCircle
        MarkerType.INFO -> Icons.Default.Info
    }
}

fun getIconColor(type: MarkerType): Color {
    return when(type) {
        MarkerType.DEFAULT -> Color(0xFF1976D2)
        MarkerType.WARNING -> Color(0xFFD32F2F)
        MarkerType.CHECK -> Color(0xFF388E3C)
        MarkerType.INFO -> Color(0xFFFBC02D)
    }
}

fun getMarkerDrawable(context: Context, type: MarkerType): Drawable {
    val drawable = ContextCompat.getDrawable(context, org.osmdroid.library.R.drawable.marker_default)!!.mutate()
    drawable.setTint(getIconColor(type).toArgb())
    return drawable
}

@Composable
fun OsmMapView(
    lat: Double,
    lon: Double,
    markers: List<com.denoise.denoiseapp.presentation.weather.MapMarker>,
    onMarkerClick: (com.denoise.denoiseapp.presentation.weather.MapMarker) -> Unit,
    onMapCenterChange: (GeoPoint) -> Unit
) {
    val context = LocalContext.current

    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(13.0)
        }
    }

    DisposableEffect(mapView) {
        val listener = object : MapListener {
            override fun onScroll(event: ScrollEvent?): Boolean {
                onMapCenterChange(mapView.mapCenter as GeoPoint)
                return true
            }
            override fun onZoom(event: ZoomEvent?): Boolean {
                onMapCenterChange(mapView.mapCenter as GeoPoint)
                return true
            }
        }
        mapView.addMapListener(listener)
        onMapCenterChange(mapView.mapCenter as GeoPoint)

        onDispose { mapView.removeMapListener(listener) }
    }

    LaunchedEffect(lat, lon) {
        mapView.controller.animateTo(GeoPoint(lat, lon))
    }

    AndroidView(
        factory = { mapView },
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds(),
        update = { view ->
            val overlaysToRemove = view.overlays.filterIsInstance<Marker>()
            view.overlays.removeAll(overlaysToRemove)

            val myLocMarker = Marker(view)
            myLocMarker.position = GeoPoint(lat, lon)
            myLocMarker.title = "Ubicación Actual"
            myLocMarker.icon = ContextCompat.getDrawable(context, org.osmdroid.library.R.drawable.person)
            myLocMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            view.overlays.add(myLocMarker)

            markers.forEach { markerData ->
                val customMarker = Marker(view)
                customMarker.position = markerData.location
                customMarker.title = markerData.title
                customMarker.icon = getMarkerDrawable(context, markerData.type)

                customMarker.setOnMarkerClickListener { m, _ ->
                    m.showInfoWindow()
                    onMarkerClick(markerData)
                    true
                }
                view.overlays.add(customMarker)
            }

            view.invalidate()
        }
    )
}

@SuppressLint("MissingPermission")
private fun obtenerUbicacion(
    context: Context,
    client: com.google.android.gms.location.FusedLocationProviderClient,
    onLocationFound: (Double, Double) -> Unit
) {
    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
        client.lastLocation.addOnSuccessListener { location ->
            location?.let {
                onLocationFound(it.latitude, it.longitude)
            }
        }
    }
}