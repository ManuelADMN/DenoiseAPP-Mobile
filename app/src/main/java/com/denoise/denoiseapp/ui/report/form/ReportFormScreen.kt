package com.denoise.denoiseapp.ui.report.form

import android.Manifest
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.denoise.denoiseapp.presentation.report.FormUiEvent
import com.denoise.denoiseapp.presentation.report.FormViewModel
import com.denoise.denoiseapp.ui.components.MinimalSection
import com.denoise.denoiseapp.ui.components.MinimalTopBar
import kotlinx.coroutines.flow.collectLatest
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportFormScreen(
    vm: FormViewModel,
    onSaved: () -> Unit // Esta función navegará atrás al recibir el evento
) {
    val haptics = LocalHapticFeedback.current
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val state by vm.ui

    // Estado de scroll para poder ver todo el contenido
    val scrollState = rememberScrollState()

    // Estado para el Snackbar (Mensajes flotantes de error/éxito)
    val snackbarHostState = remember { SnackbarHostState() }

    // --- ESCUCHA DE EVENTOS DE NAVEGACIÓN (CRÍTICO) ---
    // Este bloque escucha el canal de eventos del ViewModel.
    // Cuando recibe NavigateBack, ejecuta onSaved() que hace el popBackStack()
    LaunchedEffect(key1 = true) {
        vm.uiEvent.collectLatest { event ->
            when (event) {
                is FormUiEvent.NavigateBack -> {
                    onSaved() // Navegación segura
                }
                is FormUiEvent.ShowError -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    // Efecto secundario para errores de estado (validaciones locales)
    LaunchedEffect(state.error) {
        state.error?.let { errorMsg ->
            snackbarHostState.showSnackbar(message = errorMsg)
            vm.errorMostrado()
        }
    }

    // Variables para la cámara
    var tempUri by remember { mutableStateOf<Uri?>(null) }

    // Launcher 1: Recibe el resultado de la cámara
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempUri != null) {
            vm.agregarFoto(tempUri.toString())
        }
    }

    // Launcher 2: Pide permisos y lanza la cámara
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val (_, uri) = crearArchivoTemporal(context)
            tempUri = uri
            cameraLauncher.launch(uri)
        }
    }

    Scaffold(
        topBar = { MinimalTopBar(if (state.id == null) "Nuevo reporte" else "Editar reporte") },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    focusManager.clearFocus() // Cierra teclado
                    if (!state.guardando) {
                        // Llamada simple al ViewModel. Él se encarga de emitir el evento de navegación.
                        vm.guardar()
                    }
                },
                containerColor = if (state.guardando) Color.LightGray else MaterialTheme.colorScheme.primaryContainer,
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                if (state.guardando) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("Guardando...")
                } else {
                    Text("Guardar Reporte")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .fillMaxSize()
                .verticalScroll(scrollState), // Habilitamos scroll vertical
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // --- CAMPOS DE TEXTO ---
            OutlinedTextField(
                value = state.titulo, onValueChange = vm::onTituloChange,
                label = { Text("Título *") },
                isError = state.error != null && state.titulo.isBlank(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = state.plantaNombre, onValueChange = vm::onPlantaChange,
                label = { Text("Planta *") },
                isError = state.error != null && state.plantaNombre.isBlank(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = state.linea, onValueChange = vm::onLineaChange,
                    label = { Text("Línea") }, modifier = Modifier.weight(1f), singleLine = true
                )
                OutlinedTextField(
                    value = state.lote,  onValueChange = vm::onLoteChange,
                    label = { Text("Lote")  }, modifier = Modifier.weight(1f), singleLine = true
                )
            }

            OutlinedTextField(
                value = state.notas, onValueChange = vm::onNotasChange,
                label = { Text("Notas Adicionales") },
                modifier = Modifier.fillMaxWidth().height(100.dp),
                maxLines = 4
            )

            // --- SECCIÓN DE MÉTRICAS ---
            MinimalSection("Métricas de Calidad (0–100)")

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricInput(value = state.porcentajeInfectados, onValueChange = vm::onPorcentajeChange, label = "% Infectados", modifier = Modifier.weight(1f))
                MetricInput(value = state.melanosis, onValueChange = vm::onMelanosisChange, label = "Melanosis", modifier = Modifier.weight(1f))
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricInput(value = state.cracking, onValueChange = vm::onCrackingChange, label = "Cracking", modifier = Modifier.weight(1f))
                MetricInput(value = state.gaping, onValueChange = vm::onGapingChange, label = "Gaping", modifier = Modifier.weight(1f))
            }

            // --- SECCIÓN DE EVIDENCIAS (MODERNA Y GRANDE) ---
            MinimalSection("Evidencias Fotográficas")

            // Botón Grande tipo Tarjeta
            Card(
                onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(12.dp),
                border = userBorder(width = 1.dp, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth().height(60.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text("Añadir Nueva Foto", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                }
            }

            // Galería de Fotos Grandes
            if (state.fotosUris.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp) // Altura generosa para el contenedor
                        .padding(vertical = 8.dp)
                ) {
                    items(state.fotosUris) { uriStr ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shadowElevation = 4.dp,
                            modifier = Modifier
                                .size(240.dp) // Fotos grandes de 240dp
                                .border(1.dp, Color.LightGray, RoundedCornerShape(16.dp))
                        ) {
                            AsyncImage(
                                model = uriStr,
                                contentDescription = "Evidencia",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            } else {
                // Estado vacío visual
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No hay fotos adjuntas", color = Color.Gray)
                }
            }

            // Espacio final extra para evitar que el FAB tape contenido al final del scroll
            Spacer(Modifier.height(100.dp))
        }
    }
}

// Componente reutilizable para inputs numéricos
@Composable
fun MetricInput(value: String, onValueChange: (String) -> Unit, label: String, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange, label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true, modifier = modifier
    )
}

@Composable
fun userBorder(width: androidx.compose.ui.unit.Dp, color: Color) = androidx.compose.foundation.BorderStroke(width, color)

private fun crearArchivoTemporal(context: Context): Pair<File, Uri> {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val nombre = "JPEG_${timeStamp}_"
    val directorio = File(context.cacheDir, "images")
    if (!directorio.exists()) directorio.mkdirs()
    val file = File.createTempFile(nombre, ".jpg", directorio)
    val authority = "${context.packageName}.fileprovider"
    val uri = FileProvider.getUriForFile(context, authority, file)
    return Pair(file, uri)
}