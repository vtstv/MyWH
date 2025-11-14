package com.murr.mywh.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.murr.mywh.utils.PreferencesManager

abstract class BaseActivity : AppCompatActivity() {
    protected lateinit var preferencesManager: PreferencesManager
    private var currentLanguage: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        preferencesManager = PreferencesManager(this)
        currentLanguage = preferencesManager.language
        preferencesManager.applyTheme()
        preferencesManager.applyLanguage()

        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()

        // Check if language changed
        if (currentLanguage != preferencesManager.language) {
            recreate()
        }
    }
}

