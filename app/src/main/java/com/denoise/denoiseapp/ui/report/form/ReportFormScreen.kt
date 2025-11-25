package com.denoise.denoiseapp.ui.report.form

import android.Manifest
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.denoise.denoiseapp.presentation.report.FormViewModel
import com.denoise.denoiseapp.ui.components.MinimalSection
import com.denoise.denoiseapp.ui.components.MinimalTopBar
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportFormScreen(
    vm: FormViewModel,
    onSaved: () -> Unit
) {
    val haptics = LocalHapticFeedback.current
    val context = LocalContext.current
    val state by vm.ui

    // Estado para el scroll de la pantalla
    val scrollState = rememberScrollState()

    // Variable para guardar la URI temporal de la foto que se va a tomar
    var tempUri by remember { mutableStateOf<Uri?>(null) }

    // 1. Launcher para la cámara
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempUri != null) {
            vm.agregarFoto(tempUri.toString())
        }
    }

    // 2. Launcher para pedir permisos
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
        topBar = {
            MinimalTopBar(if (state.id == null) "Nuevo reporte" else "Editar reporte")
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    vm.guardar {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSaved()
                    }
                }
            ) { Text("Guardar") }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(scrollState), // <--- AQUÍ AGREGAMOS EL SCROLL
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // --- CAMPOS DE TEXTO ---
            OutlinedTextField(
                value = state.titulo, onValueChange = vm::onTituloChange,
                label = { Text("Título *") },
                isError = state.error != null && state.titulo.isBlank(),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.plantaNombre, onValueChange = vm::onPlantaChange,
                label = { Text("Planta *") },
                isError = state.error != null && state.plantaNombre.isBlank(),
                modifier = Modifier.fillMaxWidth()
            )

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = state.linea, onValueChange = vm::onLineaChange,
                    label = { Text("Línea") }, modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = state.lote,  onValueChange = vm::onLoteChange,
                    label = { Text("Lote")  }, modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = state.notas, onValueChange = vm::onNotasChange,
                label = { Text("Notas") }, modifier = Modifier.fillMaxWidth()
            )

            // --- SECCIÓN DE MÉTRICAS ---
            MinimalSection("Métricas (0–100)")

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = state.porcentajeInfectados, onValueChange = vm::onPorcentajeChange,
                    label = { Text("% Infectados") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = state.porcentajeInfectados.toIntOrNull()?.let { it !in 0..100 } == true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = state.melanosis, onValueChange = vm::onMelanosisChange,
                    label = { Text("Melanosis") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = state.melanosis.toIntOrNull()?.let { it !in 0..100 } == true,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = state.cracking, onValueChange = vm::onCrackingChange,
                    label = { Text("Cracking") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = state.cracking.toIntOrNull()?.let { it !in 0..100 } == true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = state.gaping, onValueChange = vm::onGapingChange,
                    label = { Text("Gaping") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = state.gaping.toIntOrNull()?.let { it !in 0..100 } == true,
                    modifier = Modifier.weight(1f)
                )
            }

            // --- SECCIÓN DE EVIDENCIAS (CÁMARA) ---
            MinimalSection("Evidencias")

            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    modifier = Modifier.padding(end = 12.dp)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Tomar Foto")
                }

                if (state.fotosUris.isEmpty()) {
                    Text("Sin fotos", style = MaterialTheme.typography.bodySmall)
                }
            }

            // Galería horizontal de fotos tomadas (TAMAÑO AUMENTADO)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp) // Aumentado a 280dp
                    .padding(vertical = 8.dp)
            ) {
                items(state.fotosUris) { uriStr ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .size(260.dp) // Aumentado a 260dp para que sea bien grande
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
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

            // Espacio extra al final para que el FAB no tape la última parte al hacer scroll
            Spacer(Modifier.height(80.dp))
        }
    }
}

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