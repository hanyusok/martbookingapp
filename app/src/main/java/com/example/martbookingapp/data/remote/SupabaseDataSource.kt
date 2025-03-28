package com.example.martbookingapp.data.remote

import com.example.martbookingapp.data.model.Appointment
import com.example.martbookingapp.data.model.Patient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseDataSource @Inject constructor(
    private val supabaseConfig: SupabaseConfig
) {
    // Patient operations
    suspend fun syncPatients(patients: List<Patient>) {
        supabaseConfig.client.postgrest["patients"]
            .insert(patients, onConflict = "REPLACE")
    }

    suspend fun getRemotePatients(): List<Patient> {
        return supabaseConfig.client.postgrest["patients"]
            .select()
            .decodeList<Patient>()
    }

    // Appointment operations
    suspend fun syncAppointments(appointments: List<Appointment>) {
        supabaseConfig.client.postgrest["appointments"]
            .insert(appointments, onConflict = "REPLACE")
    }

    suspend fun getRemoteAppointments(): List<Appointment> {
        return supabaseConfig.client.postgrest["appointments"]
            .select()
            .decodeList<Appointment>()
    }

    // Basic data flow (without realtime)
    fun subscribeToPatientChanges(): Flow<List<Patient>> = flow {
        emit(getRemotePatients())
    }

    fun subscribeToAppointmentChanges(): Flow<List<Appointment>> = flow {
        emit(getRemoteAppointments())
    }
} 