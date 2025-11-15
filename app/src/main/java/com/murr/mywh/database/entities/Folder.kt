package com.murr.mywh.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "folders",
    foreignKeys = [
        ForeignKey(
            entity = Storage::class,
            parentColumns = ["id"],
            childColumns = ["storageId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("storageId"), Index("parentFolderId")]
)
data class Folder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    var name: String,
    var description: String = "",
    var storageId: Long,
    var parentFolderId: Long? = null, // For nested folders
    var isMarked: Boolean = false,
    var createdAt: Long = System.currentTimeMillis(),
    var updatedAt: Long = System.currentTimeMillis()
)
