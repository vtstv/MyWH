package com.murr.mywh.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "storages")
data class Storage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    var name: String,
    var description: String = "",
    var createdAt: Long = System.currentTimeMillis()
)
