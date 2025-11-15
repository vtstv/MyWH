package com.murr.mywh.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.murr.mywh.R
import com.murr.mywh.ui.navigation.Screen

@Composable
fun AppDrawer(
    currentRoute: String,
    navController: NavController,
    closeDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    ModalDrawerSheet(modifier = modifier) {
        Spacer(Modifier.height(12.dp))

        // App Title and Version
        NavigationDrawerItem(
            label = {
                Text(
                    text = "MyWH v1.0",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            selected = false,
            onClick = { },
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // Home
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_home)) },
            selected = currentRoute == Screen.Home.route,
            onClick = {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Home.route) { inclusive = true }
                    launchSingleTop = true
                }
                closeDrawer()
            },
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        // Folders
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Folder, contentDescription = null) },
            label = { Text(stringResource(R.string.all_folders)) },
            selected = currentRoute == Screen.AllFolders.route,
            onClick = {
                navController.navigate(Screen.AllFolders.route) {
                    launchSingleTop = true
                }
                closeDrawer()
            },
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        // Storages
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Store, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_storages)) },
            selected = currentRoute == Screen.Storages.route,
            onClick = {
                navController.navigate(Screen.Storages.route) {
                    launchSingleTop = true
                }
                closeDrawer()
            },
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        // Favorites
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Favorite, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_favorites)) },
            selected = currentRoute == Screen.Favorites.route,
            onClick = {
                navController.navigate(Screen.Favorites.route) {
                    launchSingleTop = true
                }
                closeDrawer()
            },
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        // Statistics
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.BarChart, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_statistics)) },
            selected = currentRoute == Screen.Statistics.route,
            onClick = {
                navController.navigate(Screen.Statistics.route) {
                    launchSingleTop = true
                }
                closeDrawer()
            },
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // Settings
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_settings)) },
            selected = currentRoute == Screen.Settings.route,
            onClick = {
                navController.navigate(Screen.Settings.route) {
                    launchSingleTop = true
                }
                closeDrawer()
            },
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(Modifier.weight(1f))

        // About
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Info, contentDescription = null) },
            label = { Text(stringResource(R.string.about)) },
            selected = false,
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/vtstv/MyWH"))
                context.startActivity(intent)
                closeDrawer()
            },
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(Modifier.height(12.dp))
    }
}

