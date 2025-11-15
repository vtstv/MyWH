package com.murr.mywh.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.murr.mywh.database.entities.Storage
import com.murr.mywh.repositories.StorageRepository
import kotlinx.coroutines.launch

class StoragesViewModel(application: Application) : AndroidViewModel(application) {
    private val storageRepository = StorageRepository(application)

    val storages: LiveData<List<Storage>> = storageRepository.getAllStorages().asLiveData()

    fun addStorage(name: String, description: String) {
        viewModelScope.launch {
            val storage = Storage(
                name = name,
                description = description,
                createdAt = System.currentTimeMillis()
            )
            storageRepository.insertStorage(storage)
        }
    }

    fun updateStorage(storage: Storage) {
        viewModelScope.launch {
            storageRepository.updateStorage(storage)
        }
    }

    fun deleteStorage(storage: Storage) {
        viewModelScope.launch {
            storageRepository.deleteStorage(storage)
        }
    }
}

class StoragesViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StoragesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StoragesViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

