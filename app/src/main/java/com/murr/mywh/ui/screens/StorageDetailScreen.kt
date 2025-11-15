package com.murr.mywh.ui.screens

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
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
import com.murr.mywh.viewmodels.StorageDetailViewModel
import com.murr.mywh.viewmodels.StorageDetailViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageDetailScreen(
    navController: NavController,
    storageId: Long,
    viewModel: StorageDetailViewModel = viewModel(
        factory = StorageDetailViewModelFactory(
            LocalContext.current.applicationContext as Application,
            storageId
        )
    )
) {
    val storage by viewModel.storage.observeAsState()
    val folders by viewModel.folders.observeAsState(emptyList())

    if (storage == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(storage!!.name) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Storage Info Card
            if (storage!!.description.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.description),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = storage!!.description,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            // Folders List
            if (folders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
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
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
                ) {
                    items(folders) { folder ->
                        FolderCard(
                            folder = folder,
                            storageName = storage!!.name,
                            onFolderClick = {
                                navController.navigate(
                                    Screen.FolderDetail.createRoute(folder.storageId, folder.id)
                                )
                            },
                            onFavoriteClick = {
                                viewModel.toggleFolderMarked(folder)
                            }
                        )
                    }
                }
            }
        }
    }
}

// Preview functions
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun StorageDetailScreenPreview() {
    MaterialTheme {
        Surface {
            val sampleFolders = listOf(
                Folder(
                    id = 1,
                    name = "Electronics",
                    description = "All electronic devices",
                    storageId = 1,
                    isMarked = true
                ),
                Folder(
                    id = 2,
                    name = "Tools",
                    description = "Hand and power tools",
                    storageId = 1,
                    isMarked = false
                ),
                Folder(
                    id = 3,
                    name = "Office Supplies",
                    description = "Pens, papers, etc.",
                    storageId = 1,
                    isMarked = false
                )
            )

            Column(modifier = Modifier.fillMaxSize()) {
                // Top Bar
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    tonalElevation = 3.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Store,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Main Warehouse",
                                style = MaterialTheme.typography.headlineSmall
                            )
                            Text(
                                text = "Primary storage location",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Folders List
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(sampleFolders) { folder ->
                        FolderCard(
                            folder = folder,
                            storageName = "Main Warehouse",
                            onFolderClick = {},
                            onFavoriteClick = {}
                        )
                    }
                }
            }
        }
    }
}
