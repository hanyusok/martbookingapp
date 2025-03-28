package com.example.martbookingapp.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.martbookingapp.data.model.Appointment
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
fun EditAppointmentScreen(
    appointmentId: Long,
    viewModel: AppointmentViewModel = hiltViewModel(),
    patientViewModel: PatientViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onAppointmentUpdated: () -> Unit
) {
    val patients by patientViewModel.patients.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val context = LocalContext.current

    var selectedPatient by remember { mutableStateOf<Patient?>(null) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var selectedTime by remember { mutableStateOf<LocalTime?>(null) }
    var selectedType by remember { mutableStateOf<AppointmentType?>(null) }
    var notes by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showPatientDialog by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var currentAppointment by remember { mutableStateOf<Appointment?>(null) }
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }

    // Filter patients based on search query
    val filteredPatients = remember(patients, searchQuery.text) {
        if (searchQuery.text.isBlank()) {
            patients
        } else {
            patients.filter { patient ->
                patient.name.contains(searchQuery.text, ignoreCase = true) ||
                patient.phone.contains(searchQuery.text, ignoreCase = true)
            }
        }
    }

    // Load and initialize appointment data
    LaunchedEffect(appointmentId) {
        try {
            viewModel.loadAppointmentById(appointmentId)
            val appointment = viewModel.getAppointmentById(appointmentId)
            if (appointment != null) {
                currentAppointment = appointment
                selectedDate = appointment.dateTime.toLocalDate()
                selectedTime = appointment.dateTime.toLocalTime()
                selectedType = appointment.type
                notes = appointment.notes
                // Find and set the patient
                patients.find { patient -> patient.id == appointment.patientId }?.let { patient ->
                    selectedPatient = patient
                }
            } else {
                errorMessage = "Appointment not found"
                showErrorDialog = true
            }
        } catch (e: Exception) {
            errorMessage = "Error loading appointment: ${e.message}"
            showErrorDialog = true
        }
    }

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
                title = { Text("Edit Appointment") },
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(50.dp)
                        .align(Alignment.Center)
                )
            } else if (currentAppointment == null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Appointment not found",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onNavigateBack) {
                        Text("Go Back")
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
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

                    // Appointment Type Selection
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedType?.name ?: "",
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Appointment Type") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            AppointmentType.values().forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type.name) },
                                    onClick = {
                                        selectedType = type
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Notes
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // Update Button
                    Button(
                        onClick = {
                            if (selectedPatient != null && selectedDate != null && selectedTime != null && selectedType != null) {
                                val dateTime = LocalDateTime.of(selectedDate, selectedTime)
                                currentAppointment?.let {
                                    viewModel.updateAppointment(
                                        appointment = it,
                                        dateTime = dateTime,
                                        type = selectedType!!,
                                        notes = notes
                                    )
                                    onAppointmentUpdated()
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        enabled = selectedPatient != null && selectedDate != null && 
                                 selectedTime != null && selectedType != null && !isLoading
                    ) {
                        Text("Update Appointment")
                    }
                }
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
                onDismissRequest = { 
                    showPatientDialog = false
                    searchQuery = TextFieldValue("") // Reset search query when dialog is closed
                },
                title = { Text("Select Patient") },
                text = {
                    Column {
                        // Search TextField
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = { Text("Search Patient") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                            trailingIcon = {
                                if (searchQuery.text.isNotBlank()) {
                                    IconButton(onClick = { searchQuery = TextFieldValue("") }) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Clear Search"
                                        )
                                    }
                                }
                            }
                        )

                        // Patient List
                        LazyColumn(
                            modifier = Modifier.height(300.dp)
                        ) {
                            if (filteredPatients.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (searchQuery.text.isBlank()) 
                                                "No patients found" 
                                            else 
                                                "No patients match your search",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            } else {
                                items(filteredPatients) { patient ->
                                    ListItem(
                                        headlineContent = { 
                                            Text(
                                                text = patient.name,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        },
                                        supportingContent = { 
                                            Text(
                                                text = patient.phone,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        },
                                        modifier = Modifier.clickable {
                                            selectedPatient = patient
                                            showPatientDialog = false
                                            searchQuery = TextFieldValue("") // Reset search query
                                        }
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { 
                            showPatientDialog = false
                            searchQuery = TextFieldValue("") // Reset search query
                        }
                    ) {
                        Text("Close")
                    }
                }
            )
        }

        // Error Dialog
        if (showErrorDialog) {
            AlertDialog(
                onDismissRequest = { showErrorDialog = false },
                title = { Text("Error") },
                text = { Text(errorMessage) },
                confirmButton = {
                    TextButton(onClick = { showErrorDialog = false }) {
                        Text("OK")
                    }
                }
            )
        }
    }
} 