package com.murr.mywh.database.dao

import androidx.room.*
import com.murr.mywh.database.entities.Folder
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderDao {
    @Query("SELECT * FROM folders WHERE storageId = :storageId AND parentFolderId IS NULL ORDER BY createdAt DESC")
    fun getRootFoldersByStorage(storageId: Long): Flow<List<Folder>>

    @Query("SELECT * FROM folders WHERE parentFolderId = :parentId ORDER BY createdAt DESC")
    fun getSubFolders(parentId: Long): Flow<List<Folder>>

    @Query("SELECT * FROM folders WHERE storageId = :storageId ORDER BY createdAt DESC LIMIT :limit OFFSET :offset")
    fun getFoldersByStorage(storageId: Long, limit: Int, offset: Int): Flow<List<Folder>>

    @Query("SELECT * FROM folders ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentFolders(limit: Int): Flow<List<Folder>>

    @Query("SELECT * FROM folders ORDER BY createdAt DESC")
    suspend fun getAllFoldersOnce(): List<Folder>

    @Query("SELECT * FROM folders WHERE isMarked = 1 ORDER BY name ASC")
    fun getMarkedFolders(): Flow<List<Folder>>

    @Query("SELECT * FROM folders WHERE name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    fun searchFolders(query: String): Flow<List<Folder>>

    @Query("SELECT * FROM folders WHERE id = :id")
    suspend fun getFolderById(id: Long): Folder?

    @Query("SELECT * FROM folders WHERE id = :id")
    fun getFolderByIdFlow(id: Long): Flow<Folder?>

    @Insert
    suspend fun insertFolder(folder: Folder): Long

    @Update
    suspend fun updateFolder(folder: Folder)

    @Delete
    suspend fun deleteFolder(folder: Folder)

    @Query("DELETE FROM folders WHERE id = :id")
    suspend fun deleteFolderById(id: Long)

    @Query("SELECT COUNT(*) FROM folders")
    suspend fun getFolderCount(): Int

    @Query("SELECT COUNT(*) FROM folders WHERE isMarked = 1")
    suspend fun getMarkedFolderCount(): Int

    @Query("SELECT COUNT(*) FROM folders WHERE storageId = :storageId")
    suspend fun getFolderCountByStorage(storageId: Long): Int

    @Query("SELECT * FROM folders ORDER BY createdAt DESC LIMIT :limit OFFSET :offset")
    fun getAllFoldersPaginated(limit: Int, offset: Int): Flow<List<Folder>>

    @Query("UPDATE folders SET parentFolderId = :newParentId, updatedAt = :updatedAt WHERE id = :folderId")
    suspend fun moveFolder(folderId: Long, newParentId: Long?, updatedAt: Long)

    @Transaction
    suspend fun copyFolder(sourceId: Long, targetParentId: Long?): Long {
        val source = getFolderById(sourceId) ?: return -1
        val copy = source.copy(
            id = 0,
            parentFolderId = targetParentId,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        return insertFolder(copy)
    }
}

