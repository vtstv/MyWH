package com.murr.mywh.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.murr.mywh.database.entities.Folder
import com.murr.mywh.database.entities.Storage
import com.murr.mywh.repositories.FolderRepository
import com.murr.mywh.repositories.StorageRepository
import kotlinx.coroutines.launch

class StorageDetailViewModel(
    application: Application,
    private val storageId: Long
) : AndroidViewModel(application) {
    private val folderRepository = FolderRepository(application)
    private val storageRepository = StorageRepository(application)

    private val _storage = MutableLiveData<Storage>()
    val storage: LiveData<Storage> = _storage

    val folders: LiveData<List<Folder>> = folderRepository.getRootFoldersByStorage(storageId).asLiveData()

    init {
        loadStorage()
    }

    private fun loadStorage() {
        viewModelScope.launch {
            _storage.value = storageRepository.getStorageById(storageId)
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

class StorageDetailViewModelFactory(
    private val application: Application,
    private val storageId: Long
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StorageDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StorageDetailViewModel(application, storageId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

