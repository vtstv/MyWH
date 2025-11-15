package com.murr.mywh.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.murr.mywh.database.entities.Folder
import com.murr.mywh.database.entities.Storage
import com.murr.mywh.ui.components.FolderCard
import com.murr.mywh.ui.components.StorageCard

// Preview for Home Screen Content
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    MaterialTheme {
        val sampleFolders = listOf(
            Folder(
                id = 1,
                name = "Electronics",
                description = "All electronic devices and components",
                storageId = 1,
                isMarked = true,
                createdAt = System.currentTimeMillis() - 86400000,
                updatedAt = System.currentTimeMillis()
            ),
            Folder(
                id = 2,
                name = "Tools",
                description = "Hand tools and power tools",
                storageId = 2,
                isMarked = false,
                createdAt = System.currentTimeMillis() - 172800000,
                updatedAt = System.currentTimeMillis() - 172800000
            ),
            Folder(
                id = 3,
                name = "Office Supplies",
                description = "Pens, papers, and other office materials",
                storageId = 1,
                isMarked = false,
                createdAt = System.currentTimeMillis() - 259200000,
                updatedAt = System.currentTimeMillis() - 259200000
            )
        )

        val sampleStorages = listOf(
            Storage(id = 1, name = "Main Warehouse", description = "Primary storage", createdAt = System.currentTimeMillis()),
            Storage(id = 2, name = "Garage", description = "Secondary storage", createdAt = System.currentTimeMillis())
        )

        Surface {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
            ) {
                item {
                    Text(
                        text = "Recent Folders",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                items(sampleFolders) { folder ->
                    FolderCard(
                        folder = folder,
                        storageName = sampleStorages.find { it.id == folder.storageId }?.name ?: "Unknown",
                        onFolderClick = {},
                        onFavoriteClick = {}
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Storages",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                items(sampleStorages) { storage ->
                    StorageCard(
                        storage = storage,
                        folderCount = sampleFolders.count { it.storageId == storage.id },
                        onClick = {}
                    )
                }
            }
        }
    }
}

// Preview for Favorites Screen
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun FavoritesScreenPreview() {
    MaterialTheme {
        val favoriteFolders = listOf(
            Folder(
                id = 1,
                name = "Important Documents",
                description = "Critical business documents and files",
                storageId = 1,
                isMarked = true,
                createdAt = System.currentTimeMillis() - 86400000,
                updatedAt = System.currentTimeMillis()
            ),
            Folder(
                id = 2,
                name = "Frequently Used",
                description = "Most commonly accessed items",
                storageId = 2,
                isMarked = true,
                createdAt = System.currentTimeMillis() - 172800000,
                updatedAt = System.currentTimeMillis()
            )
        )

        Surface {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
            ) {
                items(favoriteFolders) { folder ->
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

// Preview for Storages List
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun StoragesScreenPreview() {
    MaterialTheme {
        val storages = listOf(
            Storage(
                id = 1,
                name = "Main Warehouse",
                description = "Primary storage location for all inventory",
                createdAt = System.currentTimeMillis() - 2592000000
            ),
            Storage(
                id = 2,
                name = "Garage",
                description = "Secondary storage for tools and equipment",
                createdAt = System.currentTimeMillis() - 1296000000
            ),
            Storage(
                id = 3,
                name = "Office Cabinet",
                description = "Small storage for office supplies",
                createdAt = System.currentTimeMillis() - 604800000
            )
        )

        Surface {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(storages) { storage ->
                    StorageCard(
                        storage = storage,
                        folderCount = (1..20).random(),
                        onClick = {}
                    )
                }
            }
        }
    }
}

// Preview for Statistics Screen
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun StatisticsScreenPreview() {
    MaterialTheme {
        Surface {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Statistics",
                    style = MaterialTheme.typography.headlineMedium
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Total Folders",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "42",
                            style = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Total Storages",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "5",
                            style = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Favorite Folders",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Text(
                            text = "8",
                            style = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "New Folders This Month",
                            style = MaterialTheme.typography.labelLarge
                        )
                        Text(
                            text = "12",
                            style = MaterialTheme.typography.displaySmall
                        )
                    }
                }
            }
        }
    }
}

// Preview for Folder Detail Screen
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun FolderDetailPreview() {
    MaterialTheme {
        val folder = Folder(
            id = 1,
            name = "Important Documents",
            description = "This folder contains all the important business documents including contracts, invoices, and legal papers. All documents are sorted by date and category for easy access.",
            storageId = 1,
            isMarked = true,
            createdAt = System.currentTimeMillis() - 2592000000,
            updatedAt = System.currentTimeMillis() - 86400000
        )

        Surface {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = folder.name,
                                style = MaterialTheme.typography.headlineMedium,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = {}) {
                                Icon(
                                    imageVector = if (folder.isMarked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Favorite",
                                    tint = if (folder.isMarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        HorizontalDivider()

                        Text(
                            text = "Storage: Main Warehouse",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Text(
                            text = "Description",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(
                            text = folder.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        HorizontalDivider()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Created",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "15.10.2024 14:30",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Updated",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "14.11.2024 16:45",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {},
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit")
                    }

                    OutlinedButton(
                        onClick = {},
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copy")
                    }
                }
            }
        }
    }
}

// Preview for Empty State
@Preview(showBackground = true)
@Composable
fun EmptyStatePreview() {
    MaterialTheme {
        Surface {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderOff,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "No folders found",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(onClick = {}) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Folder")
                    }
                }
            }
        }
    }
}

// Preview for Dark Theme
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun HomeScreenDarkPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        HomeScreenPreview()
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun FolderDetailDarkPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        FolderDetailPreview()
    }
}

