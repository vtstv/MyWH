package com.murr.mywh

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.murr.mywh.ui.components.AppDrawer
import com.murr.mywh.ui.components.BottomBar
import com.murr.mywh.ui.navigation.NavGraph
import com.murr.mywh.ui.navigation.Screen
import com.murr.mywh.ui.theme.MyWHTheme
import com.murr.mywh.utils.PreferencesManager
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        preferencesManager = PreferencesManager(this)
        preferencesManager.applyLanguage()

        enableEdgeToEdge()

        setContent {
            val initialDarkTheme = remember { preferencesManager.isDarkTheme }
            val initialFontScale = remember { preferencesManager.fontScale }
            var isDarkTheme by remember { mutableStateOf(initialDarkTheme) }
            var fontScale by remember { mutableStateOf(initialFontScale) }
            var shouldRecreate by remember { mutableStateOf(false) }

            MyWHTheme(
                darkTheme = isDarkTheme,
                fontScale = fontScale
            ) {
                MyWHApp(
                    onThemeChanged = {
                        // Reload font scale in case it changed
                        fontScale = preferencesManager.fontScale
                        isDarkTheme = if (isDarkTheme) false else true
                        preferencesManager.isDarkTheme = if (preferencesManager.isDarkTheme) false else true
                    },
                    onLanguageChanged = {
                        shouldRecreate = true
                    }
                )
            }

            if (shouldRecreate) {
                LaunchedEffect(Unit) {
                    recreate()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyWHApp(
    onThemeChanged: () -> Unit,
    onLanguageChanged: () -> Unit
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route ?: Screen.Home.route

    // Determine if we should show bottom bar and search
    val showBottomBar = remember(currentRoute) {
        // Show bottom bar on all screens except specific detail views
        !currentRoute.startsWith("storage_detail")
    }

    val showSearch = remember(currentRoute) {
        currentRoute in listOf(
            Screen.Home.route,
            Screen.AllFolders.route,
            Screen.Favorites.route
        )
    }

    var searchQuery by remember { mutableStateOf("") }

    // Clear search when route changes (except for search-enabled screens)
    LaunchedEffect(currentRoute) {
        if (!showSearch) {
            searchQuery = ""
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                currentRoute = currentRoute,
                navController = navController,
                closeDrawer = { scope.launch { drawerState.close() } }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        if (showSearch) {
                            TextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text(stringResource(R.string.search_hint)) },
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                    focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                                    unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            Text(getCurrentScreenTitle(currentRoute))
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    }
                )
            },
            bottomBar = {
                if (showBottomBar) {
                    BottomBar(navController = navController)
                }
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                NavGraph(
                    navController = navController,
                    searchQuery = searchQuery,
                    onThemeChanged = onThemeChanged,
                    onLanguageChanged = onLanguageChanged
                )
            }
        }
    }
}

@Composable
fun getCurrentScreenTitle(route: String): String {
    return when {
        route == Screen.Home.route -> stringResource(R.string.app_name)
        route == Screen.AllFolders.route -> stringResource(R.string.all_folders)
        route == Screen.Storages.route -> stringResource(R.string.nav_storages)
        route == Screen.Favorites.route -> stringResource(R.string.nav_favorites)
        route == Screen.Statistics.route -> stringResource(R.string.nav_statistics)
        route == Screen.Settings.route -> stringResource(R.string.nav_settings)
        route.startsWith("folder_detail") -> stringResource(R.string.folder_details)
        route.startsWith("storage_detail") -> stringResource(R.string.storage_details)
        else -> stringResource(R.string.app_name)
    }
}

