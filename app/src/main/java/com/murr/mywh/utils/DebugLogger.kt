package com.murr.mywh.utils

import android.content.Context
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object DebugLogger {
    private const val TAG = "MyWH_Debug"
    private var isEnabled = false
    private val logs = mutableListOf<String>()
    private const val MAX_LOGS = 1000
    
    fun init(context: Context) {
        val prefs = PreferencesManager(context)
        isEnabled = prefs.isDebugMode
        // Test that force logging works
        log("DebugLogger initialized, isEnabled=$isEnabled", force = true)
    }
    
    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
        if (enabled) {
            // When enabling, add a marker log
            val timestamp = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date())
            logs.add("[$timestamp] Debug mode enabled")
        } else {
            logs.clear()
        }
    }
    
    fun log(message: String, throwable: Throwable? = null, force: Boolean = false) {
        val timestamp = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date())
        val logMessage = "[$timestamp] $message"
        
        // Always log to Logcat for debugging
        if (throwable != null) {
            Log.e(TAG, logMessage, throwable)
        } else {
            Log.d(TAG, logMessage)
        }
        
        // Debug: log why we're storing or not
        if (force && !isEnabled) {
            Log.d(TAG, "Force logging (isEnabled=$isEnabled): $message")
        }
        
        // Store in memory if enabled or forced (for critical operations)
        if (isEnabled || force) {
            if (throwable != null) {
                val stackTrace = throwable.stackTraceToString()
                logs.add("$logMessage\nError: ${throwable.message}\n$stackTrace")
            } else {
                logs.add(logMessage)
            }
            
            // Keep only last MAX_LOGS entries
            if (logs.size > MAX_LOGS) {
                logs.removeAt(0)
            }
        }
    }
    
    fun getLogs(): List<String> = logs.toList()
    
    fun getStatus(): String {
        return "DebugLogger: isEnabled=$isEnabled, logs.size=${logs.size}"
    }
    
    fun clearLogs() {
        logs.clear()
    }
    
    fun exportLogs(context: Context): File {
        val logsDir = File(context.cacheDir, "logs")
        logsDir.mkdirs()
        
        val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(Date())
        val logFile = File(logsDir, "mywh_log_$timestamp.txt")
        
        logFile.writeText(logs.joinToString("\n"))
        return logFile
    }
}
