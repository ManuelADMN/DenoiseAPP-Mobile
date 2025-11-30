package com.denoise.denoiseapp.ui.report.list

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.denoise.denoiseapp.domain.model.ReporteEstado

/**
 * Lista horizontal de chips para filtrar por estado.
 *
 * @param options   Estados disponibles.
 * @param selected  Estado seleccionado (o null para "Todos").
 * @param onSelect  Callback con el nuevo estado seleccionado (o null para limpiar).
 * @param space     Separación entre chips (usa Dp, no Int).
 */
@Composable
fun FilterList(
    options: List<ReporteEstado>,
    selected: ReporteEstado?,
    onSelect: (ReporteEstado?) -> Unit,
    modifier: Modifier = Modifier,
    space: Dp = 8.dp,
) {
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(space)
    ) {
        // Chip "Todos" (selección nula)
        FilterChip(
            selected = selected == null,
            onClick = { onSelect(null) },
            label = { Text(text = "Todos") }
        )

        // Chips por cada estado
        options.forEach { est ->
            FilterChip(
                selected = selected == est,
                onClick = { onSelect(est) },
                label = { Text(text = est.name) }, // ajusta si tienes un displayName
                modifier = Modifier.padding(end = 0.dp)
            )
        }
    }
}
