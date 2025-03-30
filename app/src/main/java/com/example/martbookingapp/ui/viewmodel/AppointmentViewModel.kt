package com.example.martbookingapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.martbookingapp.data.model.Appointment
import com.example.martbookingapp.data.model.AppointmentStatus
import com.example.martbookingapp.data.repository.AppointmentRepository
import com.example.martbookingapp.data.sync.SyncService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class AppointmentViewModel @Inject constructor(
    private val appointmentRepository: AppointmentRepository,
    private val syncService: SyncService
) : ViewModel() {

    private val _appointments = MutableStateFlow<List<Appointment>>(emptyList())
    val appointments: StateFlow<List<Appointment>> = _appointments.asStateFlow()

    private val _selectedDate = MutableStateFlow<LocalDateTime?>(null)
    val selectedDate: StateFlow<LocalDateTime?> = _selectedDate.asStateFlow()

    private val _selectedStatus = MutableStateFlow<AppointmentStatus?>(null)
    val selectedStatus: StateFlow<AppointmentStatus?> = _selectedStatus.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _currentAppointment = MutableStateFlow<Appointment?>(null)
    val currentAppointment: StateFlow<Appointment?> = _currentAppointment.asStateFlow()

    init {
        loadAppointments()
    }

    fun loadAppointments() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                // Subscribe to sync updates which will handle both local and remote data
                syncService.subscribeToAppointmentUpdates()
                    .flowOn(Dispatchers.IO)
                    .collect { appointments ->
                        _appointments.value = appointments
                    }
            } catch (e: Exception) {
                _error.value = "Failed to load appointments: ${e.message}"
                _appointments.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setSelectedDate(date: LocalDateTime?) {
        _selectedDate.value = date
        if (date != null) {
            loadAppointmentsForDate(date)
        }
    }

    fun setSelectedStatus(status: AppointmentStatus?) {
        _selectedStatus.value = status
        if (status != null) {
            loadAppointmentsByStatus(status)
        }
    }

    private fun loadAppointmentsForDate(date: LocalDateTime) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val startOfDay = date.withHour(0).withMinute(0).withSecond(0)
                val endOfDay = date.withHour(23).withMinute(59).withSecond(59)
                appointmentRepository.getAppointmentsByDateRange(startOfDay, endOfDay)
                    .flowOn(Dispatchers.IO)
                    .collect { appointments ->
                        _appointments.value = appointments
                    }
            } catch (e: Exception) {
                _error.value = "Failed to load appointments for date: ${e.message}"
                _appointments.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadAppointmentsByStatus(status: AppointmentStatus) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                appointmentRepository.getAppointmentsByStatus(status.name)
                    .flowOn(Dispatchers.IO)
                    .collect { appointments ->
                        _appointments.value = appointments
                    }
            } catch (e: Exception) {
                _error.value = "Failed to load appointments by status: ${e.message}"
                _appointments.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createAppointment(
        patientId: String,
        dateTime: LocalDateTime,
        notes: String = ""
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val appointment = Appointment(
                    id = java.util.UUID.randomUUID().toString(),
                    patientId = patientId,
                    dateTime = dateTime,
                    status = AppointmentStatus.SCHEDULED.name,
                    notes = notes
                )
                appointmentRepository.insertAppointment(appointment)
            } catch (e: Exception) {
                _error.value = "Failed to create appointment: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadAppointmentById(appointmentId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                appointmentRepository.getAppointmentById(appointmentId)?.let { appointment ->
                    _currentAppointment.value = appointment
                }
            } catch (e: Exception) {
                _error.value = "Failed to load appointment: ${e.message}"
                _currentAppointment.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getAppointmentById(appointmentId: String): Appointment? {
        return _appointments.value.find { it.id == appointmentId }
    }

    fun updateAppointment(
        appointment: Appointment,
        dateTime: LocalDateTime,
        notes: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val updatedAppointment = appointment.copy(
                    dateTime = dateTime,
                    notes = notes
                )
                appointmentRepository.updateAppointment(updatedAppointment)
            } catch (e: Exception) {
                _error.value = "Failed to update appointment: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateAppointmentStatus(appointment: Appointment, status: AppointmentStatus) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val updatedAppointment = appointment.copy(
                    status = status.name
                )
                appointmentRepository.updateAppointment(updatedAppointment)
            } catch (e: Exception) {
                _error.value = "Failed to update appointment status: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteAppointment(appointment: Appointment) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                appointmentRepository.deleteAppointment(appointment)
            } catch (e: Exception) {
                _error.value = "Failed to delete appointment: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
} 