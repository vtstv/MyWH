package com.murr.mywh.utils

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import java.util.Locale

class PreferencesManager(private val context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "mywh_preferences"
        private const val KEY_THEME = "theme"
        private const val KEY_LANGUAGE = "language"

        const val THEME_LIGHT = "light"
        const val THEME_DARK = "dark"
        const val THEME_SYSTEM = "system"

        const val LANG_EN = "en"
        const val LANG_RU = "ru"
        const val LANG_DE = "de"
    }

    var theme: String
        get() = sharedPreferences.getString(KEY_THEME, THEME_SYSTEM) ?: THEME_SYSTEM
        set(value) {
            sharedPreferences.edit().putString(KEY_THEME, value).apply()
            applyTheme(value)
        }

    var language: String
        get() = sharedPreferences.getString(KEY_LANGUAGE, LANG_EN) ?: LANG_EN
        set(value) {
            sharedPreferences.edit().putString(KEY_LANGUAGE, value).apply()
            applyLanguage(value)
        }

    fun applyTheme(theme: String = this.theme) {
        when (theme) {
            THEME_LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            THEME_DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            THEME_SYSTEM -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    fun applyLanguage(language: String = this.language) {
        val locale = when (language) {
            LANG_RU -> Locale("ru", "RU")
            LANG_DE -> Locale("de", "DE")
            else -> Locale("en", "US")
        }
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        @Suppress("DEPRECATION")
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }
}
