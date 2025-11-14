package com.murr.mywh.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.navigation.NavigationView
import com.murr.mywh.R
import com.murr.mywh.adapters.StorageAdapter
import com.murr.mywh.adapters.FolderAdapter
import com.murr.mywh.databinding.ActivityMainBinding
import com.murr.mywh.utils.PreferencesManager
import com.murr.mywh.viewmodels.MainViewModel

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var storageAdapter: StorageAdapter
    private lateinit var folderAdapter: FolderAdapter
    private lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Apply theme and language
        preferencesManager = PreferencesManager(this)
        preferencesManager.applyTheme()
        preferencesManager.applyLanguage()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupNavigationDrawer()
        setupRecyclerViews()
        setupObservers()
        setupListeners()
        setupSearch()
        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.bottom_nav_home
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.bottom_nav_home -> {
                    // Already on home
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

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu)
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

    private fun setupRecyclerViews() {
        storageAdapter = StorageAdapter { storage ->
            startActivity(StorageActivity.newIntent(this, storage.id))
        }
        
        // Create storage map for folder adapter
        val storageMap = viewModel.storages.value?.associate { it.id to it.name } ?: emptyMap()

        folderAdapter = FolderAdapter(
            onFolderClick = { folder ->
                startActivity(FolderDetailActivity.newIntent(this, folder.storageId, folder.id))
            },
            onFolderLongClick = { folder ->
                showFolderContextMenu(folder)
            },
            storageMap = storageMap
        )

        binding.rvStorages.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = storageAdapter
        }

        binding.rvRecentFolders.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = folderAdapter
        }
    }

    private fun setupObservers() {
        viewModel.storages.observe(this) { storages ->
            storageAdapter.submitList(storages)

            // Update storage map in folder adapter
            val storageMap = storages.associate { it.id to it.name }
            folderAdapter = FolderAdapter(
                onFolderClick = { folder ->
                    startActivity(FolderDetailActivity.newIntent(this, folder.storageId, folder.id))
                },
                onFolderLongClick = { folder ->
                    showFolderContextMenu(folder)
                },
                storageMap = storageMap
            )
            binding.rvRecentFolders.adapter = folderAdapter
            viewModel.recentFolders.value?.let { folderAdapter.submitList(it) }
        }

        viewModel.recentFolders.observe(this) { folders ->
            folderAdapter.submitList(folders)
        }
    }

    private fun setupListeners() {
        binding.fabAddFolder.setOnClickListener {
            showAddFolderDialog()
        }
    }

    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { viewModel.search(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { viewModel.search(it) }
                return true
            }
        })
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> {
                // Already on home
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
                startActivity(SettingsActivity.newIntent(this))
            }
            R.id.nav_about -> {
                showAboutDialog()
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun showAddFolderDialog() {
        val storages = viewModel.storages.value ?: emptyList()
        if (storages.isEmpty()) {
            AlertDialog.Builder(this)
                .setTitle(R.string.add_folder)
                .setMessage(getString(R.string.no_storages))
                .setPositiveButton(R.string.ok, null)
                .show()
            return
        }

        val storageNames = storages.map { it.name }.toTypedArray()
        var selectedStorageId = storages[0].id

        AlertDialog.Builder(this)
            .setTitle(R.string.storage_name)
            .setSingleChoiceItems(storageNames, 0) { _, which ->
                selectedStorageId = storages[which].id
            }
            .setPositiveButton(R.string.ok) { _, _ ->
                showAddFolderDetailsDialog(selectedStorageId)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showAddFolderDetailsDialog(storageId: Long) {
        val dialog = com.murr.mywh.dialogs.AddFolderDialog { name, description ->
            viewModel.addFolder(storageId, name, description)
        }
        dialog.show(supportFragmentManager, "AddFolderDialog")
    }

    private fun showFolderContextMenu(folder: com.murr.mywh.database.entities.Folder) {
        val options = arrayOf(
            getString(R.string.edit_folder),
            getString(R.string.rename_folder),
            getString(R.string.delete_folder),
            if (folder.isMarked) getString(R.string.unmark_favorite) else getString(R.string.mark_favorite)
        )

        AlertDialog.Builder(this)
            .setTitle(folder.name)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> editFolder(folder)
                    1 -> renameFolder(folder)
                    2 -> deleteFolder(folder)
                    3 -> viewModel.toggleFolderMark(folder)
                }
            }
            .show()
    }

    private fun editFolder(folder: com.murr.mywh.database.entities.Folder) {
        val dialog = com.murr.mywh.dialogs.AddFolderDialog(
            initialName = folder.name,
            initialDescription = folder.description
        ) { name, description ->
            folder.name = name
            folder.description = description
            folder.updatedAt = System.currentTimeMillis()
            viewModel.updateFolder(folder)
        }
        dialog.show(supportFragmentManager, "EditFolderDialog")
    }

    private fun renameFolder(folder: com.murr.mywh.database.entities.Folder) {
        val dialog = com.murr.mywh.dialogs.AddFolderDialog(
            initialName = folder.name,
            initialDescription = folder.description
        ) { name, description ->
            folder.name = name
            folder.description = description
            folder.updatedAt = System.currentTimeMillis()
            viewModel.updateFolder(folder)
        }
        dialog.show(supportFragmentManager, "RenameFolderDialog")
    }

    private fun deleteFolder(folder: com.murr.mywh.database.entities.Folder) {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_folder)
            .setMessage(R.string.delete_confirmation)
            .setPositiveButton(R.string.delete) { _, _ ->
                viewModel.deleteFolder(folder)
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
