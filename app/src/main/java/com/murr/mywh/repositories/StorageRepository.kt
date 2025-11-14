package com.murr.mywh.repositories

import android.app.Application
import com.murr.mywh.database.AppDatabase
import com.murr.mywh.database.entities.Storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class StorageRepository(application: Application) {
    private val storageDao = AppDatabase.getInstance(application).storageDao()

    fun getAllStorages(): Flow<List<Storage>> = storageDao.getAllStorages()

    suspend fun getStorageById(id: Long): Storage? = withContext(Dispatchers.IO) {
        storageDao.getStorageById(id)
    }

    suspend fun insertStorage(storage: Storage): Long = withContext(Dispatchers.IO) {
        storageDao.insertStorage(storage)
    }

    suspend fun updateStorage(storage: Storage) = withContext(Dispatchers.IO) {
        storageDao.updateStorage(storage)
    }

    suspend fun deleteStorage(storage: Storage) = withContext(Dispatchers.IO) {
        storageDao.deleteStorage(storage)
    }

    suspend fun getStorageCount(): Int = withContext(Dispatchers.IO) {
        storageDao.getStorageCount()
    }
}
