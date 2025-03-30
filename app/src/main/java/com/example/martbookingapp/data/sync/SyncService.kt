package com.example.martbookingapp.data.sync

import android.util.Log
import com.example.martbookingapp.data.local.AppointmentDao
import com.example.martbookingapp.data.local.PatientDao
import com.example.martbookingapp.data.remote.SupabaseDataSource
import com.example.martbookingapp.data.model.Appointment
import com.example.martbookingapp.data.model.Patient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncService @Inject constructor(
    private val patientDao: PatientDao,
    private val appointmentDao: AppointmentDao,
    private val supabaseDataSource: SupabaseDataSource
) {
    companion object {
        private const val TAG = "SyncService"
        private const val BATCH_SIZE = 50
    }

    private val syncScope = CoroutineScope(Dispatchers.IO)

    suspend fun syncAllData() {
        Log.d(TAG, "Starting full data sync")
        try {
            // Launch both sync operations in parallel
            val patientsJob = syncScope.async { syncPatients() }
            val appointmentsJob = syncScope.async { syncAppointments() }
            
            // Wait for both operations to complete
            awaitAll(patientsJob, appointmentsJob)
            Log.d(TAG, "Full data sync completed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error during full data sync", e)
            throw e
        }
    }

    private suspend fun syncPatients() {
        Log.d(TAG, "Starting patient sync")
        try {
            // Get local and remote patients in parallel
            val localPatientsDeferred = syncScope.async { patientDao.getAllPatients().first() }
            val remotePatientsDeferred = syncScope.async { supabaseDataSource.getRemotePatients() }
            
            val localPatients = localPatientsDeferred.await()
            val remotePatients = remotePatientsDeferred.await()
            
            Log.d(TAG, "Found ${localPatients.size} local and ${remotePatients.size} remote patients")
            
            // Merge patients
            val mergedPatients = mergePatients(localPatients, remotePatients)
            
            // Update local database in batches
            mergedPatients.chunked(BATCH_SIZE).forEach { batch ->
                patientDao.insertPatients(batch)
            }
            
            // Sync back to remote in batches
            mergedPatients.chunked(BATCH_SIZE).forEach { batch ->
                supabaseDataSource.syncPatients(batch)
            }
            
            Log.d(TAG, "Patient sync completed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error during patient sync", e)
            throw e
        }
    }

    private suspend fun syncAppointments() {
        Log.d(TAG, "Starting appointment sync")
        try {
            // Get local and remote appointments in parallel
            val localAppointmentsDeferred = syncScope.async { appointmentDao.getAllAppointments().first() }
            val remoteAppointmentsDeferred = syncScope.async { supabaseDataSource.getRemoteAppointments() }
            
            val localAppointments = localAppointmentsDeferred.await()
            val remoteAppointments = remoteAppointmentsDeferred.await()
            
            Log.d(TAG, "Found ${localAppointments.size} local and ${remoteAppointments.size} remote appointments")
            
            // Merge appointments
            val mergedAppointments = mergeAppointments(localAppointments, remoteAppointments)
            
            // Update local database in batches
            mergedAppointments.chunked(BATCH_SIZE).forEach { batch ->
                appointmentDao.insertAppointments(batch)
            }
            
            // Sync back to remote in batches
            mergedAppointments.chunked(BATCH_SIZE).forEach { batch ->
                supabaseDataSource.syncAppointments(batch)
            }
            
            Log.d(TAG, "Appointment sync completed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error during appointment sync", e)
            throw e
        }
    }

    private fun mergePatients(local: List<Patient>, remote: List<Patient>): List<Patient> {
        val merged = mutableMapOf<String, Patient>()
        local.forEach { merged[it.id] = it }
        remote.forEach { merged[it.id] = it }
        return merged.values.toList()
    }

    private fun mergeAppointments(local: List<Appointment>, remote: List<Appointment>): List<Appointment> {
        val merged = mutableMapOf<String, Appointment>()
        local.forEach { merged[it.id] = it }
        remote.forEach { merged[it.id] = it }
        return merged.values.toList()
    }

    // Subscribe to updates
    fun subscribeToPatientUpdates(): Flow<List<Patient>> = flow {
        try {
            supabaseDataSource.subscribeToPatientChanges().collect { remotePatients ->
                val localPatients = patientDao.getAllPatients().first()
                val mergedPatients = mergePatients(localPatients, remotePatients)
                
                // Update in batches
                mergedPatients.chunked(BATCH_SIZE).forEach { batch ->
                    patientDao.insertPatients(batch)
                }
                
                emit(mergedPatients)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in patient subscription", e)
            throw e
        }
    }

    fun subscribeToAppointmentUpdates(): Flow<List<Appointment>> = flow {
        try {
            supabaseDataSource.subscribeToAppointmentChanges().collect { remoteAppointments ->
                val localAppointments = appointmentDao.getAllAppointments().first()
                val mergedAppointments = mergeAppointments(localAppointments, remoteAppointments)
                
                // Update in batches
                mergedAppointments.chunked(BATCH_SIZE).forEach { batch ->
                    appointmentDao.insertAppointments(batch)
                }
                
                emit(mergedAppointments)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in appointment subscription", e)
            throw e
        }
    }
} 