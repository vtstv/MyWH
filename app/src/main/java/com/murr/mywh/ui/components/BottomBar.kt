package com.murr.mywh.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.murr.mywh.R
import com.murr.mywh.ui.navigation.Screen

@Composable
fun BottomBar(navController: NavController) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_home)) },
            selected = currentRoute == Screen.Home.route,
            onClick = {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Home.route) { inclusive = true }
                    launchSingleTop = true
                }
            }
        )

        NavigationBarItem(
            icon = { Icon(Icons.Default.Folder, contentDescription = null) },
            label = { Text(stringResource(R.string.folders)) },
            selected = currentRoute == Screen.AllFolders.route,
            onClick = {
                navController.navigate(Screen.AllFolders.route) {
                    launchSingleTop = true
                }
            }
        )

        NavigationBarItem(
            icon = { Icon(Icons.Default.Store, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_storages)) },
            selected = currentRoute == Screen.Storages.route,
            onClick = {
                navController.navigate(Screen.Storages.route) {
                    launchSingleTop = true
                }
            }
        )

        NavigationBarItem(
            icon = { Icon(Icons.Default.Favorite, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_favorites)) },
            selected = currentRoute == Screen.Favorites.route,
            onClick = {
                navController.navigate(Screen.Favorites.route) {
                    launchSingleTop = true
                }
            }
        )
    }
}

// Preview version without NavController dependency
@Composable
fun BottomBarPreviewContent(selectedIndex: Int = 0) {
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text("Home") },
            selected = selectedIndex == 0,
            onClick = {}
        )

        NavigationBarItem(
            icon = { Icon(Icons.Default.Folder, contentDescription = null) },
            label = { Text("Folders") },
            selected = selectedIndex == 1,
            onClick = {}
        )

        NavigationBarItem(
            icon = { Icon(Icons.Default.Store, contentDescription = null) },
            label = { Text("Storages") },
            selected = selectedIndex == 2,
            onClick = {}
        )

        NavigationBarItem(
            icon = { Icon(Icons.Default.Favorite, contentDescription = null) },
            label = { Text("Favorites") },
            selected = selectedIndex == 3,
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BottomBarPreview() {
    MaterialTheme {
        BottomBarPreviewContent(selectedIndex = 0)
    }
}

@Preview(showBackground = true)
@Composable
fun BottomBarFavoritesSelectedPreview() {
    MaterialTheme {
        BottomBarPreviewContent(selectedIndex = 3)
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun BottomBarDarkPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        BottomBarPreviewContent(selectedIndex = 1)
    }
}
