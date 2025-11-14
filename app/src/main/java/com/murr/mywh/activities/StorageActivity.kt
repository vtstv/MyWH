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
import androidx.appcompat.widget.SearchView
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.navigation.NavigationView
import com.murr.mywh.R
import com.murr.mywh.adapters.FolderAdapter
import com.murr.mywh.databinding.ActivityStorageBinding
import com.murr.mywh.utils.PreferencesManager
import com.murr.mywh.viewmodels.StorageViewModel

class StorageActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding: ActivityStorageBinding
    private val viewModel: StorageViewModel by viewModels()
    private lateinit var folderAdapter: FolderAdapter
    private lateinit var preferencesManager: PreferencesManager
    private var currentStorageId: Long = -1L

    companion object {
        private const val EXTRA_STORAGE_ID = "extra_storage_id"

        fun newIntent(context: Context, storageId: Long) =
            Intent(context, StorageActivity::class.java).apply {
                putExtra(EXTRA_STORAGE_ID, storageId)
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        preferencesManager = PreferencesManager(this)
        preferencesManager.applyTheme()
        preferencesManager.applyLanguage()

        binding = ActivityStorageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentStorageId = intent.getLongExtra(EXTRA_STORAGE_ID, -1L)

        setupToolbar()
        setupNavigationDrawer()
        setupRecyclerView()
        setupObservers()
        setupListeners()
        setupSearch()
        setupBottomNavigation()
        loadData()
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

    private fun setupRecyclerView() {
        folderAdapter = FolderAdapter(
            onFolderClick = { folder ->
                startActivity(FolderDetailActivity.newIntent(this, folder.storageId, folder.id))
            },
            onFolderLongClick = { folder ->
                showFolderContextMenu(folder)
            }
        )

        binding.rvFolders.apply {
            layoutManager = LinearLayoutManager(this@StorageActivity)
            adapter = folderAdapter
        }
    }

    private fun setupObservers() {
        viewModel.folders.observe(this) { folders ->
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
        // Don't highlight any tab for Storage detail screen
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
                startActivity(SettingsActivity.newIntent(this))
            }
            R.id.nav_about -> {
                showAboutDialog()
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun loadData() {
        viewModel.loadFolders(currentStorageId)
    }

    private fun showAddFolderDialog() {
        val dialog = com.murr.mywh.dialogs.AddFolderDialog { name, description ->
            viewModel.addFolder(currentStorageId, name, description)
        }
        dialog.show(supportFragmentManager, "AddFolderDialog")
    }

    private fun showFolderContextMenu(folder: com.murr.mywh.database.entities.Folder) {
        val options = arrayOf(
            getString(R.string.edit_folder),
            getString(R.string.delete_folder),
            if (folder.isMarked) getString(R.string.unmark_favorite) else getString(R.string.mark_favorite)
        )

        AlertDialog.Builder(this)
            .setTitle(folder.name)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> editFolder(folder)
                    1 -> deleteFolder(folder)
                    2 -> toggleMark(folder)
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

    private fun toggleMark(folder: com.murr.mywh.database.entities.Folder) {
        folder.isMarked = !folder.isMarked
        folder.updatedAt = System.currentTimeMillis()
        viewModel.updateFolder(folder)
    }

    private fun showAboutDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.nav_about)
            .setMessage("MyWH v1.0\n\nMurr\n\nhttps://github.com/vtstv/MyWH")
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

