package com.example.martbookingapp.data.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

object LocalDateTimeSerializer : KSerializer<LocalDateTime> {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    private val formatterWithTimeZone = DateTimeFormatter.ISO_DATE_TIME

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LocalDateTime) {
        encoder.encodeString(value.format(formatter))
    }

    override fun deserialize(decoder: Decoder): LocalDateTime {
        val dateTimeString = decoder.decodeString()
        return try {
            // First try parsing with ISO_LOCAL_DATE_TIME format
            LocalDateTime.parse(dateTimeString, formatter)
        } catch (e: DateTimeParseException) {
            try {
                // If that fails, try parsing with timezone information
                LocalDateTime.parse(dateTimeString, formatterWithTimeZone)
            } catch (e: DateTimeParseException) {
                // If both fail, throw the original exception
                throw e
            }
        }
    }
} 