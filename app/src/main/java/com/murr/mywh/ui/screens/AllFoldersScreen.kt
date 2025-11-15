package com.murr.mywh.ui.screens

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.murr.mywh.R
import com.murr.mywh.database.entities.Storage
import com.murr.mywh.ui.components.FolderCard
import com.murr.mywh.ui.navigation.Screen
import com.murr.mywh.viewmodels.AllFoldersViewModel
import com.murr.mywh.viewmodels.AllFoldersViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllFoldersScreen(
    navController: NavController,
    searchQuery: String = "",
    viewModel: AllFoldersViewModel = viewModel(
        factory = AllFoldersViewModelFactory(LocalContext.current.applicationContext as Application)
    )
) {
    val folders by viewModel.folders.observeAsState(emptyList())
    val storageMap by viewModel.storageMap.observeAsState(emptyMap())
    val selectedFolders by viewModel.selectedFolders.observeAsState(emptySet())
    val isSelectionMode by viewModel.isSelectionMode.observeAsState(false)
    val searchResults by viewModel.searchResults.observeAsState(emptyList())
    val storages by viewModel.storages.observeAsState(emptyList())

    var showBatchDialog by remember { mutableStateOf(false) }
    var showMoveDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Update search when query changes
    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotEmpty()) {
            viewModel.search(searchQuery)
        }
    }

    val displayFolders = if (searchQuery.isNotEmpty()) searchResults else folders

    Column(modifier = Modifier.fillMaxSize()) {
        // Selection Mode Actions
        if (isSelectionMode) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 3.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = { viewModel.clearSelection() }) {
                        Text(stringResource(R.string.cancel))
                    }

                    Text(
                        text = "${selectedFolders.size} selected",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(onClick = { showMoveDialog = true }) {
                            Icon(Icons.Default.DriveFileMove, contentDescription = "Move")
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                }
            }
        }

        // Folders List
        if (displayFolders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.no_folders),
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
                                viewModel.toggleFolderSelection(folder.id)
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
                            viewModel.toggleFolderSelection(folder.id)
                        },
                        isSelected = selectedFolders.contains(folder.id)
                    )
                }

                // Load More Button
                item {
                    if (viewModel.hasMore.value == true) {
                        Button(
                            onClick = { viewModel.loadMoreFolders() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                        ) {
                            Text(stringResource(R.string.load_more))
                        }
                    }
                }
            }
        }
    }

    // Move Dialog
    if (showMoveDialog && selectedFolders.isNotEmpty()) {
        BatchMoveDialog(
            storages = storages,
            onDismiss = { showMoveDialog = false },
            onConfirm = { storageId ->
                viewModel.moveFoldersToStorage(selectedFolders.toList(), storageId)
                viewModel.clearSelection()
                showMoveDialog = false
            }
        )
    }

    // Delete Dialog
    if (showDeleteDialog && selectedFolders.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_folders)) },
            text = { Text(stringResource(R.string.delete_confirmation_multiple, selectedFolders.size)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteFolders(selectedFolders.toList())
                        viewModel.clearSelection()
                        showDeleteDialog = false
                    }
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchMoveDialog(
    storages: List<Storage>,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    var selectedStorageId by remember { mutableStateOf(storages.firstOrNull()?.id ?: 0L) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.batch_move)) },
        text = {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = storages.find { it.id == selectedStorageId }?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.select_storage)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    storages.forEach { storage ->
                        DropdownMenuItem(
                            text = { Text(storage.name) },
                            onClick = {
                                selectedStorageId = storage.id
                                expanded = false
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(selectedStorageId) },
                enabled = selectedStorageId != 0L
            ) {
                Text(stringResource(R.string.move_folder))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
