package com.murr.mywh.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.murr.mywh.database.entities.Folder
import com.murr.mywh.database.entities.Storage
import com.murr.mywh.repositories.FolderRepository
import com.murr.mywh.repositories.StorageRepository
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val folderRepository = FolderRepository(application)
    private val storageRepository = StorageRepository(application)

    // Use MutableLiveData to trigger updates
    private val _recentFolders = MutableLiveData<List<Folder>>()
    val recentFolders: LiveData<List<Folder>> = _recentFolders

    val storages: LiveData<List<Storage>> = storageRepository.getAllStorages().asLiveData()

    val storageMap: LiveData<Map<Long, String>> = storages.map { storageList ->
        storageList.associate { it.id to it.name }
    }

    private val _searchResults = MutableLiveData<List<Folder>>(emptyList())
    val searchResults: LiveData<List<Folder>> = _searchResults

    init {
        // Load recent folders initially
        viewModelScope.launch {
            folderRepository.getRecentFolders(10).collect { folders ->
                _recentFolders.value = folders
            }
        }
    }

    fun search(query: String) {
        viewModelScope.launch {
            folderRepository.searchFolders(query).collect { results ->
                _searchResults.value = results
            }
        }
    }

    fun toggleFolderMarked(folder: Folder) {
        viewModelScope.launch {
            // Update in database - don't update updatedAt for favorites
            val updatedFolder = folder.copy(
                isMarked = !folder.isMarked
            )
            folderRepository.updateFolder(updatedFolder)

            // Force refresh the list
            _recentFolders.value = _recentFolders.value?.map {
                if (it.id == folder.id) updatedFolder else it
            }
        }
    }

    fun deleteFolder(folder: Folder) {
        viewModelScope.launch {
            folderRepository.deleteFolder(folder)
        }
    }

    fun addFolder(name: String, description: String, storageId: Long) {
        viewModelScope.launch {
            val folder = Folder(
                name = name,
                description = description,
                storageId = storageId,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            folderRepository.insertFolder(folder)
        }
    }

    fun moveFolder(folder: Folder, newStorageId: Long) {
        viewModelScope.launch {
            folder.storageId = newStorageId
            folder.updatedAt = System.currentTimeMillis()
            folderRepository.updateFolder(folder)
        }
    }
}

class HomeViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

