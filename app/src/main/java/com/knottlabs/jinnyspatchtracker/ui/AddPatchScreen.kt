package com.knottlabs.jinnyspatchtracker.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import com.knottlabs.jinnyspatchtracker.DataStoreManager
import com.knottlabs.jinnyspatchtracker.Patch
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPatchScreen(navController: NavController, dataStore: DataStoreManager) {
    // Color palette
    val backgroundColor = Color(0xFFE6F7E6)
    val primaryColor = Color(0xFF6B8E23)
    val textColor = Color(0xFF4A4A3A)
    val surfaceColor = Color(0xFFF5F5DC)

    // State variables
    var patchName by remember { mutableStateOf("") }
    var intervalDays by remember { mutableStateOf("") }
    var lastChangedDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    // Update your date formatter to ensure consistent display:
    val dateTimeFormatter = remember {
        SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault()).apply {
            timeZone = TimeZone.getDefault()
        }
    }
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Add New Patch", color = Color.White) },
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
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Patch Name Field
            OutlinedTextField(
                value = patchName,
                onValueChange = { patchName = it },
                label = { Text("Patch Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = surfaceColor,
                    unfocusedContainerColor = surfaceColor,
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor,
                    focusedIndicatorColor = primaryColor,
                    unfocusedIndicatorColor = primaryColor.copy(alpha = 0.5f),
                    focusedLabelColor = primaryColor,
                    unfocusedLabelColor = primaryColor.copy(alpha = 0.5f),
                    cursorColor = primaryColor
                )
            )

            // Last Changed Date/Time Button
            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = surfaceColor,
                    contentColor = textColor
                )
            ) {
                Text("Last Changed: ${dateTimeFormatter.format(Date(lastChangedDate))}")
            }

            // Change Interval Field
            OutlinedTextField(
                value = intervalDays,
                onValueChange = { intervalDays = it.filter { c -> c.isDigit() } },
                label = { Text("Change Interval (days)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = surfaceColor,
                    unfocusedContainerColor = surfaceColor,
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor,
                    focusedIndicatorColor = primaryColor,
                    unfocusedIndicatorColor = primaryColor.copy(alpha = 0.5f),
                    focusedLabelColor = primaryColor,
                    unfocusedLabelColor = primaryColor.copy(alpha = 0.5f),
                    cursorColor = primaryColor
                )
            )

            // Date & Time Picker Dialog
            if (showDatePicker) {
                val datePickerState = rememberDatePickerState(
                    initialSelectedDateMillis = lastChangedDate
                )
                val timePickerState = rememberTimePickerState(
                    initialHour = Calendar.getInstance().apply { timeInMillis = lastChangedDate }.get(Calendar.HOUR_OF_DAY),
                    initialMinute = Calendar.getInstance().apply { timeInMillis = lastChangedDate }.get(Calendar.MINUTE)
                )

                Dialog(
                    onDismissRequest = { showDatePicker = false },
                    properties = DialogProperties(usePlatformDefaultWidth = false)
                ) {
                    Card(
                        modifier = Modifier
                            .widthIn(min = 340.dp, max = 400.dp)
                            .heightIn(max = 600.dp)
                            .padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = surfaceColor,
                            contentColor = textColor
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Select Last Changed Date/Time",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            // Date Picker
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(400.dp)
                            ) {
                                DatePicker(
                                    state = datePickerState,
                                    modifier = Modifier.fillMaxSize(),
                                    colors = DatePickerDefaults.colors(
                                        containerColor = surfaceColor,
                                        titleContentColor = textColor,
                                        headlineContentColor = textColor,
                                        weekdayContentColor = textColor,
                                        subheadContentColor = textColor,
                                        navigationContentColor = primaryColor,
                                        yearContentColor = textColor,
                                        currentYearContentColor = primaryColor,
                                        selectedYearContentColor = Color.White,
                                        disabledYearContentColor = textColor.copy(alpha = 0.3f),
                                        selectedYearContainerColor = primaryColor,
                                        dayContentColor = textColor,
                                        disabledDayContentColor = textColor.copy(alpha = 0.3f),
                                        selectedDayContentColor = Color.White,
                                        selectedDayContainerColor = primaryColor,
                                        todayContentColor = primaryColor,
                                        todayDateBorderColor = primaryColor
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Time Picker
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp)
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                TimePicker(
                                    state = timePickerState,
                                    modifier = Modifier.fillMaxSize(),
                                    colors = TimePickerDefaults.colors(
                                        clockDialColor = surfaceColor,
                                        clockDialSelectedContentColor = Color.White,
                                        clockDialUnselectedContentColor = textColor,
                                        selectorColor = primaryColor,
                                        containerColor = surfaceColor,
                                        periodSelectorBorderColor = primaryColor.copy(alpha = 0.5f),
                                        periodSelectorSelectedContainerColor = primaryColor,
                                        periodSelectorUnselectedContainerColor = surfaceColor,
                                        periodSelectorSelectedContentColor = Color.White,
                                        periodSelectorUnselectedContentColor = textColor,
                                        timeSelectorSelectedContainerColor = primaryColor,
                                        timeSelectorUnselectedContainerColor = surfaceColor,
                                        timeSelectorSelectedContentColor = Color.White,
                                        timeSelectorUnselectedContentColor = textColor
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Action Buttons
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = { showDatePicker = false },
                                    colors = ButtonDefaults.textButtonColors(
                                        contentColor = textColor
                                    )
                                ) {
                                    Text("Cancel")
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        datePickerState.selectedDateMillis?.let { dateMillis ->
                                            // Create a calendar instance with the selected date at midnight UTC
                                            val utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                                                timeInMillis = dateMillis
                                            }

                                            // Get the date components (year, month, day) from UTC calendar
                                            val year = utcCalendar.get(Calendar.YEAR)
                                            val month = utcCalendar.get(Calendar.MONTH)
                                            val day = utcCalendar.get(Calendar.DAY_OF_MONTH)

                                            // Create a new calendar in local timezone with the same date components
                                            val localCalendar = Calendar.getInstance().apply {
                                                set(Calendar.YEAR, year)
                                                set(Calendar.MONTH, month)
                                                set(Calendar.DAY_OF_MONTH, day)
                                                set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                                                set(Calendar.MINUTE, timePickerState.minute)
                                                set(Calendar.SECOND, 0)
                                                set(Calendar.MILLISECOND, 0)
                                            }

                                            lastChangedDate = localCalendar.timeInMillis
                                        }
                                        showDatePicker = false
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = primaryColor,
                                        contentColor = Color.White
                                    )) {
                                    Text("Confirm")
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Save Button
            Button(
                onClick = {
                    val days = intervalDays.toIntOrNull() ?: 0
                    if (patchName.isNotBlank() && days > 0) {
                        scope.launch {
                            val newId = dataStore.getNextPatchId()
                            val newPatch = Patch(
                                id = newId,
                                name = patchName,
                                changeIntervalDays = days,
                                history = listOf(lastChangedDate)
                            )
                            dataStore.savePatch(newPatch)
                            Toast.makeText(context, "Patch added!", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        }
                    } else {
                        Toast.makeText(context, "Please fill all fields correctly", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor,
                    contentColor = Color.White
                ),
                enabled = patchName.isNotBlank() && intervalDays.isNotBlank() && intervalDays.toIntOrNull()?.let { it > 0 } ?: false
            ) {
                Text("Save Patch")
            }
        }
    }
}