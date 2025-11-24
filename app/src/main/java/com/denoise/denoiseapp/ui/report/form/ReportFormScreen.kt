package com.denoise.denoiseapp.ui.report.form

import android.Manifest
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
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

    // --- LÓGICA DE CÁMARA ---
    var tempUri by remember { mutableStateOf<Uri?>(null) }

    // Launcher: Toma la foto y si es exitoso, avisa al ViewModel
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempUri != null) {
            vm.agregarFoto(tempUri.toString())
        }
    }

    // Permission Launcher: Pide permiso, y si lo tiene, crea archivo y lanza cámara
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val (file, uri) = crearArchivoTemporal(context)
            tempUri = uri
            cameraLauncher.launch(uri)
        }
    }

    Scaffold(
        topBar = { MinimalTopBar(if (state.id == null) "Nuevo reporte" else "Editar reporte") },
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
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ... Campos de Texto (Sin cambios) ...
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
                OutlinedTextField(value = state.linea, onValueChange = vm::onLineaChange, label = { Text("Línea") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = state.lote,  onValueChange = vm::onLoteChange,  label = { Text("Lote")  }, modifier = Modifier.weight(1f))
            }
            OutlinedTextField(
                value = state.notas, onValueChange = vm::onNotasChange,
                label = { Text("Notas") }, modifier = Modifier.fillMaxWidth()
            )

            MinimalSection("Métricas (0–100)")
            // ... Campos de Métricas (Sin cambios) ...
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

            // --- ZONA DE EVIDENCIAS (ACTUALIZADA) ---
            MinimalSection("Evidencias")

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Botón Cámara
                OutlinedButton(
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    modifier = Modifier.padding(end = 12.dp)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Tomar Foto")
                }

                // Texto de ayuda
                if (state.fotosUris.isEmpty()) {
                    Text("Sin fotos", style = MaterialTheme.typography.bodySmall)
                }
            }

            // Lista horizontal de fotos
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().height(110.dp)
            ) {
                items(state.fotosUris) { uriStr ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(100.dp)
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

            Spacer(Modifier.height(48.dp)) // Espacio extra para el FAB
        }
    }
}

/** Crea un archivo temporal en caché y devuelve el File y su URI (FileProvider) */
private fun crearArchivoTemporal(context: Context): Pair<File, Uri> {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val nombre = "JPEG_${timeStamp}_"
    // Usamos cacheDir o filesDir
    val directorio = File(context.cacheDir, "images")
    if (!directorio.exists()) directorio.mkdirs()

    val file = File.createTempFile(nombre, ".jpg", directorio)

    val authority = "${context.packageName}.fileprovider"
    val uri = FileProvider.getUriForFile(context, authority, file)

    return Pair(file, uri)
}