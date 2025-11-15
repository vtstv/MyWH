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
            val json = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    reader.readText()
                }
            } ?: return@withContext false

            val export = gson.fromJson(json, DatabaseExport::class.java)

            // Clear existing data
            database.clearAllTables()

            // Import storages first
            export.storages.forEach { storage ->
                database.storageDao().insertStorage(storage)
            }

            // Then import folders
            export.folders.forEach { folder ->
                database.folderDao().insertFolder(folder)
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun importFromMySQLDump(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val parser = MySQLDumpParser(context)
            val result = parser.parseDump(uri)

            if (!result.success) return@withContext false

            // Clear existing data
            database.clearAllTables()

            // Import parsed data
            result.storages.forEach { storage ->
                database.storageDao().insertStorage(storage)
            }

            result.folders.forEach { folder ->
                database.folderDao().insertFolder(folder)
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
