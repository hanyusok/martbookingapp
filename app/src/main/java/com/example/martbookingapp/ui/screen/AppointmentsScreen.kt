package com.example.martbookingapp.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.martbookingapp.data.model.Appointment
import com.example.martbookingapp.data.model.AppointmentStatus
import com.example.martbookingapp.ui.viewmodel.AppointmentViewModel
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentsScreen(
    viewModel: AppointmentViewModel = hiltViewModel(),
    onNavigateToAddAppointment: () -> Unit,
    onNavigateToEditAppointment: (String) -> Unit
) {
    val appointments by viewModel.appointments.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    var showDeleteDialog by remember { mutableStateOf<Appointment?>(null) }
    var showStatusDialog by remember { mutableStateOf<Appointment?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Appointments") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddAppointment) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Appointment"
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                error != null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = error!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadAppointments() }) {
                            Text("Retry")
                        }
                    }
                }
                appointments.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No appointments found",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onNavigateToAddAppointment) {
                            Text("Add Appointment")
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(appointments) { appointment ->
                            AppointmentCard(
                                appointment = appointment,
                                onEditClick = { onNavigateToEditAppointment(appointment.id) },
                                onDeleteClick = { showDeleteDialog = appointment },
                                onStatusClick = { showStatusDialog = appointment }
                            )
                        }
                    }
                }
            }
        }

        // Delete Confirmation Dialog
        showDeleteDialog?.let { appointment ->
            AlertDialog(
                onDismissRequest = { showDeleteDialog = null },
                title = { Text("Delete Appointment") },
                text = { Text("Are you sure you want to delete this appointment?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteAppointment(appointment)
                            showDeleteDialog = null
                        }
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = null }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Status Update Dialog
        showStatusDialog?.let { appointment ->
            AlertDialog(
                onDismissRequest = { showStatusDialog = null },
                title = { Text("Update Status") },
                text = {
                    Column {
                        AppointmentStatus.entries.forEach { status ->
                            ListItem(
                                headlineContent = { Text(status.name) },
                                modifier = Modifier.clickable {
                                    viewModel.updateAppointmentStatus(appointment, status)
                                    showStatusDialog = null
                                }
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showStatusDialog = null }) {
                        Text("Close")
                    }
                }
            )
        }
    }
}

@Composable
private fun AppointmentCard(
    appointment: Appointment,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onStatusClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onEditClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = appointment.dateTime.format(DateTimeFormatter.ofPattern("MMM d, yyyy hh:mm a")),
                    style = MaterialTheme.typography.titleMedium
                )
                Row {
                    IconButton(onClick = onStatusClick) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Update Status"
                        )
                    }
                    IconButton(onClick = onEditClick) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit"
                        )
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete"
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Status: ${appointment.status}",
                style = MaterialTheme.typography.bodyMedium
            )
            if (appointment.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Notes: ${appointment.notes}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
} 