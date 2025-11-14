package com.murr.mywh.activities

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.google.android.material.navigation.NavigationView
import com.murr.mywh.R
import com.murr.mywh.databinding.ActivitySettingsBinding
import com.murr.mywh.utils.PreferencesManager
import com.murr.mywh.viewmodels.SettingsViewModel

class SettingsActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding: ActivitySettingsBinding
    private val viewModel: SettingsViewModel by viewModels()
    private lateinit var preferencesManager: PreferencesManager

    private val importFilePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            viewModel.importDataFromUri(this, it)
        }
    }

    private val exportFilePicker = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri: Uri? ->
        uri?.let {
            viewModel.exportDataToUri(this, it)
        }
    }

    private val mySQLDumpPicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            viewModel.importFromMySQLDump(this, it)
        }
    }

    companion object {
        fun newIntent(context: Context) = Intent(context, SettingsActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        preferencesManager = PreferencesManager(this)

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupNavigationDrawer()
        setupListeners()
        setupBottomNavigation()
        updateThemeText()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu)
        supportActionBar?.title = getString(R.string.settings)
    }

    private fun setupNavigationDrawer() {
        val toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, binding.toolbar,
            R.string.nav_home, R.string.nav_home
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.navigationView.setNavigationItemSelectedListener(this)
    }

    //...existing code...

    private fun setupListeners() {
        binding.btnTheme.setOnClickListener {
            showThemeDialog()
        }

        binding.btnLanguage.setOnClickListener {
            showLanguageDialog()
        }

        binding.btnImportData.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(R.string.import_data)
                .setMessage(R.string.import_warning_message)
                .setPositiveButton(R.string.import_continue) { _, _ ->
                    importFilePicker.launch("application/json")
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }

        binding.btnExportData.setOnClickListener {
            val fileName = "mywh_export_${System.currentTimeMillis()}.json"
            exportFilePicker.launch(fileName)
        }

        binding.btnImportMySQLDump.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(R.string.import_mysql_dump)
                .setMessage(R.string.import_warning_message)
                .setPositiveButton(R.string.import_continue) { _, _ ->
                    mySQLDumpPicker.launch("*/*") // Accept all file types
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }
    }

    private fun showThemeDialog() {
        val themes = arrayOf(
            getString(R.string.theme_light),
            getString(R.string.theme_dark),
            getString(R.string.theme_system)
        )

        val currentTheme = when(preferencesManager.theme) {
            PreferencesManager.THEME_LIGHT -> 0
            PreferencesManager.THEME_DARK -> 1
            else -> 2
        }

        AlertDialog.Builder(this)
            .setTitle(R.string.theme)
            .setSingleChoiceItems(themes, currentTheme) { dialog, which ->
                val newTheme = when(which) {
                    0 -> PreferencesManager.THEME_LIGHT
                    1 -> PreferencesManager.THEME_DARK
                    else -> PreferencesManager.THEME_SYSTEM
                }
                preferencesManager.theme = newTheme
                dialog.dismiss()
                updateThemeText()
            }
            .show()
    }

    private fun showLanguageDialog() {
        val languages = arrayOf("English", "Русский", "Deutsch")
        val currentLang = when (preferencesManager.language) {
            PreferencesManager.LANG_RU -> 1
            PreferencesManager.LANG_DE -> 2
            else -> 0
        }

        AlertDialog.Builder(this)
            .setTitle(R.string.language)
            .setSingleChoiceItems(languages, currentLang) { dialog, which ->
                val newLang = when (which) {
                    1 -> PreferencesManager.LANG_RU
                    2 -> PreferencesManager.LANG_DE
                    else -> PreferencesManager.LANG_EN
                }
                if (newLang != preferencesManager.language) {
                    preferencesManager.language = newLang
                    dialog.dismiss()

                    // Show restart message
                    AlertDialog.Builder(this)
                        .setTitle(R.string.language)
                        .setMessage(R.string.language_changed_restart)
                        .setPositiveButton(R.string.ok) { _, _ ->
                            // Restart the app
                            val intent = Intent(this, MainActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                            finishAffinity()
                        }
                        .setCancelable(false)
                        .show()
                }
            }
            .show()
    }

    private fun updateThemeText() {
        val themeText = when(preferencesManager.theme) {
            PreferencesManager.THEME_LIGHT -> getString(R.string.theme_light)
            PreferencesManager.THEME_DARK -> getString(R.string.theme_dark)
            else -> getString(R.string.theme_system)
        }
        binding.tvCurrentTheme.text = themeText
    }

    private fun setupBottomNavigation() {
        // Don't highlight any tab for Settings screen
        binding.bottomNavigation.menu.setGroupCheckable(0, true, false)
        for (i in 0 until binding.bottomNavigation.menu.size()) {
            binding.bottomNavigation.menu.getItem(i).isChecked = false
        }
        binding.bottomNavigation.menu.setGroupCheckable(0, true, true)

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.bottom_nav_home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.bottom_nav_folders -> {
                    val intent = Intent(this, AllFoldersActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.bottom_nav_storages -> {
                    val intent = Intent(this, StorageManagementActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.bottom_nav_favorites -> {
                    val intent = Intent(this, MarkedFoldersActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> {
                finish()
            }
            R.id.nav_folders -> {
                startActivity(Intent(this, AllFoldersActivity::class.java))
            }
            R.id.nav_storages -> {
                startActivity(Intent(this, StorageManagementActivity::class.java))
            }
            R.id.nav_marked -> {
                startActivity(Intent(this, MarkedFoldersActivity::class.java))
            }
            R.id.nav_statistics -> {
                startActivity(StatisticsActivity.newIntent(this))
            }
            R.id.nav_settings -> {
                // Already here
            }
            R.id.nav_about -> {
                showAboutDialog()
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun showAboutDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.nav_about)
            .setMessage("MyWH ${getString(R.string.app_version)}\n\nMurr\n\nhttps://github.com/vtstv/MyWH")
            .setPositiveButton(R.string.ok, null)
            .setNeutralButton("GitHub") { _, _ ->
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/vtstv/MyWH"))
                startActivity(intent)
            }
            .show()
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
