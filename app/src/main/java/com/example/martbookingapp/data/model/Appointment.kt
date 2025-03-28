package com.example.martbookingapp.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.martbookingapp.data.serialization.LocalDateTimeSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
@Entity(
    tableName = "appointments",
    foreignKeys = [
        ForeignKey(
            entity = Patient::class,
            parentColumns = ["id"],
            childColumns = ["patientId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("patientId")
    ]
)
data class Appointment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val patientId: Long,
    @Serializable(with = LocalDateTimeSerializer::class)
    val dateTime: LocalDateTime,
    val status: AppointmentStatus,
    val notes: String = "",
    val type: AppointmentType
)

@Serializable
enum class AppointmentStatus {
    SCHEDULED,
    COMPLETED,
    CANCELLED,
    NO_SHOW
}

@Serializable
enum class AppointmentType {
    GENERAL_CHECKUP,
    FOLLOW_UP,
    EMERGENCY,
    SPECIALIST_REFERRAL,
    VACCINATION,
    OTHER
} 