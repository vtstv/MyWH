package com.murr.mywh.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.murr.mywh.database.entities.Storage
import com.murr.mywh.repositories.StorageRepository
import kotlinx.coroutines.launch

class StorageManagementViewModel(application: Application) : AndroidViewModel(application) {
    private val storageRepository = StorageRepository(application)

    private val _storages = MutableLiveData<List<Storage>>()
    val storages: LiveData<List<Storage>> = _storages

    init {
        loadStorages()
    }

    private fun loadStorages() {
        viewModelScope.launch {
            storageRepository.getAllStorages().collect { storages ->
                _storages.value = storages
            }
        }
    }

    fun addStorage(name: String, description: String = "") = viewModelScope.launch {
        storageRepository.insertStorage(Storage(name = name, description = description))
    }

    fun updateStorage(storage: Storage) = viewModelScope.launch {
        storageRepository.updateStorage(storage)
    }

    fun deleteStorage(storage: Storage) = viewModelScope.launch {
        storageRepository.deleteStorage(storage)
    }
}

