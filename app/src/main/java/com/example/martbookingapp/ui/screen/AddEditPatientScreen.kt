package com.example.martbookingapp.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.martbookingapp.data.model.Patient
import com.example.martbookingapp.ui.viewmodel.PatientViewModel
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditPatientScreen(
    patientId: Long? = null,
    viewModel: PatientViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onPatientSaved: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf<LocalDate?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var address by remember { mutableStateOf("") }
    var medicalHistory by remember { mutableStateOf("") }

    // Date Picker State
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = dateOfBirth?.atStartOfDay()?.toInstant(ZoneOffset.UTC)?.toEpochMilli()
    )

    // Load patient data if editing
    LaunchedEffect(patientId) {
        if (patientId != null) {
            val patient = viewModel.patients.value.find { it.id == patientId }
            patient?.let {
                name = it.name
                email = it.email
                phone = it.phone
                // Parse the date string to LocalDate
                try {
                    dateOfBirth = LocalDate.parse(it.dateOfBirth, DateTimeFormatter.ISO_LOCAL_DATE)
                } catch (e: Exception) {
                    // Handle invalid date format
                }
                address = it.address
                medicalHistory = it.medicalHistory
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (patientId == null) "Add Patient" else "Edit Patient") },
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
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone") },
                modifier = Modifier.fillMaxWidth()
            )

            // Date of Birth with Date Picker
            OutlinedTextField(
                value = dateOfBirth?.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")) ?: "",
                onValueChange = { },
                label = { Text("Date of Birth") },
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

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Address") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = medicalHistory,
                onValueChange = { medicalHistory = it },
                label = { Text("Medical History") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    val patient = Patient(
                        id = patientId ?: 0,
                        name = name,
                        email = email,
                        phone = phone,
                        dateOfBirth = dateOfBirth?.format(DateTimeFormatter.ISO_LOCAL_DATE) ?: "",
                        address = address,
                        medicalHistory = medicalHistory
                    )
                    if (patientId == null) {
                        viewModel.addPatient(patient)
                    } else {
                        viewModel.updatePatient(patient)
                    }
                    onPatientSaved()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = name.isNotBlank() && email.isNotBlank() && phone.isNotBlank() && dateOfBirth != null
            ) {
                Text(if (patientId == null) "Add Patient" else "Save Changes")
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
                                dateOfBirth = LocalDate.ofEpochDay(millis / 86400000)
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
    }
} 