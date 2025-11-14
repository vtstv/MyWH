package com.murr.mywh.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.murr.mywh.adapters.FolderAdapter
import com.murr.mywh.databinding.ActivityFolderBinding
import com.murr.mywh.utils.PreferencesManager
import com.murr.mywh.viewmodels.FolderViewModel

class FolderActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFolderBinding
    private val viewModel: FolderViewModel by viewModels()
    private lateinit var folderAdapter: FolderAdapter
    private lateinit var preferencesManager: PreferencesManager
    private var currentStorageId: Long = -1L
    private var currentFolderId: Long? = null

    companion object {
        private const val EXTRA_STORAGE_ID = "extra_storage_id"
        private const val EXTRA_FOLDER_ID = "extra_folder_id"

        fun newIntent(context: Context, storageId: Long, folderId: Long? = null) =
            Intent(context, FolderActivity::class.java).apply {
                putExtra(EXTRA_STORAGE_ID, storageId)
                folderId?.let { putExtra(EXTRA_FOLDER_ID, it) }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        preferencesManager = PreferencesManager(this)
        preferencesManager.applyTheme()
        preferencesManager.applyLanguage()

        binding = ActivityFolderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupObservers()
        setupListeners()
        loadData()
    }

    private fun setupRecyclerView() {
        folderAdapter = FolderAdapter(
            onFolderClick = { folder ->
                // Открываем вложенную папку
                startActivity(newIntent(this, folder.storageId, folder.id))
            },
            onFolderLongClick = { folder ->
                // TODO: Показать контекстное меню
            }
        )

        binding.rvProducts.apply {
            layoutManager = LinearLayoutManager(this@FolderActivity)
            adapter = folderAdapter
        }
    }

    private fun setupObservers() {
        viewModel.folders.observe(this) { folders ->
            folderAdapter.submitList(folders)
        }
    }

    private fun setupListeners() {
        binding.fabAddProduct.setOnClickListener {
            showAddFolderDialog()
        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = true
            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { viewModel.searchFolders(it) }
                return true
            }
        })
    }

    private fun loadData() {
        currentStorageId = intent.getLongExtra(EXTRA_STORAGE_ID, -1L)
        val folderId = intent.getLongExtra(EXTRA_FOLDER_ID, -1L)
        currentFolderId = if (folderId != -1L) folderId else null

        viewModel.loadFolders(currentStorageId, currentFolderId)
    }

    private fun showAddFolderDialog() {
        val dialog = com.murr.mywh.dialogs.AddFolderDialog { name, description ->
            viewModel.addFolder(currentStorageId, name, description, currentFolderId)
        }
        dialog.show(supportFragmentManager, "AddFolderDialog")
    }
}
