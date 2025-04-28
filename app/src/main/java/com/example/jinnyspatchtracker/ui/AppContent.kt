package com.example.jinnyspatchtracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.*
import com.example.jinnyspatchtracker.DataStoreManager
import kotlinx.coroutines.delay
@Composable
fun MainApp() {
    var showSplash by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(6000)
        showSplash = false
    }

    if (showSplash) {
        SplashScreen()

    } else {
        AppContent()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppContent() {
    // Earthy color palette
    val backgroundColor = Color(0xFFE6F7E6)
    val primaryColor = Color(0xFF6B8E23)
    val surfaceColor = Color(0xFFF5F5DC)
    val textColor = Color(0xFF4A4A3A)

    val navController = rememberNavController()
    val context = LocalContext.current
    val dataStore = remember { DataStoreManager(context) }

    Scaffold(
        topBar = {
            ModernTopAppBar(
                backgroundColor = primaryColor,
                textColor = Color.White
            )
        },
        containerColor = backgroundColor
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "patch",
            modifier = Modifier
                .padding(paddingValues)
                .background(backgroundColor)
        ) {
            composable("patch") {
                PatchScreen(navController, dataStore)
            }
            composable("history") {
                HistoryScreen(navController)
            }
            composable("journal") {
                JournalScreen(navController, dataStore)
            }
            composable("add_patch") {
                AddPatchScreen(navController, dataStore)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernTopAppBar(
    backgroundColor: Color,
    textColor: Color
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "Jinny's Patch Tracker",
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
            )
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = backgroundColor,
            titleContentColor = textColor,
            actionIconContentColor = textColor,
            navigationIconContentColor = textColor
        ),
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(16.dp))
    )
}
