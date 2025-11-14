package com.murr.mywh.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.murr.mywh.database.entities.Folder
import com.murr.mywh.repositories.FolderRepository
import kotlinx.coroutines.launch

class StorageViewModel(application: Application) : AndroidViewModel(application) {
    private val folderRepository = FolderRepository(application)
    private val _folders = MutableLiveData<List<Folder>>()
    val folders: LiveData<List<Folder>> = _folders
    private var currentStorageId: Long = -1L

    fun loadFolders(storageId: Long) {
        currentStorageId = storageId
        viewModelScope.launch {
            folderRepository.getRootFoldersByStorage(storageId).collect { folders ->
                _folders.value = folders
            }
        }
    }

    fun addFolder(storageId: Long, name: String, description: String = "") = viewModelScope.launch {
        val folder = Folder(
            name = name,
            description = description,
            storageId = storageId
        )
        folderRepository.insertFolder(folder)
    }

    fun updateFolder(folder: Folder) = viewModelScope.launch {
        folderRepository.updateFolder(folder)
    }

    fun deleteFolder(folder: Folder) = viewModelScope.launch {
        folderRepository.deleteFolder(folder)
    }

    fun searchFolders(query: String) = viewModelScope.launch {
        if (query.isEmpty()) {
            loadFolders(currentStorageId)
        } else {
            folderRepository.searchFolders(query).collect { allFolders ->
                val filtered = allFolders.filter { it.storageId == currentStorageId }
                _folders.value = filtered
            }
        }
    }
}

