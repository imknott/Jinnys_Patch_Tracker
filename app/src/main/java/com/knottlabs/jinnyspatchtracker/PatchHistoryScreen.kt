package com.knottlabs.jinnyspatchtracker

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PatchHistoryContent(
    patches: List<Patch>,
    selectedPatch: Patch?,
    onPatchSelected: (Patch) -> Unit,
    surfaceColor: Color,
    textColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (patches.isNotEmpty()) {
            // Patch selector
            Text(
                "Select Patch:",
                color = textColor.copy(alpha = 0.8f),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                patches.forEach { patch ->
                    FilterChip(
                        selected = selectedPatch?.id == patch.id,
                        onClick = { onPatchSelected(patch) },
                        label = { Text(patch.name) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = surfaceColor,
                            selectedLabelColor = textColor,
                            containerColor = surfaceColor.copy(alpha = 0.6f),
                            labelColor = textColor.copy(alpha = 0.6f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // History list
            selectedPatch?.let { patch ->
                if (patch.history.isNotEmpty()) {
                    Text(
                        "Change History for ${patch.name}:",
                        style = MaterialTheme.typography.titleMedium,
                        color = textColor,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(patch.history.sortedDescending()) { timestamp ->
                            HistoryCard(
                                text = formatDate(timestamp),
                                surfaceColor = surfaceColor,
                                textColor = textColor
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No change history for ${patch.name}",
                            color = textColor.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No patches available",
                    color = textColor.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun HistoryCard(text: String, surfaceColor: Color, textColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = surfaceColor,
            contentColor = textColor
        )
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

private fun formatDate(ms: Long): String {
    val sdf = SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault())
    return sdf.format(Date(ms))
}