package com.example.martbookingapp.di

import com.example.martbookingapp.data.local.AppDatabase
import com.example.martbookingapp.data.remote.SupabaseConfig
import com.example.martbookingapp.data.remote.SupabaseDataSource
import com.example.martbookingapp.data.repository.AppointmentRepository
import com.example.martbookingapp.data.repository.PatientRepository
import com.example.martbookingapp.data.sync.SyncService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

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

    @Provides
    @Singleton
    fun provideSupabaseConfig(): SupabaseConfig {
        return SupabaseConfig()
    }

    @Provides
    @Singleton
    fun provideSupabaseDataSource(supabaseConfig: SupabaseConfig): SupabaseDataSource {
        return SupabaseDataSource(supabaseConfig)
    }

    @Provides
    @Singleton
    fun provideSyncService(
        patientRepository: PatientRepository,
        appointmentRepository: AppointmentRepository,
        supabaseDataSource: SupabaseDataSource
    ): SyncService {
        return SyncService(
            patientRepository.getPatientDao(),
            appointmentRepository.getAppointmentDao(),
            supabaseDataSource
        )
    }
} 