package com.murr.mywh.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.murr.mywh.database.entities.Folder
import com.murr.mywh.repositories.FolderRepository
import com.murr.mywh.repositories.StorageRepository
import kotlinx.coroutines.launch

class FolderDetailViewModel(
    application: Application,
    private val folderId: Long
) : AndroidViewModel(application) {
    private val folderRepository = FolderRepository(application)
    private val storageRepository = StorageRepository(application)

    private val _folder = MutableLiveData<Folder>()
    val folder: LiveData<Folder> = _folder

    private val _storageName = MutableLiveData<String>()
    val storageName: LiveData<String> = _storageName

    init {
        loadFolder()
    }

    private fun loadFolder() {
        viewModelScope.launch {
            val folderData = folderRepository.getFolderById(folderId)
            _folder.value = folderData

            folderData?.let {
                val storage = storageRepository.getStorageById(it.storageId)
                _storageName.value = storage?.name ?: ""
            }
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            _folder.value?.let { currentFolder ->
                currentFolder.isMarked = !currentFolder.isMarked
                currentFolder.updatedAt = System.currentTimeMillis()
                folderRepository.updateFolder(currentFolder)
                _folder.value = currentFolder
            }
        }
    }

    fun updateFolder(name: String, description: String) {
        viewModelScope.launch {
            _folder.value?.let { currentFolder ->
                currentFolder.name = name
                currentFolder.description = description
                currentFolder.updatedAt = System.currentTimeMillis()
                folderRepository.updateFolder(currentFolder)
                _folder.value = currentFolder
            }
        }
    }

    fun deleteFolder() {
        viewModelScope.launch {
            _folder.value?.let { currentFolder ->
                folderRepository.deleteFolder(currentFolder)
            }
        }
    }
}

class FolderDetailViewModelFactory(
    private val application: Application,
    private val folderId: Long
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FolderDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FolderDetailViewModel(application, folderId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

