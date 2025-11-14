package com.murr.mywh.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.murr.mywh.database.entities.Folder
import com.murr.mywh.database.entities.Storage
import com.murr.mywh.repositories.FolderRepository
import com.murr.mywh.repositories.StorageRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class FolderDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = FolderRepository(application)
    private val storageRepository = StorageRepository(application)

    private val _currentFolder = MutableLiveData<Folder?>()
    val currentFolder: LiveData<Folder?> = _currentFolder

    private val _subFolders = MutableLiveData<List<Folder>>()
    val subFolders: LiveData<List<Folder>> = _subFolders

    private val _storageName = MutableLiveData<String>()
    val storageName: LiveData<String> = _storageName

    private var currentFolderId: Long = -1L

    fun loadFolder(folderId: Long) {
        currentFolderId = folderId
        viewModelScope.launch {
            repository.getFolderById(folderId)?.let { folder ->
                _currentFolder.value = folder
                loadSubFolders(folderId)
            }
        }
    }

    private fun loadSubFolders(parentId: Long) {
        viewModelScope.launch {
            repository.getSubFolders(parentId).collect { folders ->
                _subFolders.value = folders
            }
        }
    }

    fun searchSubFolders(query: String) {
        viewModelScope.launch {
            if (query.isEmpty()) {
                loadSubFolders(currentFolderId)
            } else {
                // Show ALL matching folders, like on main page
                repository.searchFolders(query).collect { folders ->
                    _subFolders.value = folders
                }
            }
        }
    }

    fun addSubFolder(storageId: Long, parentId: Long, name: String, description: String) = viewModelScope.launch {
        val folder = Folder(
            name = name,
            description = description,
            storageId = storageId,
            parentFolderId = parentId
        )
        repository.insertFolder(folder)
        loadSubFolders(parentId)
    }

    fun loadStorageName(storageId: Long) = viewModelScope.launch {
        storageRepository.getStorageById(storageId)?.let { storage ->
            _storageName.postValue(storage.name)
        }
    }

    fun loadAllStorages(callback: (List<Storage>) -> Unit) = viewModelScope.launch {
        val storages = storageRepository.getAllStorages().first()
        callback(storages)
    }

    fun updateFolder(folder: Folder) = viewModelScope.launch {
        repository.updateFolder(folder)
        _currentFolder.postValue(folder)
    }

    fun deleteFolder(folder: Folder) = viewModelScope.launch {
        repository.deleteFolder(folder)
        loadSubFolders(currentFolderId)
    }

    fun toggleFolderMark(folder: Folder) = viewModelScope.launch {
        folder.isMarked = !folder.isMarked
        folder.updatedAt = System.currentTimeMillis()
        repository.updateFolder(folder)
        loadSubFolders(currentFolderId)
    }
}

