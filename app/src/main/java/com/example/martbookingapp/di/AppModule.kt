package com.example.martbookingapp.di

import android.content.Context
import com.example.martbookingapp.data.local.AppDatabase
import com.example.martbookingapp.data.local.AppointmentDao
import com.example.martbookingapp.data.local.PatientDao
import com.example.martbookingapp.data.remote.SupabaseConfig
import com.example.martbookingapp.data.remote.SupabaseDataSource
import com.example.martbookingapp.data.repository.AppointmentRepository
import com.example.martbookingapp.data.repository.PatientRepository
import com.example.martbookingapp.data.sync.SyncService
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
    fun providePatientDao(database: AppDatabase): PatientDao {
        return database.patientDao()
    }

    @Provides
    @Singleton
    fun provideAppointmentDao(database: AppDatabase): AppointmentDao {
        return database.appointmentDao()
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
        patientDao: PatientDao,
        appointmentDao: AppointmentDao,
        supabaseDataSource: SupabaseDataSource
    ): SyncService {
        return SyncService(patientDao, appointmentDao, supabaseDataSource)
    }
} 