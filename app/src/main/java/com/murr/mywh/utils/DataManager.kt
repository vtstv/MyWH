package com.murr.mywh.utils

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.murr.mywh.database.AppDatabase
import com.murr.mywh.database.entities.Folder
import com.murr.mywh.database.entities.Storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileReader
import java.io.FileWriter

class DataManager(private val context: Context) {
    private val database = AppDatabase.getInstance(context)
    private val gson = Gson()

    data class ExportData(
        val storages: List<Storage>,
        val folders: List<Folder>,
        val exportDate: Long = System.currentTimeMillis()
    )

    suspend fun exportData(file: File): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val storages = database.storageDao().getAllStoragesOnce()
                val folders = database.folderDao().getAllFoldersOnce()

                val exportData = ExportData(
                    storages = storages,
                    folders = folders
                )

                FileWriter(file).use { writer ->
                    gson.toJson(exportData, writer)
                }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    suspend fun importData(file: File): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // Force log critical import operations
                DebugLogger.log("=== IMPORT STARTED ===", force = true)
                DebugLogger.log("File: ${file.absolutePath}", force = true)
                DebugLogger.log("File exists: ${file.exists()}", force = true)
                DebugLogger.log("File size: ${file.length()} bytes", force = true)
                
                if (!file.exists()) {
                    DebugLogger.log("ERROR: File does not exist!", force = true)
                    return@withContext false
                }
                
                if (file.length() == 0L) {
                    DebugLogger.log("ERROR: File is empty!", force = true)
                    return@withContext false
                }
                
                // Read and log first 200 characters of file content
                val previewContent = file.readText().take(200)
                DebugLogger.log("File preview: $previewContent", force = true)
                
                FileReader(file).use { reader ->
                    val type = object : TypeToken<ExportData>() {}.type
                    val importData: ExportData = gson.fromJson(reader, type)

                    DebugLogger.log("Parsed JSON: ${importData.storages.size} storages, ${importData.folders.size} folders", force = true)
                    Log.d("DataManager", "Importing ${importData.storages.size} storages and ${importData.folders.size} folders")

                    // Очищаем текущие данные
                    DebugLogger.log("Clearing database...", force = true)
                    database.clearAllTables()
                    DebugLogger.log("Database cleared", force = true)

                    // Используем raw query для вставки с сохранением ID
                    database.runInTransaction {
                        DebugLogger.log("Transaction started", force = true)
                        // Insert storages with original IDs
                        importData.storages.forEach { storage ->
                            try {
                                database.openHelper.writableDatabase.execSQL(
                                    "INSERT INTO storages (id, name, description, createdAt) VALUES (?, ?, ?, ?)",
                                    arrayOf(storage.id, storage.name, storage.description, storage.createdAt)
                                )
                                DebugLogger.log("✓ Storage inserted: ${storage.name} (ID: ${storage.id})")
                                Log.d("DataManager", "Inserted storage: ${storage.name} with ID ${storage.id}")
                            } catch (e: Exception) {
                                DebugLogger.log("✗ FAILED inserting storage: ${storage.name}", e, force = true)
                                throw e
                            }
                        }

                        // Insert folders with original IDs
                        importData.folders.forEach { folder ->
                            try {
                                database.openHelper.writableDatabase.execSQL(
                                    "INSERT INTO folders (id, name, description, storageId, parentFolderId, isMarked, createdAt, updatedAt) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                                    arrayOf(
                                        folder.id,
                                        folder.name,
                                        folder.description,
                                        folder.storageId,
                                        folder.parentFolderId,
                                        if (folder.isMarked) 1 else 0,
                                        folder.createdAt,
                                        folder.updatedAt
                                    )
                                )
                                DebugLogger.log("✓ Folder inserted: ${folder.name} (ID: ${folder.id}, Storage: ${folder.storageId})")
                                Log.d("DataManager", "Inserted folder: ${folder.name} with ID ${folder.id}")
                            } catch (e: Exception) {
                                DebugLogger.log("✗ FAILED inserting folder: ${folder.name} (storageId: ${folder.storageId})", e, force = true)
                                throw e
                            }
                        }
                        DebugLogger.log("Transaction committing...", force = true)
                    }
                }
                DebugLogger.log("=== IMPORT SUCCESSFUL ===", force = true)
                true
            } catch (e: Exception) {
                DebugLogger.log("=== IMPORT FAILED ===", e, force = true)
                Log.e("DataManager", "Error importing data", e)
                e.printStackTrace()
                false
            }
        }
    }
}

