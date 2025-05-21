package com.example.focuslock.settings

import android.content.Context
import android.content.SharedPreferences
import java.security.MessageDigest
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

class SettingsLockManager(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    companion object {
        private const val PREFS_NAME = "focus_lock_settings_lock_prefs"
        private const val KEY_SETTINGS_PASSWORD = "settings_password"
        private const val KEY_SETTINGS_LOCKED = "settings_locked"
        private const val DEFAULT_PASSWORD = "focuslock"
        private const val ENCRYPTION_KEY = "FocusLockSecureKey123456789012345"
    }
    
    fun isSettingsLocked(): Boolean {
        return prefs.getBoolean(KEY_SETTINGS_LOCKED, false)
    }
    
    fun setSettingsLocked(locked: Boolean) {
        prefs.edit().putBoolean(KEY_SETTINGS_LOCKED, locked).apply()
    }
    
    fun getSettingsPassword(): String {
        val encryptedPassword = prefs.getString(KEY_SETTINGS_PASSWORD, null)
        return if (encryptedPassword != null) {
            try {
                decrypt(encryptedPassword)
            } catch (e: Exception) {
                DEFAULT_PASSWORD
            }
        } else {
            DEFAULT_PASSWORD
        }
    }
    
    fun setSettingsPassword(password: String) {
        if (password.isNotBlank()) {
            try {
                val encryptedPassword = encrypt(password)
                prefs.edit().putString(KEY_SETTINGS_PASSWORD, encryptedPassword).apply()
            } catch (e: Exception) {
                // If encryption fails, store the default password
                prefs.edit().putString(KEY_SETTINGS_PASSWORD, DEFAULT_PASSWORD).apply()
            }
        }
    }
    
    fun verifyPassword(inputPassword: String): Boolean {
        val storedPassword = getSettingsPassword()
        return inputPassword == storedPassword
    }
    
    private fun encrypt(input: String): String {
        val key = generateKey()
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val encryptedBytes = cipher.doFinal(input.toByteArray())
        return Base64.getEncoder().encodeToString(encryptedBytes)
    }
    
    private fun decrypt(input: String): String {
        val key = generateKey()
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.DECRYPT_MODE, key)
        val decodedBytes = Base64.getDecoder().decode(input)
        val decryptedBytes = cipher.doFinal(decodedBytes)
        return String(decryptedBytes)
    }
    
    private fun generateKey(): SecretKeySpec {
        val sha = MessageDigest.getInstance("SHA-256")
        var key = ENCRYPTION_KEY.toByteArray()
        key = sha.digest(key)
        return SecretKeySpec(key, "AES")
    }
}
