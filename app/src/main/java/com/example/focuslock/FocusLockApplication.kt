package com.example.focuslock

import android.app.Application
import androidx.multidex.MultiDexApplication

class FocusLockApplication : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        // Initialize any app-wide components here
    }
}
