package com.murr.mywh

import android.app.Application
import android.content.res.Configuration
import com.murr.mywh.utils.PreferencesManager
import java.util.Locale

class MyWHApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        applyLanguage()
    }
    
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        applyLanguage()
    }
    
    private fun applyLanguage() {
        val preferencesManager = PreferencesManager(this)
        val language = preferencesManager.language
        
        val locale = when (language) {
            PreferencesManager.LANG_RU -> Locale("ru", "RU")
            PreferencesManager.LANG_DE -> Locale("de", "DE")
            else -> Locale("en", "US")
        }
        
        Locale.setDefault(locale)
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        
        @Suppress("DEPRECATION")
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}

