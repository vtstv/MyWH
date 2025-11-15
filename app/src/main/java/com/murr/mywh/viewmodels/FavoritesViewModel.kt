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

    private val _markedFolders = MutableLiveData<List<Folder>>()
    val markedFolders: LiveData<List<Folder>> = _markedFolders

    val storages = storageRepository.getAllStorages().asLiveData()
    val storageMap: LiveData<Map<Long, String>> = storages.map { storageList ->
        storageList.associate { it.id to it.name }
    }

    private val _searchResults = MutableLiveData<List<Folder>>(emptyList())
    val searchResults: LiveData<List<Folder>> = _searchResults

    init {
        // Load marked folders initially
        viewModelScope.launch {
            folderRepository.getMarkedFolders().collect { folders ->
                _markedFolders.value = folders
            }
        }
    }

    fun search(query: String) {
        viewModelScope.launch {
            folderRepository.searchFolders(query).collect { results ->
                _searchResults.value = results.filter { it.isMarked }
            }
        }
    }

    fun toggleFolderMarked(folder: Folder) {
        viewModelScope.launch {
            // Create updated copy - don't update updatedAt for favorites
            val updatedFolder = folder.copy(
                isMarked = !folder.isMarked
            )
            folderRepository.updateFolder(updatedFolder)

            // Force refresh the list
            _markedFolders.value = _markedFolders.value?.map {
                if (it.id == folder.id) updatedFolder else it
            }?.filter { it.isMarked } // Remove if unmarked

            // Also update search results if present
            _searchResults.value = _searchResults.value?.map {
                if (it.id == folder.id) updatedFolder else it
            }?.filter { it.isMarked }
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

