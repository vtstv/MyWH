package com.murr.mywh.viewmodels

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.murr.mywh.R
import com.murr.mywh.database.entities.Storage
import com.murr.mywh.database.entities.Folder
import com.murr.mywh.repositories.StorageRepository
import com.murr.mywh.repositories.FolderRepository
import com.murr.mywh.utils.DataManager
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect
import java.io.File

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val storageRepository = StorageRepository(application)
    private val folderRepository = FolderRepository(application)
    private val dataManager = DataManager(application)

    private val _storages = MutableLiveData<List<Storage>>()
    val storages: LiveData<List<Storage>> = _storages

    private val _recentFolders = MutableLiveData<List<Folder>>()
    val recentFolders: LiveData<List<Folder>> = _recentFolders

    private val _searchResults = MutableLiveData<List<Folder>>()
    val searchResults: LiveData<List<Folder>> = _searchResults

    init {
        loadStorages()
        loadRecentFolders()
    }

    fun loadStorages() = viewModelScope.launch {
        storageRepository.getAllStorages().collect { storages ->
            _storages.value = storages
        }
    }

    private fun loadRecentFolders() = viewModelScope.launch {
        folderRepository.getRecentFolders(15).collect { folders ->
            _recentFolders.value = folders
        }
    }

    fun showMarkedFolders() = viewModelScope.launch {
        folderRepository.getMarkedFolders().collect { folders ->
            _recentFolders.value = folders
        }
    }

    fun addStorage(name: String, description: String = "") = viewModelScope.launch {
        storageRepository.insertStorage(Storage(name = name, description = description))
    }

    fun addFolder(storageId: Long, name: String, description: String = "") = viewModelScope.launch {
        val folder = Folder(
            name = name,
            description = description,
            storageId = storageId
        )
        folderRepository.insertFolder(folder)
    }

    fun deleteFolder(folder: Folder) = viewModelScope.launch {
        folderRepository.deleteFolder(folder)
    }

    fun updateFolder(folder: Folder) = viewModelScope.launch {
        folderRepository.updateFolder(folder)
    }

    fun toggleFolderMark(folder: Folder) = viewModelScope.launch {
        folder.isMarked = !folder.isMarked
        folder.updatedAt = System.currentTimeMillis()
        folderRepository.updateFolder(folder)
    }

    fun search(query: String) = viewModelScope.launch {
        if (query.isEmpty()) {
            loadRecentFolders()
        } else {
            folderRepository.searchFolders(query).collect { folders ->
                _recentFolders.value = folders
            }
        }
    }

    fun exportData(context: Context) = viewModelScope.launch {
        val file = File(context.getExternalFilesDir(null), "mywh_export_${System.currentTimeMillis()}.json")
        val success = dataManager.exportData(file)

        val message = if (success) {
            context.getString(R.string.export_success) + ": ${file.absolutePath}"
        } else {
            context.getString(R.string.export_error)
        }

        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    fun importData(context: Context) = viewModelScope.launch {
        // TODO: Открыть файловый менеджер для выбора файла
        // Пока просто показываем сообщение
        Toast.makeText(context, "Import functionality - select file", Toast.LENGTH_SHORT).show()
    }
}
