package com.example.martbookingapp

import android.app.Application
import com.example.martbookingapp.data.sync.SyncService
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class MartBookingApplication : Application() {
    @Inject
    lateinit var syncService: SyncService

    override fun onCreate() {
        super.onCreate()
        // Database initialization is now handled by Room's DatabaseCallback
        // No need for explicit initialization
        
        // Initialize sync service
        CoroutineScope(Dispatchers.IO).launch {
            syncService.syncAllData()
        }
    }
} 