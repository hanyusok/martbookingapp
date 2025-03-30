package com.example.martbookingapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.martbookingapp.data.serialization.LocalDateSerializer
import com.example.martbookingapp.data.serialization.TimestampSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
@Entity(tableName = "patients")
data class Patient(
    @PrimaryKey
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    @Serializable(with = LocalDateSerializer::class)
    @androidx.room.ColumnInfo(name = "dateOfBirth")
    val dateOfBirth: LocalDate,
    val address: String,
    val medicalHistory: String = "",
    @Serializable(with = TimestampSerializer::class)
    val createdAt: Long = System.currentTimeMillis(),
    @Serializable(with = TimestampSerializer::class)
    val updatedAt: Long = System.currentTimeMillis()
) 