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

    val recentFolders: LiveData<List<Folder>> = folderRepository.getRecentFolders(10).asLiveData()
    val storages: LiveData<List<Storage>> = storageRepository.getAllStorages().asLiveData()

    val storageMap: LiveData<Map<Long, String>> = storages.map { storageList ->
        storageList.associate { it.id to it.name }
    }

    private val _searchResults = MutableLiveData<List<Folder>>(emptyList())
    val searchResults: LiveData<List<Folder>> = _searchResults

    fun search(query: String) {
        viewModelScope.launch {
            folderRepository.searchFolders(query).collect { results ->
                _searchResults.value = results
            }
        }
    }

    fun toggleFolderMarked(folder: Folder) {
        viewModelScope.launch {
            folder.isMarked = !folder.isMarked
            folder.updatedAt = System.currentTimeMillis()
            folderRepository.updateFolder(folder)
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

