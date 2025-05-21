package com.example.focuslock.util

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import com.example.focuslock.service.VpnService
import java.util.concurrent.TimeUnit

class LockManager(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    companion object {
        private const val PREFS_NAME = "focus_lock_prefs"
        private const val KEY_LOCK_END_TIME = "lock_end_time"
        private const val KEY_EARLY_UNLOCK_REQUEST_TIME = "early_unlock_request_time"
        private const val EARLY_UNLOCK_DELAY_HOURS = 24L
    }
    
    fun activateLock(duration: Int, timeUnit: TimeUnit) {
        val durationMillis = timeUnit.toMillis(duration.toLong())
        val endTime = System.currentTimeMillis() + durationMillis
        
        prefs.edit()
            .putLong(KEY_LOCK_END_TIME, endTime)
            .remove(KEY_EARLY_UNLOCK_REQUEST_TIME)
            .apply()
    }
    
    fun isLockActive(): Boolean {
        val endTime = prefs.getLong(KEY_LOCK_END_TIME, 0)
        if (endTime == 0L) return false
        
        val earlyUnlockRequestTime = prefs.getLong(KEY_EARLY_UNLOCK_REQUEST_TIME, 0)
        if (earlyUnlockRequestTime > 0) {
            val cooldownEndTime = earlyUnlockRequestTime + TimeUnit.HOURS.toMillis(EARLY_UNLOCK_DELAY_HOURS)
            if (System.currentTimeMillis() >= cooldownEndTime) {
                // Cooldown period has passed, deactivate lock
                deactivateLock()
                return false
            }
        }
        
        return System.currentTimeMillis() < endTime
    }
    
    fun requestEarlyUnlock() {
        if (!prefs.contains(KEY_EARLY_UNLOCK_REQUEST_TIME)) {
            prefs.edit()
                .putLong(KEY_EARLY_UNLOCK_REQUEST_TIME, System.currentTimeMillis())
                .apply()
        }
    }
    
    fun deactivateLock() {
        prefs.edit()
            .remove(KEY_LOCK_END_TIME)
            .remove(KEY_EARLY_UNLOCK_REQUEST_TIME)
            .apply()
        
        // Stop the VPN service when focus mode is deactivated
        val serviceIntent = Intent(context, VpnService::class.java)
        context.stopService(serviceIntent)
    }
    
    fun getRemainingTimeFormatted(): String {
        val endTime = prefs.getLong(KEY_LOCK_END_TIME, 0)
        val earlyUnlockRequestTime = prefs.getLong(KEY_EARLY_UNLOCK_REQUEST_TIME, 0)
        
        if (earlyUnlockRequestTime > 0) {
            val cooldownEndTime = earlyUnlockRequestTime + TimeUnit.HOURS.toMillis(EARLY_UNLOCK_DELAY_HOURS)
            val remainingMillis = cooldownEndTime - System.currentTimeMillis()
            
            if (remainingMillis > 0) {
                return formatTime(remainingMillis) + " until early unlock"
            }
        }
        
        val remainingMillis = endTime - System.currentTimeMillis()
        return if (remainingMillis > 0) formatTime(remainingMillis) else "00:00:00"
    }
    
    fun getRemainingTimeMillis(): Long {
        val endTime = prefs.getLong(KEY_LOCK_END_TIME, 0)
        val earlyUnlockRequestTime = prefs.getLong(KEY_EARLY_UNLOCK_REQUEST_TIME, 0)
        
        if (earlyUnlockRequestTime > 0) {
            val cooldownEndTime = earlyUnlockRequestTime + TimeUnit.HOURS.toMillis(EARLY_UNLOCK_DELAY_HOURS)
            val remainingMillis = cooldownEndTime - System.currentTimeMillis()
            
            if (remainingMillis > 0) {
                return remainingMillis
            }
        }
        
        val remainingMillis = endTime - System.currentTimeMillis()
        return if (remainingMillis > 0) remainingMillis else 0
    }
    
    private fun formatTime(millis: Long): String {
        val seconds = (millis / 1000) % 60
        val minutes = (millis / (1000 * 60)) % 60
        val hours = (millis / (1000 * 60 * 60)) % 24
        val days = millis / (1000 * 60 * 60 * 24)
        
        return if (days > 0) {
            String.format("%d days %02d:%02d:%02d", days, hours, minutes, seconds)
        } else {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        }
    }
}
