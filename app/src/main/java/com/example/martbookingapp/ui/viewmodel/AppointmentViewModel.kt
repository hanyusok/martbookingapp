package com.example.martbookingapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.martbookingapp.data.model.Appointment
import com.example.martbookingapp.data.model.AppointmentStatus
import com.example.martbookingapp.data.model.AppointmentType
import com.example.martbookingapp.data.repository.AppointmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class AppointmentViewModel @Inject constructor(
    private val appointmentRepository: AppointmentRepository
) : ViewModel() {

    private val _appointments = MutableStateFlow<List<Appointment>>(emptyList())
    val appointments: StateFlow<List<Appointment>> = _appointments.asStateFlow()

    private val _selectedDate = MutableStateFlow<LocalDateTime?>(null)
    val selectedDate: StateFlow<LocalDateTime?> = _selectedDate.asStateFlow()

    private val _selectedStatus = MutableStateFlow<AppointmentStatus?>(null)
    val selectedStatus: StateFlow<AppointmentStatus?> = _selectedStatus.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadAppointments()
    }

    private fun loadAppointments() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                appointmentRepository.getAllAppointments().collect { appointments ->
                    _appointments.value = appointments
                }
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
            try {
                val startOfDay = date.withHour(0).withMinute(0).withSecond(0)
                val endOfDay = date.withHour(23).withMinute(59).withSecond(59)
                appointmentRepository.getAppointmentsByDateRange(startOfDay, endOfDay)
                    .collect { appointments ->
                        _appointments.value = appointments
                    }
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadAppointmentsByStatus(status: AppointmentStatus) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                appointmentRepository.getAppointmentsByStatus(status)
                    .collect { appointments ->
                        _appointments.value = appointments
                    }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createAppointment(
        patientId: Long,
        dateTime: LocalDateTime,
        type: AppointmentType,
        notes: String = ""
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val appointment = Appointment(
                    patientId = patientId,
                    dateTime = dateTime,
                    status = AppointmentStatus.SCHEDULED,
                    type = type,
                    notes = notes
                )
                appointmentRepository.insertAppointment(appointment)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateAppointmentStatus(appointment: Appointment, newStatus: AppointmentStatus) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val updatedAppointment = appointment.copy(status = newStatus)
                appointmentRepository.updateAppointment(updatedAppointment)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateAppointment(
        appointment: Appointment,
        dateTime: LocalDateTime,
        type: AppointmentType,
        notes: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val updatedAppointment = appointment.copy(
                    dateTime = dateTime,
                    type = type,
                    notes = notes
                )
                appointmentRepository.updateAppointment(updatedAppointment)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteAppointment(appointment: Appointment) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                appointmentRepository.deleteAppointment(appointment)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getAppointmentById(appointmentId: Long): Appointment? {
        return _appointments.value.find { it.id == appointmentId }
    }

    fun loadAppointmentById(appointmentId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // First try to find in current list
                val appointment = _appointments.value.find { it.id == appointmentId }
                if (appointment == null) {
                    // If not found, load from repository
                    appointmentRepository.getAppointmentById(appointmentId)?.let { loadedAppointment ->
                        _appointments.value = _appointments.value + loadedAppointment
                    }
                }
            } finally {
                _isLoading.value = false
            }
        }
    }
} 