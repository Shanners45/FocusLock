package com.example.focuslock.util

import android.content.Context
import android.content.SharedPreferences
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class StreakManager(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    
    companion object {
        private const val PREFS_NAME = "focus_lock_streak_prefs"
        private const val KEY_STREAK_DATA_PREFIX = "streak_data_"
        private const val KEY_CURRENT_STREAK = "current_streak"
        private const val KEY_STREAK_DATES = "streak_dates"
    }
    
    fun saveStreakData(streakData: Map<LocalDate, Boolean>) {
        val editor = prefs.edit()
        
        // Clear previous data
        val allKeys = prefs.all.keys.filter { it.startsWith(KEY_STREAK_DATA_PREFIX) }
        for (key in allKeys) {
            editor.remove(key)
        }
        
        // Save new data
        val datesList = mutableListOf<String>()
        
        for ((date, isSuccess) in streakData) {
            val dateStr = date.format(dateFormatter)
            val key = KEY_STREAK_DATA_PREFIX + dateStr
            editor.putBoolean(key, isSuccess)
            datesList.add(dateStr)
        }
        
        // Save the list of dates
        editor.putString(KEY_STREAK_DATES, datesList.joinToString(","))
        
        editor.apply()
    }
    
    fun getStreakData(): Map<LocalDate, Boolean> {
        val result = mutableMapOf<LocalDate, Boolean>()
        
        val datesStr = prefs.getString(KEY_STREAK_DATES, "") ?: ""
        if (datesStr.isNotEmpty()) {
            val dates = datesStr.split(",")
            
            for (dateStr in dates) {
                val key = KEY_STREAK_DATA_PREFIX + dateStr
                val isSuccess = prefs.getBoolean(key, false)
                val date = LocalDate.parse(dateStr, dateFormatter)
                result[date] = isSuccess
            }
        }
        
        return result
    }
    
    fun saveCurrentStreak(streak: Int) {
        prefs.edit().putInt(KEY_CURRENT_STREAK, streak).apply()
    }
    
    fun getCurrentStreak(): Int {
        return prefs.getInt(KEY_CURRENT_STREAK, 0)
    }
    
    fun calculateCurrentStreak(): Int {
        val streakData = getStreakData()
        var currentDate = LocalDate.now()
        var streak = 0
        
        // Count consecutive successful days
        while (streakData[currentDate] == true) {
            streak++
            currentDate = currentDate.minusDays(1)
        }
        
        return streak
    }
    
    fun incrementStreak() {
        val currentStreak = getCurrentStreak()
        saveCurrentStreak(currentStreak + 1)
    }
    
    fun resetStreak() {
        saveCurrentStreak(0)
    }
}
