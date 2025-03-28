package com.example.martbookingapp

import android.app.Application
import com.example.martbookingapp.data.local.DatabaseInitializer
import com.example.martbookingapp.data.sync.SyncService
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class MartBookingApplication : Application() {
    @Inject
    lateinit var databaseInitializer: DatabaseInitializer

    @Inject
    lateinit var syncService: SyncService

    override fun onCreate() {
        super.onCreate()
        databaseInitializer.initialize()
        
        // Initialize sync service
        CoroutineScope(Dispatchers.IO).launch {
            syncService.syncAllData()
        }
    }
} 