package com.murr.mywh.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.murr.mywh.BuildConfig
import com.murr.mywh.MainActivity
import com.murr.mywh.R
import com.murr.mywh.ui.navigation.Screen
import com.murr.mywh.utils.PasswordManager

@Composable
fun AppDrawer(
    currentRoute: String,
    navController: NavController,
    closeDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showAboutDialog by remember { mutableStateOf(false) }
    val passwordManager = remember { PasswordManager(context) }

    ModalDrawerSheet(modifier = modifier) {
        Spacer(Modifier.height(12.dp))

        // App Title and Version
        NavigationDrawerItem(
            label = {
                Text(
                    text = "MyWH v1.2",
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

        // Spacer equivalent to one button height (48dp + padding)
        Spacer(modifier = Modifier.height(56.dp))

        // Lock (only show if password protection is enabled)
        if (passwordManager.hasPassword() && passwordManager.isPasswordEnabled) {
            NavigationDrawerItem(
                icon = { Icon(Icons.Default.Lock, contentDescription = null) },
                label = { Text(stringResource(R.string.nav_lock)) },
                selected = false,
                onClick = {
                    passwordManager.resetUnlockTime()
                    closeDrawer()
                    // The app will re-check and show lock screen
                    (context as? MainActivity)?.recreate()
                },
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }

        Spacer(Modifier.weight(1f))

        // About
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Info, contentDescription = null) },
            label = { Text(stringResource(R.string.about)) },
            selected = false,
            onClick = {
                showAboutDialog = true
                closeDrawer()
            },
            modifier = Modifier.padding(horizontal = 12.dp)
        )
    }

    // About Dialog
    if (showAboutDialog) {
        AboutDialog(onDismiss = { showAboutDialog = false })
    }
}

// Preview version without NavController dependency
@Composable
fun AppDrawerPreviewContent(selectedRoute: String = "home") {
    ModalDrawerSheet {
        Spacer(Modifier.height(12.dp))

        // App Title and Version
        NavigationDrawerItem(
            label = {
                Text(
                    text = "MyWH v${BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            selected = false,
            onClick = { },
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Home
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text("Home") },
            selected = selectedRoute == "home",
            onClick = {},
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        // Folders
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Folder, contentDescription = null) },
            label = { Text("Folders") },
            selected = selectedRoute == "folders",
            onClick = {},
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        // Storages
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Store, contentDescription = null) },
            label = { Text("Storages") },
            selected = selectedRoute == "storages",
            onClick = {},
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        // Favorites
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Favorite, contentDescription = null) },
            label = { Text("Favorites") },
            selected = selectedRoute == "favorites",
            onClick = {},
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        // Statistics
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.BarChart, contentDescription = null) },
            label = { Text("Statistics") },
            selected = selectedRoute == "statistics",
            onClick = {},
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Settings
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
            label = { Text("Settings") },
            selected = selectedRoute == "settings",
            onClick = {},
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.height(56.dp))

        Spacer(Modifier.weight(1f))

        // About
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Info, contentDescription = null) },
            label = { Text("About") },
            selected = false,
            onClick = {},
            modifier = Modifier.padding(horizontal = 12.dp)
        )
    }
}

@Preview(showBackground = true, widthDp = 300, heightDp = 600)
@Composable
fun AppDrawerPreview() {
    MaterialTheme {
        AppDrawerPreviewContent(selectedRoute = "home")
    }
}

@Preview(showBackground = true, widthDp = 300, heightDp = 600)
@Composable
fun AppDrawerStoragesSelectedPreview() {
    MaterialTheme {
        AppDrawerPreviewContent(selectedRoute = "storages")
    }
}

@Preview(
    showBackground = true,
    widthDp = 300,
    heightDp = 600,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun AppDrawerDarkPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        AppDrawerPreviewContent(selectedRoute = "favorites")
    }
}
