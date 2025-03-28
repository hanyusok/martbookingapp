package com.example.martbookingapp.data.local

import com.example.martbookingapp.data.model.Appointment
import com.example.martbookingapp.data.model.AppointmentStatus
import com.example.martbookingapp.data.model.AppointmentType
import com.example.martbookingapp.data.model.Patient
import com.example.martbookingapp.data.repository.AppointmentRepository
import com.example.martbookingapp.data.repository.PatientRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

class DatabaseInitializer @Inject constructor(
    private val patientRepository: PatientRepository,
    private val appointmentRepository: AppointmentRepository
) {
    fun initialize() {
        CoroutineScope(Dispatchers.IO).launch {
            // Add sample patients
            val patients = listOf(
                Patient(
                    name = "John Doe",
                    email = "john.doe@email.com",
                    phone = "123-456-7890",
                    dateOfBirth = "1980-05-15",
                    address = "123 Main St, City",
                    medicalHistory = "Hypertension, Allergic to penicillin"
                ),
                Patient(
                    name = "Jane Smith",
                    email = "jane.smith@email.com",
                    phone = "098-765-4321",
                    dateOfBirth = "1990-08-22",
                    address = "456 Oak Ave, Town",
                    medicalHistory = "Asthma"
                ),
                Patient(
                    name = "Mike Johnson",
                    email = "mike.j@email.com",
                    phone = "555-123-4567",
                    dateOfBirth = "1975-12-10",
                    address = "789 Pine Rd, Village",
                    medicalHistory = "Diabetes Type 2"
                )
            )

            val patientIds = patients.map { patient ->
                patientRepository.insertPatient(patient)
            }

            // Add sample appointments
            val now = LocalDateTime.now()
            val appointments = listOf(
                Appointment(
                    patientId = patientIds[0],
                    dateTime = now.plusDays(1).withHour(9).withMinute(0),
                    status = AppointmentStatus.SCHEDULED,
                    type = AppointmentType.GENERAL_CHECKUP,
                    notes = "Regular checkup"
                ),
                Appointment(
                    patientId = patientIds[1],
                    dateTime = now.plusDays(2).withHour(10).withMinute(30),
                    status = AppointmentStatus.SCHEDULED,
                    type = AppointmentType.FOLLOW_UP,
                    notes = "Follow-up for asthma treatment"
                ),
                Appointment(
                    patientId = patientIds[2],
                    dateTime = now.plusDays(3).withHour(14).withMinute(0),
                    status = AppointmentStatus.SCHEDULED,
                    type = AppointmentType.SPECIALIST_REFERRAL,
                    notes = "Referral to cardiologist"
                ),
                Appointment(
                    patientId = patientIds[0],
                    dateTime = now.minusDays(1).withHour(11).withMinute(0),
                    status = AppointmentStatus.COMPLETED,
                    type = AppointmentType.VACCINATION,
                    notes = "Annual flu shot"
                )
            )

            appointments.forEach { appointment ->
                appointmentRepository.insertAppointment(appointment)
            }
        }
    }
} 