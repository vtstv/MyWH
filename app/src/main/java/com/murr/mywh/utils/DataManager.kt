package com.murr.mywh.utils

import android.content.Context
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
                FileReader(file).use { reader ->
                    val type = object : TypeToken<ExportData>() {}.type
                    val importData: ExportData = gson.fromJson(reader, type)

                    // Очищаем текущие данные
                    database.clearAllTables()

                    // Импортируем новые данные
                    importData.storages.forEach { storage ->
                        database.storageDao().insertStorage(storage)
                    }

                    importData.folders.forEach { folder ->
                        database.folderDao().insertFolder(folder)
                    }
                }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
}

