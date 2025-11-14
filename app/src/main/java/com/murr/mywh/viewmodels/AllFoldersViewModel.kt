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
import kotlinx.coroutines.launch

class AllFoldersViewModel(application: Application) : AndroidViewModel(application) {
    private val folderRepository = FolderRepository(application)
    private val storageRepository = StorageRepository(application)

    private val _folders = MutableLiveData<List<Folder>>()
    val folders: LiveData<List<Folder>> = _folders

    private val _storages = MutableLiveData<List<Storage>>()
    val storages: LiveData<List<Storage>> = _storages

    private var currentPage = 0
    private val pageSize = 30
    private var searchQuery: String? = null

    init {
        loadFolders()
        loadStorages()
    }

    private fun loadFolders() {
        viewModelScope.launch {
            if (searchQuery.isNullOrEmpty()) {
                folderRepository.getAllFoldersPaginated(pageSize, currentPage * pageSize).collect { folders ->
                    _folders.value = folders
                }
            } else {
                searchFolders(searchQuery!!)
            }
        }
    }

    private fun loadStorages() {
        viewModelScope.launch {
            storageRepository.getAllStorages().collect { storages ->
                _storages.value = storages
            }
        }
    }

    fun searchFolders(query: String) {
        searchQuery = query
        viewModelScope.launch {
            if (query.isEmpty()) {
                loadFolders()
            } else {
                folderRepository.searchFolders(query).collect { folders ->
                    _folders.value = folders
                }
            }
        }
    }

    fun loadNextPage() {
        currentPage++
        loadFolders()
    }

    fun loadPreviousPage() {
        if (currentPage > 0) {
            currentPage--
            loadFolders()
        }
    }

    fun moveFoldersToStorage(folderIds: List<Long>, targetStorageId: Long) = viewModelScope.launch {
        folderIds.forEach { folderId ->
            folderRepository.getFolderById(folderId)?.let { folder ->
                folder.storageId = targetStorageId
                folder.updatedAt = System.currentTimeMillis()
                folderRepository.updateFolder(folder)
            }
        }
        loadFolders()
    }

    fun deleteFolders(folderIds: List<Long>) = viewModelScope.launch {
        folderIds.forEach { folderId ->
            folderRepository.getFolderById(folderId)?.let { folder ->
                folderRepository.deleteFolder(folder)
            }
        }
        loadFolders()
    }
}

