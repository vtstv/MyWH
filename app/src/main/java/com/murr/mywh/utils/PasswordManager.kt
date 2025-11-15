package com.murr.mywh.utils

import android.content.Context
import android.content.SharedPreferences
import java.security.MessageDigest

class PasswordManager(private val context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "mywh_security"
        private const val KEY_PASSWORD_HASH = "password_hash"
        private const val KEY_PASSWORD_ENABLED = "password_enabled"
        private const val KEY_REASK_INTERVAL = "reask_interval"
        private const val KEY_LAST_UNLOCK_TIME = "last_unlock_time"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"

        const val INTERVAL_HOUR = 3600000L // 1 hour in milliseconds
        const val INTERVAL_DAY = 86400000L // 1 day in milliseconds
        const val INTERVAL_WEEK = 604800000L // 1 week in milliseconds
    }

    var isPasswordEnabled: Boolean
        get() = sharedPreferences.getBoolean(KEY_PASSWORD_ENABLED, false)
        set(value) {
            sharedPreferences.edit().putBoolean(KEY_PASSWORD_ENABLED, value).apply()
        }

    var reaskInterval: Long
        get() = sharedPreferences.getLong(KEY_REASK_INTERVAL, INTERVAL_HOUR)
        set(value) {
            sharedPreferences.edit().putLong(KEY_REASK_INTERVAL, value).apply()
        }

    var isBiometricEnabled: Boolean
        get() = sharedPreferences.getBoolean(KEY_BIOMETRIC_ENABLED, false)
        set(value) {
            sharedPreferences.edit().putBoolean(KEY_BIOMETRIC_ENABLED, value).apply()
        }

    private var lastUnlockTime: Long
        get() = sharedPreferences.getLong(KEY_LAST_UNLOCK_TIME, 0)
        set(value) {
            sharedPreferences.edit().putLong(KEY_LAST_UNLOCK_TIME, value).apply()
        }

    fun setPassword(password: String) {
        val hash = hashPassword(password)
        sharedPreferences.edit().putString(KEY_PASSWORD_HASH, hash).apply()
        isPasswordEnabled = true
    }

    fun verifyPassword(password: String): Boolean {
        val hash = hashPassword(password)
        val storedHash = sharedPreferences.getString(KEY_PASSWORD_HASH, null)
        return hash == storedHash
    }

    fun removePassword() {
        sharedPreferences.edit()
            .remove(KEY_PASSWORD_HASH)
            .putBoolean(KEY_PASSWORD_ENABLED, false)
            .apply()
    }

    fun hasPassword(): Boolean {
        return sharedPreferences.contains(KEY_PASSWORD_HASH) && isPasswordEnabled
    }

    fun shouldAskPassword(): Boolean {
        if (!hasPassword()) return false

        val currentTime = System.currentTimeMillis()
        val timeSinceLastUnlock = currentTime - lastUnlockTime

        return timeSinceLastUnlock > reaskInterval
    }

    fun markAsUnlocked() {
        lastUnlockTime = System.currentTimeMillis()
    }

    fun resetUnlockTime() {
        lastUnlockTime = 0
    }

    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(password.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }

    fun getReaskIntervalLabel(): String {
        return when (reaskInterval) {
            INTERVAL_HOUR -> "1 Hour"
            INTERVAL_DAY -> "1 Day"
            INTERVAL_WEEK -> "1 Week"
            else -> "1 Hour"
        }
    }
}

