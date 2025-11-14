package com.murr.mywh.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    var name: String,
    var description: String,
    var folderId: Long,
    var isMarked: Boolean = false
)
