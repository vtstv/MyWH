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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.murr.mywh.R
import com.murr.mywh.database.entities.Folder
import com.murr.mywh.database.entities.Storage
import com.murr.mywh.ui.components.FolderCard
import com.murr.mywh.ui.components.StorageCard
import com.murr.mywh.ui.navigation.Screen
import com.murr.mywh.viewmodels.HomeViewModel
import com.murr.mywh.viewmodels.HomeViewModelFactory
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    searchQuery: String = "",
    viewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(LocalContext.current.applicationContext as Application)
    )
) {
    val recentFolders by viewModel.recentFolders.observeAsState(emptyList())
    val storages by viewModel.storages.observeAsState(emptyList())
    val storageMap by viewModel.storageMap.observeAsState(emptyMap())
    val searchResults by viewModel.searchResults.observeAsState(emptyList())

    var showAddFolderDialog by remember { mutableStateOf(false) }
    var selectedFolders by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showMoveDialog by remember { mutableStateOf(false) }
    val isSelectionMode = selectedFolders.isNotEmpty()

    val scope = rememberCoroutineScope()

    // Update search when query changes
    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotEmpty()) {
            viewModel.search(searchQuery)
        }
    }

    val displayFolders = if (searchQuery.isNotEmpty()) searchResults else recentFolders

    Column(modifier = Modifier.fillMaxSize()) {
        // Selection Mode Actions Bar
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
                    TextButton(onClick = { selectedFolders = emptySet() }) {
                        Text(stringResource(R.string.cancel))
                    }

                    Text(
                        text = "${selectedFolders.size} ${stringResource(R.string.selected)}",
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

        Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
        ) {
            // Recent Folders Section
            item {
                Text(
                    text = if (searchQuery.isNotEmpty())
                        stringResource(R.string.search_folders)
                    else
                        stringResource(R.string.recent_folders),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            if (displayFolders.isEmpty()) {
                item {
                    Text(
                        text = if (searchQuery.isNotEmpty())
                            stringResource(R.string.no_results)
                        else
                            stringResource(R.string.no_folders),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            } else {
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

            // Warehouses Section
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.warehouses),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            if (storages.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.no_storages),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            } else {
                items(storages) { storage ->
                    StorageCard(
                        storage = storage,
                        onClick = {
                            navController.navigate(Screen.StorageDetail.createRoute(storage.id))
                        }
                    )
                }
            }
        }

        // FAB
        FloatingActionButton(
            onClick = { showAddFolderDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .padding(bottom = 64.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_folder))
        }
        }
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
                        scope.launch {
                            selectedFolders.forEach { folderId ->
                                displayFolders.find { it.id == folderId }?.let { folder ->
                                    viewModel.deleteFolder(folder)
                                }
                            }
                            selectedFolders = emptySet()
                            showDeleteDialog = false
                        }
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

    // Move Dialog
    if (showMoveDialog && selectedFolders.isNotEmpty()) {
        BatchMoveDialog(
            storages = storages,
            onDismiss = { showMoveDialog = false },
            onConfirm = { storageId ->
                scope.launch {
                    selectedFolders.forEach { folderId ->
                        displayFolders.find { it.id == folderId }?.let { folder ->
                            viewModel.moveFolder(folder, storageId)
                        }
                    }
                    selectedFolders = emptySet()
                    showMoveDialog = false
                }
            }
        )
    }

    if (showAddFolderDialog) {
        AddFolderDialog(
            storages = storages,
            onDismiss = { showAddFolderDialog = false },
            onConfirm = { name, description, storageId ->
                viewModel.addFolder(name, description, storageId)
                showAddFolderDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFolderDialog(
    storages: List<Storage>,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Long) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedStorageId by remember { mutableStateOf(storages.firstOrNull()?.id ?: 0L) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_folder)) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Storage selector FIRST (as requested)
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

                // THEN folder name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.folder_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // THEN description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.description)) },
                    minLines = 5,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank() && selectedStorageId != 0L) {
                        onConfirm(name, description, selectedStorageId)
                    }
                },
                enabled = name.isNotBlank() && selectedStorageId != 0L
            ) {
                Text(stringResource(R.string.add))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

