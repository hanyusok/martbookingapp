package com.example.martbookingapp.data.local

import androidx.room.*
import com.example.martbookingapp.data.model.Appointment
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface AppointmentDao {
    @Query("SELECT * FROM appointments ORDER BY dateTime ASC")
    fun getAllAppointments(): Flow<List<Appointment>>

    @Query("SELECT * FROM appointments WHERE patientId = :patientId ORDER BY dateTime ASC")
    fun getAppointmentsByPatient(patientId: String): Flow<List<Appointment>>

    @Query("SELECT * FROM appointments WHERE dateTime BETWEEN :startDate AND :endDate ORDER BY dateTime ASC")
    fun getAppointmentsByDateRange(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<Appointment>>

    @Query("SELECT * FROM appointments WHERE status = :status ORDER BY dateTime ASC")
    fun getAppointmentsByStatus(status: String): Flow<List<Appointment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointment(appointment: Appointment)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointments(appointments: List<Appointment>)

    @Update
    suspend fun updateAppointment(appointment: Appointment)

    @Delete
    suspend fun deleteAppointment(appointment: Appointment)

    @Query("SELECT * FROM appointments WHERE id = :appointmentId")
    suspend fun getAppointmentById(appointmentId: String): Appointment?

    @Query("SELECT * FROM appointments WHERE dateTime >= :startDate AND dateTime < :endDate AND status = :status")
    fun getAppointmentsByDateAndStatus(
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        status: String
    ): Flow<List<Appointment>>
} 