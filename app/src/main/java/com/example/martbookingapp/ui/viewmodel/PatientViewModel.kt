package com.example.martbookingapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.martbookingapp.data.model.Patient
import com.example.martbookingapp.data.repository.PatientRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class PatientViewModel @Inject constructor(
    private val patientRepository: PatientRepository
) : ViewModel() {

    private val _patients = MutableStateFlow<List<Patient>>(emptyList())
    val patients: StateFlow<List<Patient>> = _patients.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        loadPatients()
    }

    private fun loadPatients() {
        viewModelScope.launch {
            patientRepository.getAllPatients()
                .flowOn(Dispatchers.IO)
                .collect { patients ->
                    _patients.value = patients
                }
        }
    }

    fun searchPatients(query: String) {
        _searchQuery.value = query
        viewModelScope.launch {
            patientRepository.searchPatients(query)
                .flowOn(Dispatchers.IO)
                .collect { patients ->
                    _patients.value = patients
                }
        }
    }

    fun createPatient(
        name: String,
        email: String,
        phone: String,
        dateOfBirth: LocalDate,
        address: String,
        medicalHistory: String = ""
    ) {
        viewModelScope.launch {
            val patient = Patient(
                id = java.util.UUID.randomUUID().toString(),
                name = name,
                email = email,
                phone = phone,
                dateOfBirth = dateOfBirth,
                address = address,
                medicalHistory = medicalHistory
            )
            patientRepository.insertPatient(patient)
        }
    }

    fun updatePatient(patient: Patient) {
        viewModelScope.launch {
            patientRepository.updatePatient(patient)
        }
    }

    fun deletePatient(patient: Patient) {
        viewModelScope.launch {
            patientRepository.deletePatient(patient)
        }
    }

    fun getPatientById(patientId: String): Patient? {
        return _patients.value.find { it.id == patientId }
    }
} 