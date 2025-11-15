package com.murr.mywh.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.murr.mywh.database.entities.Folder
import com.murr.mywh.repositories.FolderRepository
import com.murr.mywh.repositories.StorageRepository
import kotlinx.coroutines.launch

class FavoritesViewModel(application: Application) : AndroidViewModel(application) {
    private val folderRepository = FolderRepository(application)
    private val storageRepository = StorageRepository(application)

    val markedFolders: LiveData<List<Folder>> = folderRepository.getMarkedFolders().asLiveData()

    val storages = storageRepository.getAllStorages().asLiveData()
    val storageMap: LiveData<Map<Long, String>> = storages.map { storageList ->
        storageList.associate { it.id to it.name }
    }

    private val _searchResults = MutableLiveData<List<Folder>>(emptyList())
    val searchResults: LiveData<List<Folder>> = _searchResults

    fun search(query: String) {
        viewModelScope.launch {
            folderRepository.searchFolders(query).collect { results ->
                _searchResults.value = results.filter { it.isMarked }
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
}

class FavoritesViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FavoritesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FavoritesViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

