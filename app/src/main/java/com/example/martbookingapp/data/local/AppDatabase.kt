package com.example.martbookingapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.martbookingapp.data.model.Appointment
import com.example.martbookingapp.data.model.Patient

@Database(
    entities = [Patient::class, Appointment::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun patientDao(): PatientDao
    abstract fun appointmentDao(): AppointmentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create index on patientId column
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_appointments_patientId ON appointments(patientId)"
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mart_booking_database"
                )
                .addMigrations(MIGRATION_1_2)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class Converters {
    @androidx.room.TypeConverter
    fun fromTimestamp(value: Long?): java.time.LocalDateTime? {
        return value?.let { java.time.LocalDateTime.ofEpochSecond(it, 0, java.time.ZoneOffset.UTC) }
    }

    @androidx.room.TypeConverter
    fun dateToTimestamp(date: java.time.LocalDateTime?): Long? {
        return date?.toEpochSecond(java.time.ZoneOffset.UTC)
    }
} 