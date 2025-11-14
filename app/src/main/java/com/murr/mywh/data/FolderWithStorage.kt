package com.murr.mywh.data

import com.murr.mywh.database.entities.Folder

data class FolderWithStorage(
    val folder: Folder,
    val storageName: String
)

