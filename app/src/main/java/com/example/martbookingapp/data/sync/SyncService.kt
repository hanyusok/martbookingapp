package com.example.martbookingapp.data.sync

import com.example.martbookingapp.data.local.AppointmentDao
import com.example.martbookingapp.data.local.PatientDao
import com.example.martbookingapp.data.remote.SupabaseDataSource
import com.example.martbookingapp.data.model.Appointment
import com.example.martbookingapp.data.model.Patient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncService @Inject constructor(
    private val patientDao: PatientDao,
    private val appointmentDao: AppointmentDao,
    private val supabaseDataSource: SupabaseDataSource
) {
    suspend fun syncAllData() {
        syncPatients()
        syncAppointments()
    }

    private suspend fun syncPatients() {
        try {
            // Get local patients
            val localPatients = patientDao.getAllPatients().first()
            
            // Get remote patients
            val remotePatients = supabaseDataSource.getRemotePatients()
            
            // Merge and update local database
            val mergedPatients = mergePatients(localPatients, remotePatients)
            mergedPatients.forEach { patient ->
                patientDao.insertPatient(patient) // Room's OnConflictStrategy.REPLACE will handle conflicts
            }
            
            // Sync back to remote
            supabaseDataSource.syncPatients(mergedPatients)
        } catch (e: Exception) {
            // Handle sync errors
            e.printStackTrace()
        }
    }

    private suspend fun syncAppointments() {
        try {
            // Get local appointments
            val localAppointments = appointmentDao.getAllAppointments().first()
            
            // Get remote appointments
            val remoteAppointments = supabaseDataSource.getRemoteAppointments()
            
            // Merge and update local database
            val mergedAppointments = mergeAppointments(localAppointments, remoteAppointments)
            mergedAppointments.forEach { appointment ->
                appointmentDao.insertAppointment(appointment) // Room's OnConflictStrategy.REPLACE will handle conflicts
            }
            
            // Sync back to remote
            supabaseDataSource.syncAppointments(mergedAppointments)
        } catch (e: Exception) {
            // Handle sync errors
            e.printStackTrace()
        }
    }

    private fun mergePatients(local: List<Patient>, remote: List<Patient>): List<Patient> {
        val merged = mutableListOf<Patient>()
        val remoteMap = remote.associateBy { it.id }
        
        // First, add all local patients
        local.forEach { localPatient ->
            val remotePatient = remoteMap[localPatient.id]
            if (remotePatient != null) {
                // If both exist, use the most recently updated version
                merged.add(if (localPatient.id > remotePatient.id) localPatient else remotePatient)
            } else {
                merged.add(localPatient)
            }
        }
        
        // Then, add any remote patients that don't exist locally
        remote.forEach { remotePatient ->
            if (!merged.any { it.id == remotePatient.id }) {
                merged.add(remotePatient)
            }
        }
        
        return merged
    }

    private fun mergeAppointments(local: List<Appointment>, remote: List<Appointment>): List<Appointment> {
        val merged = mutableListOf<Appointment>()
        val remoteMap = remote.associateBy { it.id }
        
        // First, add all local appointments
        local.forEach { localAppointment ->
            val remoteAppointment = remoteMap[localAppointment.id]
            if (remoteAppointment != null) {
                // If both exist, use the most recently updated version
                merged.add(if (localAppointment.id > remoteAppointment.id) localAppointment else remoteAppointment)
            } else {
                merged.add(localAppointment)
            }
        }
        
        // Then, add any remote appointments that don't exist locally
        remote.forEach { remoteAppointment ->
            if (!merged.any { it.id == remoteAppointment.id }) {
                merged.add(remoteAppointment)
            }
        }
        
        return merged
    }

    // Subscribe to updates
    fun subscribeToPatientUpdates(): Flow<List<Patient>> = flow {
        try {
            supabaseDataSource.subscribeToPatientChanges().collect { remotePatients ->
                val localPatients = patientDao.getAllPatients().first()
                val mergedPatients = mergePatients(localPatients, remotePatients)
                mergedPatients.forEach { patient ->
                    patientDao.insertPatient(patient) // Room's OnConflictStrategy.REPLACE will handle conflicts
                }
                emit(mergedPatients)
            }
        } catch (e: Exception) {
            // Handle subscription errors
            e.printStackTrace()
        }
    }

    fun subscribeToAppointmentUpdates(): Flow<List<Appointment>> = flow {
        try {
            supabaseDataSource.subscribeToAppointmentChanges().collect { remoteAppointments ->
                val localAppointments = appointmentDao.getAllAppointments().first()
                val mergedAppointments = mergeAppointments(localAppointments, remoteAppointments)
                mergedAppointments.forEach { appointment ->
                    appointmentDao.insertAppointment(appointment) // Room's OnConflictStrategy.REPLACE will handle conflicts
                }
                emit(mergedAppointments)
            }
        } catch (e: Exception) {
            // Handle subscription errors
            e.printStackTrace()
        }
    }
} 