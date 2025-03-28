package com.example.martbookingapp.data.local

import androidx.room.*
import com.example.martbookingapp.data.model.Appointment
import com.example.martbookingapp.data.model.AppointmentStatus
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface AppointmentDao {
    @Query("SELECT * FROM appointments ORDER BY dateTime ASC")
    fun getAllAppointments(): Flow<List<Appointment>>

    @Query("SELECT * FROM appointments WHERE patientId = :patientId ORDER BY dateTime ASC")
    fun getAppointmentsByPatient(patientId: Long): Flow<List<Appointment>>

    @Query("SELECT * FROM appointments WHERE dateTime BETWEEN :startDate AND :endDate ORDER BY dateTime ASC")
    fun getAppointmentsByDateRange(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<Appointment>>

    @Query("SELECT * FROM appointments WHERE status = :status ORDER BY dateTime ASC")
    fun getAppointmentsByStatus(status: AppointmentStatus): Flow<List<Appointment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointment(appointment: Appointment): Long

    @Update
    suspend fun updateAppointment(appointment: Appointment)

    @Delete
    suspend fun deleteAppointment(appointment: Appointment)

    @Query("SELECT * FROM appointments WHERE id = :appointmentId")
    suspend fun getAppointmentById(appointmentId: Long): Appointment?

    @Query("SELECT * FROM appointments WHERE dateTime >= :startDate AND dateTime < :endDate AND status = :status")
    fun getAppointmentsByDateAndStatus(
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        status: AppointmentStatus
    ): Flow<List<Appointment>>
} 