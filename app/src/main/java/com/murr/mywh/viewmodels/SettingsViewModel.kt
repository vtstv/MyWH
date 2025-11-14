package com.murr.mywh.viewmodels

import android.app.Application
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.murr.mywh.R
import com.murr.mywh.repositories.FolderRepository
import com.murr.mywh.repositories.StorageRepository
import com.murr.mywh.utils.DataManager
import com.murr.mywh.utils.MySQLDumpParser
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val dataManager = DataManager(application)
    private val mySQLDumpParser = MySQLDumpParser(application)
    private val storageRepository = StorageRepository(application)
    private val folderRepository = FolderRepository(application)

    fun exportData(context: Context) = viewModelScope.launch {
        val file = File(context.getExternalFilesDir(null), "mywh_export_${System.currentTimeMillis()}.json")
        val success = dataManager.exportData(file)

        val message = if (success) {
            context.getString(R.string.export_success) + ": ${file.absolutePath}"
        } else {
            context.getString(R.string.export_error)
        }

        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    fun exportDataToUri(context: Context, uri: Uri) = viewModelScope.launch {
        try {
            val tempFile = File(context.cacheDir, "export_temp.json")
            val success = dataManager.exportData(tempFile)

            if (success) {
                context.contentResolver.openOutputStream(uri)?.use { output ->
                    tempFile.inputStream().use { input ->
                        input.copyTo(output)
                    }
                }
                Toast.makeText(context, context.getString(R.string.export_success), Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, context.getString(R.string.export_error), Toast.LENGTH_LONG).show()
            }

            tempFile.delete()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, context.getString(R.string.export_error), Toast.LENGTH_LONG).show()
        }
    }

    fun importDataFromUri(context: Context, uri: Uri) = viewModelScope.launch {
        try {
            val tempFile = File(context.cacheDir, "import_temp.json")
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }

            val success = dataManager.importData(tempFile)
            val message = if (success) {
                context.getString(R.string.import_success)
            } else {
                context.getString(R.string.import_error)
            }

            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            tempFile.delete()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, context.getString(R.string.import_error), Toast.LENGTH_LONG).show()
        }
    }

    fun importFromMySQLDump(context: Context, uri: Uri) = viewModelScope.launch {
        try {
            // Parse MySQL dump file
            val result = mySQLDumpParser.parseDump(uri)

            if (!result.success) {
                Toast.makeText(
                    context,
                    "${context.getString(R.string.import_error)}: ${result.errorMessage}",
                    Toast.LENGTH_LONG
                ).show()
                return@launch
            }

            // Import storages first
            result.storages.forEach { storage ->
                try {
                    storageRepository.insertStorage(storage)
                } catch (e: Exception) {
                    // Storage might already exist, try to update
                    storageRepository.updateStorage(storage)
                }
            }

            // Import folders
            result.folders.forEach { folder ->
                try {
                    folderRepository.insertFolder(folder)
                } catch (e: Exception) {
                    // Folder might already exist, skip or update
                    e.printStackTrace()
                }
            }

            Toast.makeText(
                context,
                "${context.getString(R.string.import_success)}\n" +
                "${result.storages.size} storages, ${result.folders.size} folders",
                Toast.LENGTH_LONG
            ).show()

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "${context.getString(R.string.import_error)}: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
