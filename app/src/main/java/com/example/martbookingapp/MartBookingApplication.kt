package com.example.martbookingapp

import android.app.Application
import com.example.martbookingapp.data.local.DatabaseInitializer
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MartBookingApplication : Application() {
    @Inject
    lateinit var databaseInitializer: DatabaseInitializer

    override fun onCreate() {
        super.onCreate()
        databaseInitializer.initialize()
    }
} 