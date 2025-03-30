package com.example.martbookingapp.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.martbookingapp.data.model.AppointmentType
import com.example.martbookingapp.data.model.Patient
import com.example.martbookingapp.ui.viewmodel.AppointmentViewModel
import com.example.martbookingapp.ui.viewmodel.PatientViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAppointmentScreen(
    viewModel: AppointmentViewModel = hiltViewModel(),
    patientViewModel: PatientViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onAppointmentCreated: () -> Unit
) {
    var selectedPatient by remember { mutableStateOf<Patient?>(null) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var selectedTime by remember { mutableStateOf<LocalTime?>(null) }
    var notes by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showPatientDialog by remember { mutableStateOf(false) }

    val patients by patientViewModel.patients.collectAsState()
    val context = LocalContext.current

    // Date Picker State
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate?.atStartOfDay()?.toInstant(ZoneOffset.UTC)?.toEpochMilli()
    )

    // Time Picker State
    val timePickerState = rememberTimePickerState(
        initialHour = selectedTime?.hour ?: 9,
        initialMinute = selectedTime?.minute ?: 0
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Appointment") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIosNew,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Patient Selection
            OutlinedTextField(
                value = selectedPatient?.name ?: "",
                onValueChange = { },
                label = { Text("Patient") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showPatientDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Patient"
                        )
                    }
                }
            )

            // Date Selection
            OutlinedTextField(
                value = selectedDate?.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")) ?: "",
                onValueChange = { },
                label = { Text("Date") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Select Date"
                        )
                    }
                }
            )

            // Time Selection
            OutlinedTextField(
                value = selectedTime?.format(DateTimeFormatter.ofPattern("hh:mm a")) ?: "",
                onValueChange = { },
                label = { Text("Time") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showTimePicker = true }) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Select Time"
                        )
                    }
                }
            )

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Spacer(modifier = Modifier.weight(1f))

            // Create Button
            Button(
                onClick = {
                    if (selectedPatient != null && selectedDate != null && selectedTime != null) {
                        val dateTime = LocalDateTime.of(selectedDate, selectedTime)
                        viewModel.createAppointment(
                            patientId = selectedPatient!!.id,
                            dateTime = dateTime,
                            notes = notes
                        )
                        onAppointmentCreated()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = selectedPatient != null && selectedDate != null && 
                         selectedTime != null
            ) {
                Text("Create Appointment")
            }
        }

        // Date Picker Dialog
        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                selectedDate = LocalDate.ofEpochDay(millis / 86400000)
                            }
                            showDatePicker = false
                        }
                    ) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(
                    state = datePickerState,
                    showModeToggle = false
                )
            }
        }

        // Time Picker Dialog
        if (showTimePicker) {
            AlertDialog(
                onDismissRequest = { showTimePicker = false },
                title = { Text("Select Time") },
                text = {
                    TimePicker(
                        state = timePickerState
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            selectedTime = LocalTime.of(
                                timePickerState.hour,
                                timePickerState.minute
                            )
                            showTimePicker = false
                        }
                    ) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showTimePicker = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Patient Selection Dialog
        if (showPatientDialog) {
            AlertDialog(
                onDismissRequest = { showPatientDialog = false },
                title = { Text("Select Patient") },
                text = {
                    LazyColumn {
                        items(patients) { patient ->
                            ListItem(
                                headlineContent = { Text(patient.name) },
                                supportingContent = { Text(patient.phone) },
                                modifier = Modifier.clickable {
                                    selectedPatient = patient
                                    showPatientDialog = false
                                }
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showPatientDialog = false }) {
                        Text("Close")
                    }
                }
            )
        }
    }
} 