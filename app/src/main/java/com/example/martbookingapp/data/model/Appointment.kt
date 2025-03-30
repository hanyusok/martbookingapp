package com.example.martbookingapp.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.martbookingapp.data.serialization.LocalDateTimeSerializer
import com.example.martbookingapp.data.serialization.TimestampSerializer
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
    @PrimaryKey
    val id: String,
    val patientId: String,
    @Serializable(with = LocalDateTimeSerializer::class)
    val dateTime: LocalDateTime,
    val status: String,
    val notes: String = "",
    @Serializable(with = TimestampSerializer::class)
    val createdAt: Long = System.currentTimeMillis(),
    @Serializable(with = TimestampSerializer::class)
    val updatedAt: Long = System.currentTimeMillis()
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