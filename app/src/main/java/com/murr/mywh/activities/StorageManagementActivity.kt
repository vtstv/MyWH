package com.murr.mywh.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.murr.mywh.R
import com.murr.mywh.adapters.StorageAdapter
import com.murr.mywh.databinding.ActivityStorageManagementBinding
import com.murr.mywh.database.entities.Storage
import com.murr.mywh.utils.PreferencesManager
import com.murr.mywh.viewmodels.StorageManagementViewModel

class StorageManagementActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStorageManagementBinding
    private val viewModel: StorageManagementViewModel by viewModels()
    private lateinit var storageAdapter: StorageAdapter
    private lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        preferencesManager = PreferencesManager(this)
        preferencesManager.applyTheme()
        preferencesManager.applyLanguage()

        binding = ActivityStorageManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupObservers()
        setupListeners()
        setupBottomNavigation()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.nav_storages)
    }

    private fun setupRecyclerView() {
        storageAdapter = StorageAdapter(
            onStorageClick = { storage ->
                showStorageOptionsDialog(storage)
            }
        )

        binding.rvStorages.apply {
            layoutManager = LinearLayoutManager(this@StorageManagementActivity)
            adapter = storageAdapter
        }
    }

    private fun setupObservers() {
        viewModel.storages.observe(this) { storages ->
            storageAdapter.submitList(storages)
        }
    }

    private fun setupListeners() {
        binding.fabAddStorage.setOnClickListener {
            showAddStorageDialog()
        }
    }

    private fun showAddStorageDialog() {
        val dialog = com.murr.mywh.dialogs.AddStorageDialog { name, description ->
            viewModel.addStorage(name, description)
        }
        dialog.show(supportFragmentManager, "AddStorageDialog")
    }

    private fun showStorageOptionsDialog(storage: Storage) {
        val options = arrayOf(
            getString(R.string.edit_storage),
            getString(R.string.delete_storage)
        )

        AlertDialog.Builder(this)
            .setTitle(storage.name)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> renameStorage(storage)
                    1 -> deleteStorage(storage)
                }
            }
            .show()
    }

    private fun renameStorage(storage: Storage) {
        val dialog = com.murr.mywh.dialogs.AddStorageDialog(
            initialName = storage.name,
            initialDescription = storage.description
        ) { name, description ->
            storage.name = name
            storage.description = description
            viewModel.updateStorage(storage)
        }
        dialog.show(supportFragmentManager, "EditStorageDialog")
    }

    private fun deleteStorage(storage: Storage) {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_storage)
            .setMessage(R.string.delete_confirmation)
            .setPositiveButton(R.string.delete) { _, _ ->
                viewModel.deleteStorage(storage)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.bottom_nav_storages
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
                    // Already here
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

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

