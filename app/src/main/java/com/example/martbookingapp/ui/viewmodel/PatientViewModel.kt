package com.example.martbookingapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.martbookingapp.data.model.Patient
import com.example.martbookingapp.data.repository.PatientRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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
            patientRepository.getAllPatients().collect { patients ->
                _patients.value = patients
            }
        }
    }

    fun searchPatients(query: String) {
        _searchQuery.value = query
        viewModelScope.launch {
            patientRepository.searchPatients(query).collect { patients ->
                _patients.value = patients
            }
        }
    }

    fun addPatient(patient: Patient) {
        viewModelScope.launch {
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
} 