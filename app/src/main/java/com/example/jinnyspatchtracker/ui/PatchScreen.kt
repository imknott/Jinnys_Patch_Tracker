package com.example.jinnyspatchtracker.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.jinnyspatchtracker.DataStoreManager
import com.example.jinnyspatchtracker.Patch
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import java.util.*

@Composable
fun PatchScreen(navController: NavController, dataStore: DataStoreManager) {
    // Color palette
    val backgroundColor = Color(0xFFE6F7E6)
    val primaryColor = Color(0xFF6B8E23)
    val secondaryColor = Color(0xFF8FBC8F)
    val surfaceColor = Color(0xFFF5F5DC)
    val textColor = Color(0xFF4A4A3A)
    val errorColor = Color(0xFFB22222)

    var showMenu by remember { mutableStateOf(false) }
    var patches by remember { mutableStateOf(emptyList<Patch>()) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var patchToDelete by remember { mutableStateOf<Patch?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dateTimeFormatter = remember { SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault()) }

    // Load patches
    LaunchedEffect(Unit) {
        patches = dataStore.getAllPatches()
    }

    // Delete dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Patch") },
            text = { Text("Are you sure you want to delete this patch?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            patchToDelete?.let { patch ->
                                dataStore.deletePatch(patch.id)
                                patches = dataStore.getAllPatches()
                            }
                            showDeleteDialog = false
                        }
                    }
                ) {
                    Text("Delete", color = errorColor)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(backgroundColor)) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            if (patches.isEmpty()) {
                Text(
                    "No patches added yet",
                    color = textColor.copy(alpha = 0.6f),
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.Center)
                )
            } else {
                patches.forEach { patch ->
                    PatchCard(
                        patch = patch,
                        onUpdate = { updatedPatch ->
                            scope.launch {
                                dataStore.updatePatch(updatedPatch)
                                patches = dataStore.getAllPatches()
                            }
                        },
                        onDelete = {
                            patchToDelete = patch
                            showDeleteDialog = true
                        },
                        dateTimeFormatter = dateTimeFormatter,
                        primaryColor = primaryColor,
                        surfaceColor = surfaceColor,
                        textColor = textColor,
                        errorColor = errorColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        // FAB Menu
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomEnd
        ) {
            // Main FAB button
            FloatingActionButton(
                onClick = { showMenu = !showMenu },
                modifier = Modifier.padding(16.dp),
                containerColor = primaryColor,
                contentColor = Color.White
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = if (showMenu) "Close menu" else "Open menu",
                    modifier = Modifier.rotate(if (showMenu) 45f else 0f)
                )
            }

            // Expanded menu items
            AnimatedVisibility(
                visible = showMenu,
                modifier = Modifier.padding(end = 16.dp, bottom = 80.dp), // Better positioning
                enter = fadeIn() + expandVertically(expandFrom = Alignment.Bottom),
                exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Bottom)
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(12.dp) // Better spacing
                ) {
                    // History button
                    ExtendedFloatingActionButton(
                        onClick = {
                            showMenu = false
                            navController.navigate("history")
                        },
                        modifier = Modifier.width(150.dp), // Consistent width
                        containerColor = secondaryColor,
                        contentColor = textColor,
                        text = { Text("History") },
                        icon = {
                            Icon(
                                imageVector = Icons.Rounded.Info,
                                contentDescription = "History"
                            )
                        }
                    )

                    // Add Patch button (only shown if less than 3 patches)
                    if (patches.size < 3) {
                        ExtendedFloatingActionButton(
                            onClick = {
                                showMenu = false
                                navController.navigate("add_patch")
                            },
                            modifier = Modifier.width(150.dp),
                            containerColor = secondaryColor,
                            contentColor = textColor,
                            text = { Text("Add Patch") },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add icon"
                                )
                            }
                        )
                    }

                    // Journal button
                    ExtendedFloatingActionButton(
                        onClick = {
                            showMenu = false
                            navController.navigate("journal")
                        },
                        modifier = Modifier.width(150.dp),
                        containerColor = secondaryColor,
                        contentColor = textColor,
                        text = { Text("Journal") },
                        icon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.MenuBook, // Add your icon
                                contentDescription = "Journal"
                            )
                        }
                    )
                }
            }
        }
    }
}


@Composable
fun PatchCard(
    patch: Patch,
    onUpdate: (Patch) -> Unit,
    onDelete: () -> Unit,
    dateTimeFormatter: SimpleDateFormat,
    primaryColor: Color,
    surfaceColor: Color,
    textColor: Color,
    errorColor: Color
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val now = System.currentTimeMillis()

    // Calculate next change date
    val lastChanged = patch.history.maxOrNull() ?: now
    val nextChangeMillis = lastChanged + (patch.changeIntervalDays * 24 * 60 * 60 * 1000L)
    val isDue = nextChangeMillis <= now

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = surfaceColor,
            contentColor = textColor
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = patch.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = primaryColor
                )
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = errorColor)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text("Change every ${patch.changeIntervalDays} days")

            Text(
                text = if (isDue) {
                    "Due now! (since ${dateTimeFormatter.format(Date(nextChangeMillis))})"
                } else {
                    "Next change: ${dateTimeFormatter.format(Date(nextChangeMillis))}"
                },
                color = if (isDue) errorColor else primaryColor
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    scope.launch {
                        val updatedPatch = patch.copy(
                            history = patch.history + now
                        )
                        onUpdate(updatedPatch)
                        Toast.makeText(context, "${patch.name} changed!", Toast.LENGTH_SHORT).show()
                    }
                },
                enabled = isDue,
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor,
                    contentColor = Color.White,
                    disabledContainerColor = primaryColor.copy(alpha = 0.3f),
                    disabledContentColor = Color.White.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isDue) "Mark as Changed" else "Not Due Yet")
            }
        }
    }
}