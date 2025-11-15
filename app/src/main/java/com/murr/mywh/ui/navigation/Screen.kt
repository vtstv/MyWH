package com.murr.mywh.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object AllFolders : Screen("all_folders")
    object Storages : Screen("storages")
    object Favorites : Screen("favorites")
    object Statistics : Screen("statistics")
    object Settings : Screen("settings")
    object DebugLogs : Screen("debug_logs")
    object FolderDetail : Screen("folder_detail/{storageId}/{folderId}") {
        fun createRoute(storageId: Long, folderId: Long) = "folder_detail/$storageId/$folderId"
    }
    object StorageDetail : Screen("storage_detail/{storageId}") {
        fun createRoute(storageId: Long) = "storage_detail/$storageId"
    }
}

