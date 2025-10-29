package com.denoise.denoiseapp.ui.report.form

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.denoise.denoiseapp.presentation.report.FormViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportFormScreen(
    vm: FormViewModel,   // mantenemos la firma
    onSaved: () -> Unit
) {
    val haptics = LocalHapticFeedback.current

    // Estado local (independiente del VM para evitar incompatibilidades)
    var titulo by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue("")) }
    var planta by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue("")) }
    var notas  by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue("")) }

    var errorTitulo by rememberSaveable { mutableStateOf<String?>(null) }
    var errorPlanta by rememberSaveable { mutableStateOf<String?>(null) }

    var evidencias by rememberSaveable { mutableStateOf<List<Uri>>(emptyList()) }

    // Photo Picker (estable)
    val pickMedia = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5)
    ) { uris ->
        evidencias = uris ?: emptyList()
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Nuevo Reporte") }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    // Validación mínima
                    errorTitulo = if (titulo.text.isBlank()) "El título es obligatorio" else null
                    errorPlanta = if (planta.text.isBlank()) "La planta es obligatoria" else null

                    if (errorTitulo == null && errorPlanta == null) {
                        // (Opcional) Si tu VM tiene guardar(...), llama aquí:
                        // vm.guardar(titulo.text, planta.text, notas.text, evidencias)

                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSaved()
                    }
                }
            ) {
                Text("Guardar")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = titulo,
                onValueChange = {
                    titulo = it
                    if (errorTitulo != null && it.text.isNotBlank()) errorTitulo = null
                },
                label = { Text("Título *") },
                isError = errorTitulo != null,
                supportingText = {
                    errorTitulo?.let { msg ->
                        Text(msg, color = MaterialTheme.colorScheme.error)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = planta,
                onValueChange = {
                    planta = it
                    if (errorPlanta != null && it.text.isNotBlank()) errorPlanta = null
                },
                label = { Text("Planta *") },
                isError = errorPlanta != null,
                supportingText = {
                    errorPlanta?.let { msg ->
                        Text(msg, color = MaterialTheme.colorScheme.error)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = notas,
                onValueChange = { notas = it },
                label = { Text("Notas") },
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider()

            Text("Evidencias (opcional)")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = {
                    pickMedia.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                    )
                }) { Text("Agregar evidencias") }

                if (evidencias.isNotEmpty()) {
                    Text("${evidencias.size} seleccionadas", color = MaterialTheme.colorScheme.primary)
                }
            }

            if (evidencias.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(evidencias) { uri ->
                        Card(
                            modifier = Modifier.size(80.dp),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            AsyncImage(model = uri, contentDescription = "Evidencia")
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
            Text(
                text = "Campos con * son obligatorios",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
