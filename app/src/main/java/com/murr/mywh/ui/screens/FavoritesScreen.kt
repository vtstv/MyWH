package com.murr.mywh.ui.screens

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.murr.mywh.R
import com.murr.mywh.database.entities.Folder
import com.murr.mywh.ui.components.FolderCard
import com.murr.mywh.ui.navigation.Screen
import com.murr.mywh.viewmodels.FavoritesViewModel
import com.murr.mywh.viewmodels.FavoritesViewModelFactory

@Composable
fun FavoritesScreen(
    navController: NavController,
    searchQuery: String = "",
    viewModel: FavoritesViewModel = viewModel(
        factory = FavoritesViewModelFactory(LocalContext.current.applicationContext as Application)
    )
) {
    val folders by viewModel.markedFolders.observeAsState(emptyList())
    val storageMap by viewModel.storageMap.observeAsState(emptyMap())
    val searchResults by viewModel.searchResults.observeAsState(emptyList())

    var selectedFolders by remember { mutableStateOf<Set<Long>>(emptySet()) }
    val isSelectionMode = selectedFolders.isNotEmpty()

    // Update search when query changes
    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotEmpty()) {
            viewModel.search(searchQuery)
        }
    }

    val displayFolders = if (searchQuery.isNotEmpty()) searchResults else folders

    if (displayFolders.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.no_favorites),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            items(displayFolders, key = { it.id }) { folder ->
                FolderCard(
                    folder = folder,
                    storageName = storageMap[folder.storageId] ?: "",
                    onFolderClick = {
                        if (isSelectionMode) {
                            selectedFolders = if (selectedFolders.contains(folder.id)) {
                                selectedFolders - folder.id
                            } else {
                                selectedFolders + folder.id
                            }
                        } else {
                            navController.navigate(
                                Screen.FolderDetail.createRoute(folder.storageId, folder.id)
                            )
                        }
                    },
                    onFavoriteClick = {
                        viewModel.toggleFolderMarked(folder)
                    },
                    onLongClick = {
                        selectedFolders = selectedFolders + folder.id
                    },
                    isSelected = selectedFolders.contains(folder.id)
                )
            }
        }
    }
}

// Preview functions
@Preview(showBackground = true, name = "Favorites Content")
@Composable
fun FavoritesScreenContentPreview() {
    MaterialTheme {
        Surface {
            val favoriteFolders = listOf(
                Folder(
                    id = 1,
                    name = "Important Documents",
                    description = "Critical business documents",
                    storageId = 1,
                    isMarked = true,
                    createdAt = System.currentTimeMillis() - 86400000,
                    updatedAt = System.currentTimeMillis()
                ),
                Folder(
                    id = 2,
                    name = "Frequently Used",
                    description = "Most accessed items",
                    storageId = 2,
                    isMarked = true
                )
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
            ) {
                items(favoriteFolders) { folder ->
                    FolderCard(
                        folder = folder,
                        storageName = "Main Storage",
                        onFolderClick = {},
                        onFavoriteClick = {}
                    )
                }
            }
        }
    }
}
