package com.murr.mywh.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.murr.mywh.repositories.FolderRepository
import com.murr.mywh.repositories.StorageRepository
import kotlinx.coroutines.launch
import java.util.*

class StatisticsViewModel(application: Application) : AndroidViewModel(application) {
    private val folderRepository = FolderRepository(application)
    private val storageRepository = StorageRepository(application)

    private val _totalFolders = MutableLiveData(0)
    val totalFolders: LiveData<Int> = _totalFolders

    private val _totalStorages = MutableLiveData(0)
    val totalStorages: LiveData<Int> = _totalStorages

    private val _markedFolders = MutableLiveData(0)
    val markedFolders: LiveData<Int> = _markedFolders

    private val _foldersThisMonth = MutableLiveData(0)
    val foldersThisMonth: LiveData<Int> = _foldersThisMonth

    init {
        loadStatistics()
    }

    private fun loadStatistics() {
        viewModelScope.launch {
            _totalFolders.value = folderRepository.getFolderCount()
            _totalStorages.value = storageRepository.getStorageCount()
            _markedFolders.value = folderRepository.getMarkedFolderCount()

            // Calculate folders created this month
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startOfMonth = calendar.timeInMillis

            _foldersThisMonth.value = folderRepository.getFoldersCreatedAfter(startOfMonth)
        }
    }
}

class StatisticsViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StatisticsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StatisticsViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

