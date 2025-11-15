package com.murr.mywh.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.murr.mywh.ui.screens.AllFoldersScreen
import com.murr.mywh.ui.screens.FavoritesScreen
import com.murr.mywh.ui.screens.FolderDetailScreen
import com.murr.mywh.ui.screens.HomeScreen
import com.murr.mywh.ui.screens.SettingsScreen
import com.murr.mywh.ui.screens.StatisticsScreen
import com.murr.mywh.ui.screens.StorageDetailScreen
import com.murr.mywh.ui.screens.StoragesScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    searchQuery: String = "",
    onThemeChanged: () -> Unit,
    onLanguageChanged: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                navController = navController,
                searchQuery = searchQuery
            )
        }

        composable(Screen.AllFolders.route) {
            AllFoldersScreen(
                navController = navController,
                searchQuery = searchQuery
            )
        }

        composable(Screen.Storages.route) {
            StoragesScreen(navController = navController)
        }

        composable(Screen.Favorites.route) {
            FavoritesScreen(
                navController = navController,
                searchQuery = searchQuery
            )
        }

        composable(Screen.Statistics.route) {
            StatisticsScreen(navController = navController)
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                navController = navController,
                onThemeChanged = onThemeChanged,
                onLanguageChanged = onLanguageChanged
            )
        }

        composable(
            route = Screen.FolderDetail.route,
            arguments = listOf(
                navArgument("storageId") { type = NavType.LongType },
                navArgument("folderId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val storageId = backStackEntry.arguments?.getLong("storageId") ?: 0L
            val folderId = backStackEntry.arguments?.getLong("folderId") ?: 0L
            FolderDetailScreen(
                navController = navController,
                storageId = storageId,
                folderId = folderId
            )
        }

        composable(
            route = Screen.StorageDetail.route,
            arguments = listOf(
                navArgument("storageId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val storageId = backStackEntry.arguments?.getLong("storageId") ?: 0L
            StorageDetailScreen(
                navController = navController,
                storageId = storageId
            )
        }
    }
}

