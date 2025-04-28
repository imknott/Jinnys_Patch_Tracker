package com.example.jinnyspatchtracker.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.jinnyspatchtracker.DataStoreManager
import com.example.jinnyspatchtracker.Patch
import com.example.jinnyspatchtracker.PatchHistoryContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(navController: NavController) {
    // Earthy color palette
    val backgroundColor = Color(0xFFE6F7E6)
    val primaryColor = Color(0xFF6B8E23)
    val surfaceColor = Color(0xFFF5F5DC)
    val textColor = Color(0xFF4A4A3A)

    val context = LocalContext.current
    val dataStore = remember { DataStoreManager(context) }

    // State declarations
    var patches by remember { mutableStateOf(emptyList<Patch>()) }
    var selectedPatch by remember { mutableStateOf<Patch?>(null) }
    var journalEntry by remember { mutableStateOf<String?>(null) }
    var tabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Patch History", "Journal")

    // Load data
    LaunchedEffect(Unit) {
        patches = dataStore.getAllPatches()
        selectedPatch = patches.firstOrNull()
        journalEntry = dataStore.getAllJournalEntries().toString()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("History", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = primaryColor
                )
            )
        },
        containerColor = backgroundColor
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Tab Row
            TabRow(
                selectedTabIndex = tabIndex,
                containerColor = primaryColor.copy(alpha = 0.2f),
                contentColor = primaryColor
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        text = { Text(title) },
                        selected = tabIndex == index,
                        onClick = { tabIndex = index }
                    )
                }
            }

            when (tabIndex) {
                0 -> PatchHistoryContent(
                    patches = patches,
                    selectedPatch = selectedPatch,
                    onPatchSelected = { selectedPatch = it },
                    surfaceColor = surfaceColor,
                    textColor = textColor
                )
                1 -> JournalHistoryContent(
                    dataStore = dataStore,
                    surfaceColor = surfaceColor,
                    textColor = textColor,
                    primaryColor = primaryColor
                )
            }
        }
    }
}