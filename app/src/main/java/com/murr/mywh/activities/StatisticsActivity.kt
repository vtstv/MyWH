package com.murr.mywh.activities

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.google.android.material.navigation.NavigationView
import com.murr.mywh.R
import com.murr.mywh.data.Statistics
import com.murr.mywh.databinding.ActivityStatisticsBinding
import com.murr.mywh.utils.PreferencesManager
import com.murr.mywh.viewmodels.StatisticsViewModel

class StatisticsActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding: ActivityStatisticsBinding
    private val viewModel: StatisticsViewModel by viewModels()
    private lateinit var preferencesManager: PreferencesManager

    companion object {
        fun newIntent(context: Context) = Intent(context, StatisticsActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        preferencesManager = PreferencesManager(this)
        preferencesManager.applyTheme()
        preferencesManager.applyLanguage()

        binding = ActivityStatisticsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupNavigationDrawer()
        setupObservers()
        setupBottomNavigation()
        loadStatistics()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu)
        supportActionBar?.title = getString(R.string.statistics)
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

    private fun setupObservers() {
        viewModel.statistics.observe(this) { stats ->
            updateStatisticsUI(stats)
        }
    }

    private fun loadStatistics() {
        viewModel.loadStatistics()
    }

    private fun updateStatisticsUI(stats: Statistics) {
        binding.tvTotalStorages.text = "${getString(R.string.total_storages)}: ${stats.totalStorages}"
        binding.tvTotalFolders.text = "${getString(R.string.total_folders)}: ${stats.totalFolders}"
        binding.tvTotalProducts.text = "${getString(R.string.total_items)}: ${stats.totalProducts}"
        binding.tvMarkedFolders.text = "${getString(R.string.marked_folders)}: ${stats.markedFolders}"
        binding.tvMarkedProducts.text = "${getString(R.string.marked_folders)}: ${stats.markedProducts}"
    }

    private fun setupBottomNavigation() {
        // Don't highlight any tab for Statistics screen
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
                // Already here
            }
            R.id.nav_settings -> {
                startActivity(SettingsActivity.newIntent(this))
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

