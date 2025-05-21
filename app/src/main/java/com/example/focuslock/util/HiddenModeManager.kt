package com.example.focuslock.util

import android.content.Context
import android.content.SharedPreferences
import android.content.ComponentName
import android.content.pm.PackageManager
import com.example.focuslock.MainActivity
import com.example.focuslock.settings.SettingsActivity

class HiddenModeManager(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    companion object {
        private const val PREFS_NAME = "focus_lock_hidden_mode_prefs"
        private const val KEY_HIDDEN_MODE_ENABLED = "hidden_mode_enabled"
        private const val KEY_UNLOCK_CODE = "unlock_code"
        private const val DEFAULT_UNLOCK_CODE = "123456"
    }
    
    fun isHiddenModeEnabled(): Boolean {
        return prefs.getBoolean(KEY_HIDDEN_MODE_ENABLED, false)
    }
    
    fun setHiddenModeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HIDDEN_MODE_ENABLED, enabled).apply()
    }
    
    fun getUnlockCode(): String {
        return prefs.getString(KEY_UNLOCK_CODE, DEFAULT_UNLOCK_CODE) ?: DEFAULT_UNLOCK_CODE
    }
    
    fun setUnlockCode(code: String) {
        if (code.isNotBlank()) {
            prefs.edit().putString(KEY_UNLOCK_CODE, code).apply()
        }
    }

    fun hideApp(context: Context) {
        // Hide the main launcher activity
        val packageManager = context.packageManager
        val componentName = ComponentName(context, MainActivity::class.java)
        
        packageManager.setComponentEnabledSetting(
            componentName,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
        
        // Also hide the settings activity to prevent direct access
        val settingsComponentName = ComponentName(context, SettingsActivity::class.java)
        packageManager.setComponentEnabledSetting(
            settingsComponentName,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
        
        setHiddenModeEnabled(true)
    }

    fun showApp(context: Context) {
        // Show the main launcher activity
        val packageManager = context.packageManager
        val componentName = ComponentName(context, MainActivity::class.java)
        
        packageManager.setComponentEnabledSetting(
            componentName,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
        
        // Also show the settings activity
        val settingsComponentName = ComponentName(context, SettingsActivity::class.java)
        packageManager.setComponentEnabledSetting(
            settingsComponentName,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
        
        setHiddenModeEnabled(false)
    }
}
