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
import com.murr.mywh.adapters.FolderAdapter
import com.murr.mywh.databinding.ActivityAllFoldersBinding
import com.murr.mywh.database.entities.Folder
import com.murr.mywh.utils.PreferencesManager
import com.murr.mywh.viewmodels.AllFoldersViewModel

class AllFoldersActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding: ActivityAllFoldersBinding
    private val viewModel: AllFoldersViewModel by viewModels()
    private lateinit var folderAdapter: FolderAdapter
    private lateinit var preferencesManager: PreferencesManager
    private val selectedFolders = mutableSetOf<Long>()
    private var isSelectionMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        preferencesManager = PreferencesManager(this)
        preferencesManager.applyTheme()
        preferencesManager.applyLanguage()

        binding = ActivityAllFoldersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupNavigationDrawer()
        setupRecyclerView()
        setupObservers()
        setupListeners()
        setupSearch()
        setupBottomNavigation()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu)
        supportActionBar?.title = getString(R.string.all_folders)
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

    private fun setupRecyclerView() {
        folderAdapter = FolderAdapter(
            onFolderClick = { folder ->
                if (isSelectionMode) {
                    toggleFolderSelection(folder)
                } else {
                    startActivity(FolderDetailActivity.newIntent(this, folder.storageId, folder.id))
                }
            },
            onFolderLongClick = { folder ->
                if (!isSelectionMode) {
                    startSelectionMode()
                    toggleFolderSelection(folder)
                }
            }
        )

        binding.rvFolders.apply {
            layoutManager = LinearLayoutManager(this@AllFoldersActivity)
            adapter = folderAdapter
        }
    }

    private fun setupObservers() {
        viewModel.folders.observe(this) { folders ->
            folderAdapter.submitList(folders)
            updateEmptyState(folders.isEmpty())
        }

        viewModel.storages.observe(this) { storages ->
            val storageMap = storages.associate { it.id to it.name }
            folderAdapter = FolderAdapter(
                onFolderClick = { folder ->
                    if (isSelectionMode) {
                        toggleFolderSelection(folder)
                    } else {
                        startActivity(FolderDetailActivity.newIntent(this, folder.storageId, folder.id))
                    }
                },
                onFolderLongClick = { folder ->
                    if (!isSelectionMode) {
                        startSelectionMode()
                        toggleFolderSelection(folder)
                    }
                },
                storageMap = storageMap
            )
            binding.rvFolders.adapter = folderAdapter
            viewModel.folders.value?.let { folderAdapter.submitList(it) }
        }
    }

    private fun setupListeners() {
        binding.fabBatchActions.setOnClickListener {
            if (isSelectionMode && selectedFolders.isNotEmpty()) {
                showBatchActionsDialog()
            } else {
                startSelectionMode()
            }
        }

        binding.btnNextPage.setOnClickListener {
            viewModel.loadNextPage()
        }

        binding.btnPrevPage.setOnClickListener {
            viewModel.loadPreviousPage()
        }
    }

    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { viewModel.searchFolders(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { viewModel.searchFolders(it) }
                return true
            }
        })
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.bottom_nav_folders
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
                    // Already here
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
            R.id.nav_home -> finish()
            R.id.nav_folders -> { /* Already here */ }
            R.id.nav_storages -> startActivity(Intent(this, StorageManagementActivity::class.java))
            R.id.nav_marked -> startActivity(Intent(this, MarkedFoldersActivity::class.java))
            R.id.nav_statistics -> startActivity(StatisticsActivity.newIntent(this))
            R.id.nav_settings -> startActivity(SettingsActivity.newIntent(this))
            R.id.nav_about -> showAboutDialog()
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun startSelectionMode() {
        isSelectionMode = true
        selectedFolders.clear()
        binding.fabBatchActions.text = getString(R.string.batch_actions)
        supportActionBar?.title = getString(R.string.select_folders)
    }

    private fun exitSelectionMode() {
        isSelectionMode = false
        selectedFolders.clear()
        folderAdapter.clearSelection()
        binding.fabBatchActions.text = getString(R.string.select_mode)
        supportActionBar?.title = getString(R.string.all_folders)
    }

    private fun toggleFolderSelection(folder: Folder) {
        if (selectedFolders.contains(folder.id)) {
            selectedFolders.remove(folder.id)
        } else {
            selectedFolders.add(folder.id)
        }

        // Update adapter to show selection
        folderAdapter.setSelectedIds(selectedFolders)

        if (selectedFolders.isEmpty()) {
            exitSelectionMode()
        } else {
            supportActionBar?.title = "${selectedFolders.size} ${getString(R.string.selected)}"
        }
    }

    private fun showBatchActionsDialog() {
        val actions = arrayOf(
            getString(R.string.batch_rename),
            getString(R.string.batch_move),
            getString(R.string.batch_delete),
            getString(R.string.cancel_selection)
        )

        AlertDialog.Builder(this)
            .setTitle(R.string.batch_actions)
            .setItems(actions) { _, which ->
                when (which) {
                    0 -> batchRename()
                    1 -> batchMove()
                    2 -> batchDelete()
                    3 -> exitSelectionMode()
                }
            }
            .show()
    }

    private fun batchRename() {
        // TODO: Implement batch rename dialog
        exitSelectionMode()
    }

    private fun batchMove() {
        val storages = viewModel.storages.value ?: return
        val storageNames = storages.map { it.name }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle(R.string.select_storage)
            .setItems(storageNames) { _, which ->
                val targetStorageId = storages[which].id
                viewModel.moveFoldersToStorage(selectedFolders.toList(), targetStorageId)
                exitSelectionMode()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun batchDelete() {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_folders)
            .setMessage(getString(R.string.delete_confirmation_multiple, selectedFolders.size))
            .setPositiveButton(R.string.delete) { _, _ ->
                viewModel.deleteFolders(selectedFolders.toList())
                exitSelectionMode()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        binding.tvEmpty.visibility = if (isEmpty) android.view.View.VISIBLE else android.view.View.GONE
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
        when {
            binding.drawerLayout.isDrawerOpen(GravityCompat.START) -> {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            }
            isSelectionMode -> {
                exitSelectionMode()
            }
            else -> {
                super.onBackPressed()
            }
        }
    }
}

