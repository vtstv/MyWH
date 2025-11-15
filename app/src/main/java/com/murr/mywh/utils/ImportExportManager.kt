package com.murr.mywh.utils

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.murr.mywh.database.AppDatabase
import com.murr.mywh.database.entities.Folder
import com.murr.mywh.database.entities.Storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

data class DatabaseExport(
    val storages: List<Storage>,
    val folders: List<Folder>,
    val exportDate: Long = System.currentTimeMillis()
)

class ImportExportManager(private val context: Context) {
    private val gson = Gson()
    private val database = AppDatabase.getInstance(context)

    suspend fun exportToJson(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val storages = database.storageDao().getAllStoragesOnce()
            val folders = database.folderDao().getAllFoldersOnce()

            val export = DatabaseExport(storages, folders)
            val json = gson.toJson(export)

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    writer.write(json)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun importFromJson(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            DebugLogger.log("=== IMPORT FROM JSON STARTED ===", force = true)
            DebugLogger.log("URI: $uri", force = true)
            
            val json = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    reader.readText()
                }
            } ?: run {
                DebugLogger.log("ERROR: Failed to open input stream", force = true)
                return@withContext false
            }

            DebugLogger.log("JSON length: ${json.length} chars", force = true)
            DebugLogger.log("JSON preview: ${json.take(200)}", force = true)

            val type = object : TypeToken<DatabaseExport>() {}.type
            val export: DatabaseExport = gson.fromJson(json, type)
            
            if (export == null) {
                DebugLogger.log("ERROR: Failed to parse JSON - export is null", force = true)
                return@withContext false
            }
            
            if (export.storages == null || export.folders == null) {
                DebugLogger.log("ERROR: Parsed data has null fields - storages: ${export.storages}, folders: ${export.folders}", force = true)
                return@withContext false
            }
            
            DebugLogger.log("Parsed: ${export.storages.size} storages, ${export.folders.size} folders", force = true)

            // Clear existing data
            DebugLogger.log("Clearing database...", force = true)
            database.clearAllTables()
            DebugLogger.log("Database cleared", force = true)

            // Use raw SQL to preserve IDs
            database.runInTransaction {
                DebugLogger.log("Transaction started", force = true)
                
                // Import storages with original IDs
                export.storages.forEach { storage ->
                    try {
                        database.openHelper.writableDatabase.execSQL(
                            "INSERT INTO storages (id, name, description, createdAt) VALUES (?, ?, ?, ?)",
                            arrayOf(storage.id, storage.name, storage.description, storage.createdAt)
                        )
                        DebugLogger.log("✓ Storage inserted: ${storage.name} (ID: ${storage.id})")
                    } catch (e: Exception) {
                        DebugLogger.log("✗ FAILED inserting storage: ${storage.name}", e, force = true)
                        throw e
                    }
                }

                // Import folders with original IDs
                export.folders.forEach { folder ->
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
                    } catch (e: Exception) {
                        DebugLogger.log("✗ FAILED inserting folder: ${folder.name} (storageId: ${folder.storageId})", e, force = true)
                        throw e
                    }
                }
                
                DebugLogger.log("Transaction committing...", force = true)
            }

            DebugLogger.log("=== IMPORT SUCCESSFUL ===", force = true)
            true
        } catch (e: Exception) {
            DebugLogger.log("=== IMPORT FAILED ===", e, force = true)
            e.printStackTrace()
            false
        }
    }

    suspend fun importFromMySQLDump(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            DebugLogger.log("=== IMPORT FROM MySQL DUMP STARTED ===", force = true)
            val parser = MySQLDumpParser(context)
            val result = parser.parseDump(uri)

            if (!result.success) {
                DebugLogger.log("MySQL parsing failed", force = true)
                return@withContext false
            }

            DebugLogger.log("Parsed: ${result.storages.size} storages, ${result.folders.size} folders", force = true)

            // Clear existing data
            DebugLogger.log("Clearing database...", force = true)
            database.clearAllTables()

            // Use raw SQL to preserve IDs
            database.runInTransaction {
                // Import parsed storages
                result.storages.forEach { storage ->
                    try {
                        database.openHelper.writableDatabase.execSQL(
                            "INSERT INTO storages (id, name, description, createdAt) VALUES (?, ?, ?, ?)",
                            arrayOf(storage.id, storage.name, storage.description, storage.createdAt)
                        )
                        DebugLogger.log("✓ Storage from MySQL: ${storage.name}")
                    } catch (e: Exception) {
                        DebugLogger.log("✗ FAILED MySQL storage: ${storage.name}", e, force = true)
                        throw e
                    }
                }

                result.folders.forEach { folder ->
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
                        DebugLogger.log("✓ Folder from MySQL: ${folder.name}")
                    } catch (e: Exception) {
                        DebugLogger.log("✗ FAILED MySQL folder: ${folder.name}", e, force = true)
                        throw e
                    }
                }
            }

            DebugLogger.log("=== MySQL IMPORT SUCCESSFUL ===", force = true)
            true
        } catch (e: Exception) {
            DebugLogger.log("=== MySQL IMPORT FAILED ===", e, force = true)
            e.printStackTrace()
            false
        }
    }
}
