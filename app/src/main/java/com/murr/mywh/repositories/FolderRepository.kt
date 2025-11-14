package com.murr.mywh.repositories

import android.app.Application
import com.murr.mywh.database.AppDatabase
import com.murr.mywh.database.entities.Folder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class FolderRepository(application: Application) {
    private val folderDao = AppDatabase.getInstance(application).folderDao()

    fun getRootFoldersByStorage(storageId: Long): Flow<List<Folder>> =
        folderDao.getRootFoldersByStorage(storageId)

    fun getSubFolders(parentId: Long): Flow<List<Folder>> =
        folderDao.getSubFolders(parentId)

    fun getFoldersByStorage(storageId: Long, page: Int): Flow<List<Folder>> =
        folderDao.getFoldersByStorage(storageId, ITEMS_PER_PAGE, page * ITEMS_PER_PAGE)

    fun getRecentFolders(limit: Int): Flow<List<Folder>> =
        folderDao.getRecentFolders(limit)

    fun getMarkedFolders(): Flow<List<Folder>> =
        folderDao.getMarkedFolders()

    fun getAllFoldersPaginated(limit: Int, offset: Int): Flow<List<Folder>> =
        folderDao.getAllFoldersPaginated(limit, offset)

    fun searchFolders(query: String): Flow<List<Folder>> =
        folderDao.searchFolders(query)

    suspend fun getFolderById(id: Long): Folder? = withContext(Dispatchers.IO) {
        folderDao.getFolderById(id)
    }

    suspend fun insertFolder(folder: Folder): Long = withContext(Dispatchers.IO) {
        folderDao.insertFolder(folder)
    }

    suspend fun updateFolder(folder: Folder) = withContext(Dispatchers.IO) {
        folderDao.updateFolder(folder)
    }

    suspend fun deleteFolder(folder: Folder) = withContext(Dispatchers.IO) {
        folderDao.deleteFolder(folder)
    }

    suspend fun moveFolder(folderId: Long, newParentId: Long?, updatedAt: Long) = withContext(Dispatchers.IO) {
        folderDao.moveFolder(folderId, newParentId, updatedAt)
    }

    suspend fun copyFolder(sourceId: Long, targetParentId: Long?): Long = withContext(Dispatchers.IO) {
        folderDao.copyFolder(sourceId, targetParentId)
    }

    suspend fun getFolderCount(): Int = withContext(Dispatchers.IO) {
        folderDao.getFolderCount()
    }

    suspend fun getMarkedFolderCount(): Int = withContext(Dispatchers.IO) {
        folderDao.getMarkedFolderCount()
    }

    companion object {
        const val ITEMS_PER_PAGE = 25
    }
}
