package com.example.martbookingapp.data.repository

import com.example.martbookingapp.data.local.AppointmentDao
import com.example.martbookingapp.data.model.Appointment
import com.example.martbookingapp.data.model.AppointmentStatus
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import javax.inject.Inject

class AppointmentRepository @Inject constructor(
    private val appointmentDao: AppointmentDao
) {
    fun getAllAppointments(): Flow<List<Appointment>> = appointmentDao.getAllAppointments()

    fun getAppointmentsByPatient(patientId: String): Flow<List<Appointment>> =
        appointmentDao.getAppointmentsByPatient(patientId)

    fun getAppointmentsByDateRange(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<Appointment>> =
        appointmentDao.getAppointmentsByDateRange(startDate, endDate)

    fun getAppointmentsByStatus(status: String): Flow<List<Appointment>> =
        appointmentDao.getAppointmentsByStatus(status)

    suspend fun insertAppointment(appointment: Appointment) =
        appointmentDao.insertAppointment(appointment)

    suspend fun updateAppointment(appointment: Appointment) =
        appointmentDao.updateAppointment(appointment)

    suspend fun deleteAppointment(appointment: Appointment) =
        appointmentDao.deleteAppointment(appointment)

    suspend fun getAppointmentById(appointmentId: String): Appointment? =
        appointmentDao.getAppointmentById(appointmentId)

    fun getAppointmentsByDateAndStatus(
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        status: String
    ): Flow<List<Appointment>> =
        appointmentDao.getAppointmentsByDateAndStatus(startDate, endDate, status)

    fun getAppointmentDao(): AppointmentDao = appointmentDao
} 