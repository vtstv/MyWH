package com.murr.mywh.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.navigation.NavigationView
import com.murr.mywh.R
import com.murr.mywh.adapters.FolderAdapter
import com.murr.mywh.databinding.ActivityMarkedFoldersBinding
import com.murr.mywh.utils.PreferencesManager
import com.murr.mywh.viewmodels.FolderViewModel

class MarkedFoldersActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding: ActivityMarkedFoldersBinding
    private val viewModel: FolderViewModel by viewModels()
    private lateinit var folderAdapter: FolderAdapter
    private lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        preferencesManager = PreferencesManager(this)
        preferencesManager.applyTheme()
        preferencesManager.applyLanguage()

        binding = ActivityMarkedFoldersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupObservers()
        setupNavigationDrawer()
        setupBottomNavigation()
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

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.marked_folders)
    }

    private fun setupRecyclerView() {
        folderAdapter = FolderAdapter(
            onFolderClick = { folder ->
                startActivity(FolderDetailActivity.newIntent(this, folder.storageId, folder.id))
            },
            onFolderLongClick = { folder ->
                showRemoveFromFavoritesDialog(folder)
            }
        )

        binding.rvMarkedFolders.apply {
            layoutManager = LinearLayoutManager(this@MarkedFoldersActivity)
            adapter = folderAdapter
        }
    }

    private fun setupObservers() {
        viewModel.markedFolders.observe(this) { folders ->
            folderAdapter.submitList(folders)
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.bottom_nav_favorites
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
                    // Already here
                    true
                }
                else -> false
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> finish()
            R.id.nav_folders -> startActivity(Intent(this, AllFoldersActivity::class.java))
            R.id.nav_storages -> startActivity(Intent(this, StorageManagementActivity::class.java))
            R.id.nav_marked -> { /* Already here */ }
            R.id.nav_statistics -> startActivity(StatisticsActivity.newIntent(this))
            R.id.nav_settings -> startActivity(SettingsActivity.newIntent(this))
            R.id.nav_about -> showAboutDialog()
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun showRemoveFromFavoritesDialog(folder: com.murr.mywh.database.entities.Folder) {
        AlertDialog.Builder(this)
            .setTitle(folder.name)
            .setMessage(R.string.remove_from_favorites_confirmation)
            .setPositiveButton(R.string.remove) { _, _ ->
                folder.isMarked = false
                folder.updatedAt = System.currentTimeMillis()
                viewModel.updateFolder(folder)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showAboutDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.nav_about)
            .setMessage("MyWH ${getString(R.string.app_version)}\n\nMurr\n\nhttps://github.com/vtstv/MyWH")
            .setPositiveButton(R.string.ok, null)
            .setNeutralButton("GitHub") { _, _ ->
                val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://github.com/vtstv/MyWH"))
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

