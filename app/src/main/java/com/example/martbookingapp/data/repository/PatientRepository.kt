package com.example.martbookingapp.data.repository

import com.example.martbookingapp.data.local.PatientDao
import com.example.martbookingapp.data.model.Patient
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PatientRepository @Inject constructor(
    private val patientDao: PatientDao
) {
    fun getAllPatients(): Flow<List<Patient>> = patientDao.getAllPatients()

    suspend fun getPatientById(patientId: String): Patient? = patientDao.getPatientById(patientId)

    suspend fun insertPatient(patient: Patient) = patientDao.insertPatient(patient)

    suspend fun updatePatient(patient: Patient) = patientDao.updatePatient(patient)

    suspend fun deletePatient(patient: Patient) = patientDao.deletePatient(patient)

    fun searchPatients(query: String): Flow<List<Patient>> = patientDao.searchPatients(query)

    fun getPatientDao(): PatientDao = patientDao
} 