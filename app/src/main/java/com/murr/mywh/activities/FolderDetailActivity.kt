package com.murr.mywh.activities

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
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
import com.murr.mywh.databinding.ActivityFolderDetailBinding
import com.murr.mywh.viewmodels.FolderDetailViewModel
import com.murr.mywh.utils.PreferencesManager
import java.text.SimpleDateFormat
import java.util.*

class FolderDetailActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding: ActivityFolderDetailBinding
    private val viewModel: FolderDetailViewModel by viewModels()
    private lateinit var folderAdapter: FolderAdapter
    private lateinit var preferencesManager: PreferencesManager
    private var currentFolderId: Long = -1L
    private var currentStorageId: Long = -1L
    
    companion object {
        private const val EXTRA_FOLDER_ID = "extra_folder_id"
        private const val EXTRA_STORAGE_ID = "extra_storage_id"
        
        fun newIntent(context: Context, storageId: Long, folderId: Long) =
            Intent(context, FolderDetailActivity::class.java).apply {
                putExtra(EXTRA_STORAGE_ID, storageId)
                putExtra(EXTRA_FOLDER_ID, folderId)
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        preferencesManager = PreferencesManager(this)
        preferencesManager.applyTheme()
        preferencesManager.applyLanguage()
        
        binding = ActivityFolderDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        currentStorageId = intent.getLongExtra(EXTRA_STORAGE_ID, -1L)
        currentFolderId = intent.getLongExtra(EXTRA_FOLDER_ID, -1L)
        
        setupToolbar()
        setupNavigationDrawer()
        setupRecyclerView()
        setupObservers()
        setupListeners()
        setupSearch()
        setupBottomNavigation()

        viewModel.loadFolder(currentFolderId)
    }
    
    private fun setupBottomNavigation() {
        // Don't highlight any tab for FolderDetail screen
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
                startActivity(newIntent(this, folder.storageId, folder.id))
            },
            onFolderLongClick = { folder ->
                showFolderContextMenu(folder)
            }
        )
        
        binding.rvSubFolders.apply {
            layoutManager = LinearLayoutManager(this@FolderDetailActivity)
            adapter = folderAdapter
        }
    }
    
    private fun setupObservers() {
        viewModel.currentFolder.observe(this) { folder ->
            folder?.let {
                supportActionBar?.title = it.name
                
                // Show folder details
                binding.tvFolderName.text = it.name
                binding.tvDescription.text = it.description
                
                val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                binding.tvCreatedAt.text = getString(R.string.created_at) + ": " + 
                    dateFormat.format(Date(it.createdAt))
                binding.tvUpdatedAt.text = getString(R.string.updated_at) + ": " + 
                    dateFormat.format(Date(it.updatedAt))

                // Load and show storage name
                viewModel.loadStorageName(it.storageId)
            }
        }

        viewModel.storageName.observe(this) { storageName ->
            binding.tvStorageName.text = storageName ?: getString(R.string.unknown)
        }

        viewModel.subFolders.observe(this) { folders ->
            folderAdapter.submitList(folders)
        }
    }
    
    private fun setupListeners() {
        binding.fabAddSubFolder.setOnClickListener {
            showAddSubFolderDialog()
        }
        
        binding.btnEditFolder.setOnClickListener {
            viewModel.currentFolder.value?.let { editFolder(it) }
        }

        binding.btnCopyContent.setOnClickListener {
            viewModel.currentFolder.value?.let { folder ->
                copyToClipboard(folder.description)
            }
        }

        binding.btnChangeStorage.setOnClickListener {
            viewModel.currentFolder.value?.let { folder ->
                showChangeStorageDialog(folder)
            }
        }
    }
    
    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    viewModel.searchSubFolders(it)
                    // Hide folder details card when searching
                    binding.cardFolderDetails.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let {
                    viewModel.searchSubFolders(it)
                    // Hide folder details card when searching
                    binding.cardFolderDetails.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
                }
                return true
            }
        })
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
    
    private fun showAddSubFolderDialog() {
        val dialog = com.murr.mywh.dialogs.AddFolderDialog(
            isSubFolder = true
        ) { name, description ->
            viewModel.addSubFolder(currentStorageId, currentFolderId, name, description)
        }
        dialog.show(supportFragmentManager, "AddSubFolderDialog")
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
                    2 -> viewModel.toggleFolderMark(folder)
                }
            }
            .show()
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

    private fun showChangeStorageDialog(folder: com.murr.mywh.database.entities.Folder) {
        viewModel.loadAllStorages { storages ->
            val storageNames = storages.map { it.name }.toTypedArray()
            val storageIds = storages.map { it.id }.toLongArray()
            val currentIndex = storages.indexOfFirst { it.id == folder.storageId }

            AlertDialog.Builder(this)
                .setTitle(R.string.change_storage)
                .setSingleChoiceItems(storageNames, currentIndex) { dialog, which ->
                    val newStorageId = storageIds[which]
                    if (newStorageId != folder.storageId) {
                        folder.storageId = newStorageId
                        folder.updatedAt = System.currentTimeMillis()
                        viewModel.updateFolder(folder)
                        viewModel.loadStorageName(newStorageId)
                    }
                    dialog.dismiss()
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }
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
    
    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Folder Content", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
    
    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}

