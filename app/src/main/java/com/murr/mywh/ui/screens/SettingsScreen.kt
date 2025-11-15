package com.murr.mywh.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.murr.mywh.R
import com.murr.mywh.utils.ImportExportManager
import com.murr.mywh.utils.PreferencesManager
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    navController: NavController,
    onThemeChanged: () -> Unit,
    onLanguageChanged: () -> Unit
) {
    val context = LocalContext.current
    val preferencesManager = remember { PreferencesManager(context) }
    val importExportManager = remember { ImportExportManager(context) }
    val scope = rememberCoroutineScope()

    var isDarkTheme by remember {
        mutableStateOf(false)
    }
    var currentLanguage by remember {
        mutableStateOf(PreferencesManager.LANG_EN)
    }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isDarkTheme = preferencesManager.isDarkTheme
        currentLanguage = preferencesManager.language
    }

    // Export launcher
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            scope.launch {
                val success = importExportManager.exportToJson(it)
                Toast.makeText(
                    context,
                    if (success) context.getString(R.string.export_success)
                    else context.getString(R.string.export_error),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // Import launcher (JSON)
    val importJsonLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            scope.launch {
                val success = importExportManager.importFromJson(it)
                Toast.makeText(
                    context,
                    if (success) context.getString(R.string.import_success)
                    else context.getString(R.string.import_error),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // Import launcher (MySQL dump)
    val importMySQLLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            scope.launch {
                val success = importExportManager.importFromMySQLDump(it)
                Toast.makeText(
                    context,
                    if (success) context.getString(R.string.import_success)
                    else context.getString(R.string.import_error),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.nav_settings),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // Theme Setting
        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.dark_theme),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = stringResource(R.string.theme_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isDarkTheme,
                    onCheckedChange = { newValue ->
                        isDarkTheme = newValue
                        preferencesManager.isDarkTheme = newValue
                        onThemeChanged()
                    }
                )
            }
        }

        // Language Setting
        Card(
            onClick = { showLanguageDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.language),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = when (currentLanguage) {
                            PreferencesManager.LANG_RU -> "Русский"
                            PreferencesManager.LANG_DE -> "Deutsch"
                            else -> "English"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null)
            }
        }

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // Import/Export Section
        Text(
            text = stringResource(R.string.data_management),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // Export Button
        Button(
            onClick = {
                exportLauncher.launch("mywh_export_${System.currentTimeMillis()}.json")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Upload, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.export_data))
        }

        // Import Button
        OutlinedButton(
            onClick = { showImportDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Download, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.import_data))
        }
    }

    // Import Dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text(stringResource(R.string.import_data)) },
            text = { Text(stringResource(R.string.import_warning_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showImportDialog = false
                        importJsonLauncher.launch("application/json")
                    }
                ) {
                    Text(stringResource(R.string.import_continue))
                }
            },
            dismissButton = {
                Column {
                    TextButton(
                        onClick = {
                            showImportDialog = false
                            importMySQLLauncher.launch("*/*")
                        }
                    ) {
                        Text(stringResource(R.string.import_mysql_dump))
                    }
                    TextButton(onClick = { showImportDialog = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            }
        )
    }

    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(stringResource(R.string.select_language)) },
            text = {
                Column {
                    LanguageOption(
                        language = "English",
                        isSelected = currentLanguage == PreferencesManager.LANG_EN,
                        onClick = {
                            currentLanguage = PreferencesManager.LANG_EN
                            preferencesManager.language = PreferencesManager.LANG_EN
                            showLanguageDialog = false
                            onLanguageChanged()
                        }
                    )
                    LanguageOption(
                        language = "Русский",
                        isSelected = currentLanguage == PreferencesManager.LANG_RU,
                        onClick = {
                            currentLanguage = PreferencesManager.LANG_RU
                            preferencesManager.language = PreferencesManager.LANG_RU
                            showLanguageDialog = false
                            onLanguageChanged()
                        }
                    )
                    LanguageOption(
                        language = "Deutsch",
                        isSelected = currentLanguage == PreferencesManager.LANG_DE,
                        onClick = {
                            currentLanguage = PreferencesManager.LANG_DE
                            preferencesManager.language = PreferencesManager.LANG_DE
                            showLanguageDialog = false
                            onLanguageChanged()
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageOption(
    language: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Text(
            text = language,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

