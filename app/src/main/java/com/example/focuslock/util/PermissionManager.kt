package com.example.focuslock.util

import android.content.Context
import android.provider.Settings
import com.example.focuslock.accessibility.AppBlockerService

class PermissionManager(private val context: Context) {
    
    fun isAccessibilityServiceEnabled(): Boolean {
        val accessibilityEnabled = try {
            Settings.Secure.getInt(
                context.contentResolver,
                Settings.Secure.ACCESSIBILITY_ENABLED
            )
        } catch (e: Settings.SettingNotFoundException) {
            0
        }
        
        if (accessibilityEnabled == 1) {
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false
            
            return enabledServices.contains(context.packageName + "/" + AppBlockerService::class.java.name)
        }
        
        return false
    }
}
