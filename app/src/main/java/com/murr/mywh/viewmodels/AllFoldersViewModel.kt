package com.murr.mywh.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.murr.mywh.database.entities.Folder
import com.murr.mywh.repositories.FolderRepository
import com.murr.mywh.repositories.StorageRepository
import kotlinx.coroutines.launch

class AllFoldersViewModel(application: Application) : AndroidViewModel(application) {
    private val folderRepository = FolderRepository(application)
    private val storageRepository = StorageRepository(application)

    private val _currentPage = MutableLiveData(0)
    private val _folders = MutableLiveData<List<Folder>>(emptyList())
    val folders: LiveData<List<Folder>> = _folders

    private val _selectedFolders = MutableLiveData<Set<Long>>(emptySet())
    val selectedFolders: LiveData<Set<Long>> = _selectedFolders

    private val _isSelectionMode = MutableLiveData(false)
    val isSelectionMode: LiveData<Boolean> = _isSelectionMode

    val hasMore = MutableLiveData(true)

    val storages = storageRepository.getAllStorages().asLiveData()
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

    fun moveFoldersToStorage(folderIds: List<Long>, storageId: Long) {
        viewModelScope.launch {
            folderRepository.moveFoldersToStorage(folderIds, storageId)
            loadFolders() // Reload folders
        }
    }

    fun deleteFolders(folderIds: List<Long>) {
        viewModelScope.launch {
            folderRepository.deleteFoldersByIds(folderIds)
            loadFolders() // Reload folders
        }
    }

    init {
        loadFolders()
    }

    private fun loadFolders() {
        viewModelScope.launch {
            val page = _currentPage.value ?: 0
            folderRepository.getAllFoldersPaginated(30, page * 30).collect { newFolders ->
                val currentFolders = _folders.value ?: emptyList()
                _folders.value = if (page == 0) newFolders else currentFolders + newFolders
                hasMore.value = newFolders.size >= 30
            }
        }
    }

    fun loadMoreFolders() {
        val currentPage = _currentPage.value ?: 0
        _currentPage.value = currentPage + 1
        loadFolders()
    }

    fun toggleFolderSelection(folderId: Long) {
        val current = _selectedFolders.value ?: emptySet()
        _selectedFolders.value = if (current.contains(folderId)) {
            current - folderId
        } else {
            current + folderId
        }

        if (_selectedFolders.value!!.isEmpty()) {
            _isSelectionMode.value = false
        } else if (!_isSelectionMode.value!!) {
            _isSelectionMode.value = true
        }
    }

    fun clearSelection() {
        _selectedFolders.value = emptySet()
        _isSelectionMode.value = false
    }

    fun toggleFolderMarked(folder: Folder) {
        viewModelScope.launch {
            folder.isMarked = !folder.isMarked
            folder.updatedAt = System.currentTimeMillis()
            folderRepository.updateFolder(folder)
        }
    }
}

class AllFoldersViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AllFoldersViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AllFoldersViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

