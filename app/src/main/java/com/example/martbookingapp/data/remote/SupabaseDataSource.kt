package com.example.martbookingapp.data.remote

import android.util.Log
import com.example.martbookingapp.data.model.Appointment
import com.example.martbookingapp.data.model.Patient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.broadcastFlow
import io.github.jan.supabase.realtime.createChannel
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseDataSource @Inject constructor(
    private val supabaseConfig: SupabaseConfig
) {
    companion object {
        private const val TAG = "SupabaseDataSource"
    }

    private val scope = CoroutineScope(Dispatchers.IO)

    // Patient operations
    suspend fun syncPatients(patients: List<Patient>) {
        Log.d(TAG, "Syncing ${patients.size} patients to remote")
        try {
            supabaseConfig.client.postgrest["patients"]
                .insert(
                    values = patients,
                    upsert = true,
                    onConflict = "id"
                )
            Log.d(TAG, "Successfully synced patients to remote")
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing patients to remote", e)
            throw e
        }
    }

    suspend fun getRemotePatients(): List<Patient> {
        Log.d(TAG, "Fetching patients from remote")
        try {
            return supabaseConfig.client.postgrest["patients"]
                .select()
                .decodeList<Patient>()
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching patients from remote", e)
            throw e
        }
    }

    // Appointment operations
    suspend fun syncAppointments(appointments: List<Appointment>) {
        Log.d(TAG, "Syncing ${appointments.size} appointments to remote")
        try {
            supabaseConfig.client.postgrest["appointments"]
                .insert(
                    values = appointments,
                    upsert = true,
                    onConflict = "id"
                )
            Log.d(TAG, "Successfully synced appointments to remote")
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing appointments to remote", e)
            throw e
        }
    }

    suspend fun getRemoteAppointments(): List<Appointment> {
        Log.d(TAG, "Fetching appointments from remote")
        try {
            return supabaseConfig.client.postgrest["appointments"]
                .select()
                .decodeList<Appointment>()
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching appointments from remote", e)
            throw e
        }
    }

    // Real-time subscriptions
    fun subscribeToPatientChanges(): Flow<List<Patient>> = flow {
        try {
            val channel = supabaseConfig.client.realtime.createChannel("public:patients")
            
            // Emit initial data
            emit(getRemotePatients())
            
            // Subscribe to changes
            channel.broadcastFlow<Patient>("patients update from realtime")
                .onEach {
                    Log.d(TAG, "Received patient change: ${it}")
                    emit(getRemotePatients())
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error in patient subscription", e)
            throw e
        }
    }

    fun subscribeToAppointmentChanges(): Flow<List<Appointment>> = flow {
        try {
            val channel = supabaseConfig.client.realtime.createChannel("public:appointments")
            
            // Emit initial data
            emit(getRemoteAppointments())
            
            // Subscribe to changes
            channel.broadcastFlow<Appointment>("appointments updated from realtime")
                .onEach {
                    Log.d(TAG, "Received appointment change: ${it}")
                    emit(getRemoteAppointments())
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error in appointment subscription", e)
            throw e
        }
    }

    fun getAppointmentById(appointmentId: String): Flow<Appointment?> = flow {
        try {
            val appointment = supabaseConfig.client.postgrest["appointments"]
                .select {
                    eq("id", appointmentId)
                }
                .decodeSingleOrNull<Appointment>()
            emit(appointment)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching appointment by ID: $appointmentId", e)
            emit(null)
        }
    }.flowOn(Dispatchers.IO)
} 