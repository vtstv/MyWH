package com.murr.mywh.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.murr.mywh.data.Statistics
import com.murr.mywh.repositories.StorageRepository
import com.murr.mywh.repositories.FolderRepository
import kotlinx.coroutines.launch

class StatisticsViewModel(application: Application) : AndroidViewModel(application) {
    private val storageRepository = StorageRepository(application)
    private val folderRepository = FolderRepository(application)

    private val _statistics = MutableLiveData<Statistics>()
    val statistics: LiveData<Statistics> = _statistics

    init {
        loadStatistics()
    }

    fun loadStatistics() {
        viewModelScope.launch {
            val storageCount = storageRepository.getStorageCount()
            val folderCount = folderRepository.getFolderCount()
            val markedCount = folderRepository.getMarkedFolderCount()

            _statistics.value = Statistics(
                totalStorages = storageCount,
                totalFolders = folderCount,
                totalProducts = folderCount, // Since products are now folders
                markedFolders = markedCount,
                markedProducts = markedCount
            )
        }
    }
}
