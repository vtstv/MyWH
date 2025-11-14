package com.murr.mywh.utils

import android.content.Context
import android.net.Uri
import com.murr.mywh.database.entities.Folder
import com.murr.mywh.database.entities.Storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*

class MySQLDumpParser(private val context: Context) {

    data class ImportResult(
        val storages: List<Storage>,
        val folders: List<Folder>,
        val success: Boolean,
        val errorMessage: String? = null
    )

    suspend fun parseDump(uri: Uri): ImportResult = withContext(Dispatchers.IO) {
        try {
            val storages = mutableListOf<Storage>()
            val folders = mutableListOf<Folder>()

            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    var line: String?
                    var currentTable: String? = null
                    val insertPattern = Regex("""INSERT INTO `(\w+)`.*?VALUES""")

                    while (reader.readLine().also { line = it } != null) {
                        line?.let { currentLine ->
                            // Detect which table we're inserting into
                            val matchResult = insertPattern.find(currentLine)
                            if (matchResult != null) {
                                currentTable = matchResult.groupValues[1]
                            }

                            // Parse INSERT statements
                            when (currentTable) {
                                "storages" -> parseStoragesInsert(currentLine)?.let { storages.add(it) }
                                "folders" -> parseFoldersInsert(currentLine)?.let {
                                    if (it.second) { // Only if is_active = 1
                                        folders.add(it.first)
                                    }
                                }
                                else -> { /* Ignore other tables */ }
                            }
                        }
                    }
                }
            }

            ImportResult(
                storages = storages,
                folders = folders,
                success = true
            )
        } catch (e: Exception) {
            e.printStackTrace()
            ImportResult(
                storages = emptyList(),
                folders = emptyList(),
                success = false,
                errorMessage = e.message
            )
        }
    }

    private fun parseStoragesInsert(line: String): Storage? {
        // Pattern: (id, name, address, description, is_active, created_at, updated_at)
        val pattern = Regex("""\((\d+),\s*'([^']*)',\s*'([^']*)',\s*'([^']*)',\s*(\d+),\s*'([^']*)',\s*'([^']*)'\)""")
        val match = pattern.find(line) ?: return null

        val id = match.groupValues[1].toLongOrNull() ?: return null
        val name = match.groupValues[2].unescapeSQL()
        val address = match.groupValues[3].unescapeSQL()
        val description = match.groupValues[4].unescapeSQL()
        val isActive = match.groupValues[5] == "1"
        val createdAt = parseMySQLTimestamp(match.groupValues[6])

        // Only import active storages
        if (!isActive) return null

        return Storage(
            id = id,
            name = name,
            description = "$address${if (description.isNotEmpty()) "\n\n$description" else ""}"
        )
    }

    private fun parseFoldersInsert(line: String): Pair<Folder, Boolean>? {
        // Pattern: (id, storage_id, name, description, is_active, created_by, created_at, updated_at)
        val pattern = Regex("""\((\d+),\s*(NULL|\d+),\s*'([^']*)',\s*'([^']*)',\s*(\d+),\s*(?:NULL|\d+),\s*'([^']*)',\s*'([^']*)'\)""")
        val match = pattern.find(line) ?: return null

        val id = match.groupValues[1].toLongOrNull() ?: return null
        val storageIdStr = match.groupValues[2]
        val storageId = if (storageIdStr == "NULL") 1L else storageIdStr.toLongOrNull() ?: 1L
        val name = match.groupValues[3].unescapeSQL()
        val description = match.groupValues[4].unescapeSQL()
        val isActive = match.groupValues[5] == "1"
        val createdAt = parseMySQLTimestamp(match.groupValues[6])

        val folder = Folder(
            id = id,
            storageId = storageId,
            name = name,
            description = description,
            createdAt = createdAt,
            updatedAt = System.currentTimeMillis()
        )

        return Pair(folder, isActive)
    }

    private fun String.unescapeSQL(): String {
        return this
            .replace("\\'", "'")
            .replace("\\\"", "\"")
            .replace("\\\\", "\\")
            .replace("\\r\\n", "\n")
            .replace("\\n", "\n")
            .replace("\\r", "\r")
            .replace("\\t", "\t")
    }

    private fun parseMySQLTimestamp(timestamp: String): Long {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            format.parse(timestamp)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }
}

