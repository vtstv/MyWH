package com.murr.mywh.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.murr.mywh.repositories.FolderRepository
import com.murr.mywh.database.entities.Folder
import kotlinx.coroutines.launch

class FolderViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: FolderRepository = FolderRepository(application)

    private val _folders = MutableLiveData<List<Folder>>()
    val folders: LiveData<List<Folder>> = _folders

    private val _markedFolders = MutableLiveData<List<Folder>>()
    val markedFolders: LiveData<List<Folder>> = _markedFolders

    private var currentStorageId: Long = -1L
    private var currentParentId: Long? = null

    fun loadFolders(storageId: Long, parentId: Long? = null) {
        currentStorageId = storageId
        currentParentId = parentId

        viewModelScope.launch {
            if (parentId == null) {
                repository.getRootFoldersByStorage(storageId).collect {
                    _folders.value = it
                }
            } else {
                repository.getSubFolders(parentId).collect {
                    _folders.value = it
                }
            }
        }
    }
    
    fun loadMarkedFolders() {
        viewModelScope.launch {
            repository.getMarkedFolders().collect {
                _markedFolders.value = it
            }
        }
    }

    init {
        loadMarkedFolders()
    }

    fun searchFolders(query: String) {
        viewModelScope.launch {
            repository.searchFolders(query).collect {
                _folders.value = it
            }
        }
    }

    fun addFolder(storageId: Long, name: String, description: String = "", parentId: Long? = null) = viewModelScope.launch {
        val folder = Folder(
            name = name,
            description = description,
            storageId = storageId,
            parentFolderId = parentId
        )
        repository.insertFolder(folder)
    }

    fun updateFolder(folder: Folder) = viewModelScope.launch {
        folder.updatedAt = System.currentTimeMillis()
        repository.updateFolder(folder)
    }

    fun deleteFolder(folder: Folder) = viewModelScope.launch {
        repository.deleteFolder(folder)
    }

    fun renameFolder(folder: Folder, newName: String) = viewModelScope.launch {
        folder.name = newName
        folder.updatedAt = System.currentTimeMillis()
        repository.updateFolder(folder)
    }

    fun moveFolder(folderId: Long, newParentId: Long?) = viewModelScope.launch {
        repository.moveFolder(folderId, newParentId, System.currentTimeMillis())
    }

    fun copyFolder(sourceId: Long, targetParentId: Long?) = viewModelScope.launch {
        repository.copyFolder(sourceId, targetParentId)
    }

    fun toggleMark(folder: Folder) = viewModelScope.launch {
        folder.isMarked = !folder.isMarked
        folder.updatedAt = System.currentTimeMillis()
        repository.updateFolder(folder)
    }
}
