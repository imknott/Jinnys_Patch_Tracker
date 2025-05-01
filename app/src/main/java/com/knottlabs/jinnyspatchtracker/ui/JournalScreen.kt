package com.knottlabs.jinnyspatchtracker.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.knottlabs.jinnyspatchtracker.DataStoreManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalScreen(navController: NavController, dataStore: DataStoreManager) {
    // Earthy color palette
    val backgroundColor = Color(0xFFE6F7E6)
    val primaryColor = Color(0xFF6B8E23)
    val surfaceColor = Color(0xFFF5F5DC)
    val textColor = Color(0xFF4A4A3A)
    val ratingSelectedColor = Color(0xFF556B2F)
    val ratingUnselectedColor = Color(0xFFC1D8AC)
    val outlineColor = Color(0xFF4A4A3A)

    // State management
    var responses by remember { mutableStateOf<MutableMap<Int, String>>(mutableMapOf()) }
    var currentResponse by remember { mutableStateOf(TextFieldValue("")) }
    var moodRating by remember { mutableStateOf<Int?>(null) }
    var currentQuestionIndex by remember { mutableStateOf(0) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Guided journal questions
    val journalQuestions = listOf(
        "How was your overall mood today? (1-5)",
        "Describe any physical sensations at/near your patch site",
        "Did you experience any side effects? (e.g., headaches, nausea)",
        "What self-care practices helped you today?",
        "Additional notes about your patch experience"
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Daily Check-In", color = textColor) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = textColor
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = backgroundColor
                )
            )
        },
        containerColor = backgroundColor
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Progress indicator
                LinearProgressIndicator(
                    progress = { (currentQuestionIndex + 1).toFloat() / journalQuestions.size },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = primaryColor
                )

                Spacer(Modifier.height(16.dp))

                // Current question
                Text(
                    text = journalQuestions[currentQuestionIndex],
                    style = MaterialTheme.typography.titleMedium.copy(color = textColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )

                // Special input for mood rating question
                if (currentQuestionIndex == 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        (1..5).forEach { number ->
                            MoodRatingButton(
                                number = number,
                                isSelected = moodRating == number,
                                selectedColor = ratingSelectedColor,
                                unselectedColor = ratingUnselectedColor,
                                onClick = {
                                    moodRating = number
                                    responses[0] = number.toString()
                                }
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                } else {
                    // Text input for other questions
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(surfaceColor)
                            .border(
                                width = 1.dp,
                                color = outlineColor.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(12.dp))
                    ) {
                        BasicTextField(
                            value = currentResponse,
                            onValueChange = { currentResponse = it },
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            textStyle = TextStyle(
                                fontSize = 16.sp,
                                color = textColor
                            ),
                            decorationBox = { innerTextField ->
                                if (currentResponse.text.isEmpty()) {
                                    Text(
                                        text = "Type your response...",
                                        color = textColor.copy(alpha = 0.4f),
                                        fontSize = 16.sp
                                    )
                                }
                                innerTextField()
                            }
                        )
                    }
                }

                Spacer(Modifier.weight(1f))

                // Navigation buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (currentQuestionIndex > 0) {
                        OutlinedButton(
                            onClick = {
                                // Save current response before going back
                                if (currentQuestionIndex > 0) {
                                    responses[currentQuestionIndex] = currentResponse.text
                                    currentQuestionIndex--
                                    currentResponse = TextFieldValue(responses[currentQuestionIndex] ?: "")
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = primaryColor
                            )
                        ) {
                            Text("Previous")
                        }
                        Spacer(Modifier.width(8.dp))
                    }

                    Button(
                        onClick = {
                            if (currentQuestionIndex == 0) {
                                // For mood rating question, we already saved it in the onClick above
                            } else {
                                // Save current response before moving to next question
                                responses[currentQuestionIndex] = currentResponse.text
                            }

                            if (currentQuestionIndex < journalQuestions.size - 1) {
                                currentQuestionIndex++
                                currentResponse = TextFieldValue(responses[currentQuestionIndex] ?: "")
                            } else {
                                // Final submission - build complete entry
                                scope.launch {
                                    val fullEntry = buildString {
                                        journalQuestions.forEachIndexed { index, question ->
                                            append("$question\n")
                                            append("${responses[index] ?: "No response"}\n\n")
                                        }
                                    }
                                    dataStore.saveJournalEntry(fullEntry, moodRating)
                                    Toast.makeText(context, "Journal saved!", Toast.LENGTH_SHORT).show()
                                    navController.navigate("patch")
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor,
                            contentColor = Color.White
                        )
                    ) {
                        Text(if (currentQuestionIndex < journalQuestions.size - 1) "Next" else "Submit")
                    }
                }
            }
        }
    }
}

@Composable
fun MoodRatingButton(
    number: Int,
    isSelected: Boolean,
    selectedColor: Color,
    unselectedColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(if (isSelected) selectedColor else unselectedColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number.toString(),
            color = if (isSelected) Color.White else Color.Black.copy(alpha = 0.6f),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}