package com.example.martbookingapp.di

import android.content.Context
import com.example.martbookingapp.data.local.AppDatabase
import com.example.martbookingapp.data.repository.AppointmentRepository
import com.example.martbookingapp.data.repository.PatientRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun providePatientRepository(database: AppDatabase): PatientRepository {
        return PatientRepository(database.patientDao())
    }

    @Provides
    @Singleton
    fun provideAppointmentRepository(database: AppDatabase): AppointmentRepository {
        return AppointmentRepository(database.appointmentDao())
    }
} 