package com.murr.mywh.database.dao

import androidx.room.*
import com.murr.mywh.database.entities.Storage
import kotlinx.coroutines.flow.Flow

@Dao
interface StorageDao {
    @Query("SELECT * FROM storages ORDER BY name ASC")
    fun getAllStorages(): Flow<List<Storage>>

    @Query("SELECT * FROM storages ORDER BY name ASC")
    suspend fun getAllStoragesOnce(): List<Storage>

    @Query("SELECT * FROM storages WHERE id = :storageId")
    suspend fun getStorageById(storageId: Long): Storage?

    @Insert
    suspend fun insertStorage(storage: Storage): Long

    @Update
    suspend fun updateStorage(storage: Storage)

    @Delete
    suspend fun deleteStorage(storage: Storage)

    @Query("SELECT COUNT(*) FROM storages")
    suspend fun getStorageCount(): Int
}
