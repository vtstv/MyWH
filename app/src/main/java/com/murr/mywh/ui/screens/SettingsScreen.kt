package com.murr.mywh.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.murr.mywh.R
import com.murr.mywh.utils.ImportExportManager
import com.murr.mywh.utils.PasswordManager
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
    var fontScale by remember {
        mutableStateOf(PreferencesManager.FONT_SCALE_NORMAL)
    }
    var showCreatedDate by remember { mutableStateOf(false) }
    var showUpdatedDate by remember { mutableStateOf(false) }
    var isDebugMode by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isDarkTheme = preferencesManager.isDarkTheme
        currentLanguage = preferencesManager.language
        fontScale = preferencesManager.fontScale
        showCreatedDate = preferencesManager.showCreatedDate
        showUpdatedDate = preferencesManager.showUpdatedDate
        isDebugMode = preferencesManager.isDebugMode
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

        // Font Size Setting
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.font_size),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = stringResource(R.string.font_size_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Font size slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.font_size_label,
                                when (fontScale) {
                                    PreferencesManager.FONT_SCALE_SMALL -> stringResource(R.string.font_size_small)
                                    PreferencesManager.FONT_SCALE_NORMAL -> stringResource(R.string.font_size_normal)
                                    PreferencesManager.FONT_SCALE_LARGE -> stringResource(R.string.font_size_large)
                                    PreferencesManager.FONT_SCALE_EXTRA_LARGE -> stringResource(R.string.font_size_extra_large)
                                    PreferencesManager.FONT_SCALE_HUGE -> stringResource(R.string.font_size_huge)
                                    else -> stringResource(R.string.font_size_normal)
                                }
                            ),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Slider(
                        value = fontScale,
                        onValueChange = { newScale ->
                            fontScale = newScale
                        },
                        onValueChangeFinished = {
                            preferencesManager.fontScale = fontScale
                            onThemeChanged() // Trigger recreation to apply font scale
                        },
                        valueRange = PreferencesManager.FONT_SCALE_SMALL..PreferencesManager.FONT_SCALE_HUGE,
                        steps = 3, // Small, Normal, Large, Extra Large, Huge (5 options = 3 steps between)
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Preview text
                    Text(
                        text = stringResource(R.string.font_size_preview),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = MaterialTheme.typography.bodyMedium.fontSize * fontScale
                        ),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // Display Options Section
        Text(
            text = "Display Options",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // Show Created Date Toggle
        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.show_created_date),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = stringResource(R.string.show_created_date_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = showCreatedDate,
                    onCheckedChange = { 
                        showCreatedDate = it
                        preferencesManager.showCreatedDate = it
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Show Updated Date Toggle
        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.show_updated_date),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = stringResource(R.string.show_updated_date_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = showUpdatedDate,
                    onCheckedChange = { 
                        showUpdatedDate = it
                        preferencesManager.showUpdatedDate = it
                    }
                )
            }
        }

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // Debug Section
        Text(
            text = "Debug",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // Debug Mode Toggle
        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.debug_mode),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = stringResource(R.string.debug_mode_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isDebugMode,
                    onCheckedChange = { 
                        isDebugMode = it
                        preferencesManager.isDebugMode = it
                    }
                )
            }
        }

        // View Logs Button (only show if debug mode is enabled)
        if (isDebugMode) {
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = {
                    navController.navigate("debug_logs")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.List, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.view_logs))
            }
        }

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // Security Section
        Text(
            text = stringResource(R.string.security),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        SecuritySettings()

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
            text = { 
                Column {
                    Text(stringResource(R.string.import_warning_message))
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Import buttons as vertical list for better layout with large fonts
                    Button(
                        onClick = {
                            showImportDialog = false
                            importJsonLauncher.launch("application/json")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.import_continue))
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedButton(
                        onClick = {
                            showImportDialog = false
                            importMySQLLauncher.launch("*/*")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.import_mysql_dump))
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text(stringResource(R.string.cancel))
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

@Composable
fun SecuritySettings() {
    val context = LocalContext.current
    val passwordManager = remember { PasswordManager(context) }
    val biometricManager = remember { BiometricManager.from(context) }

    var hasPassword by remember { mutableStateOf(passwordManager.hasPassword()) }
    var isPasswordEnabled by remember { mutableStateOf(passwordManager.isPasswordEnabled) }
    var reaskInterval by remember { mutableStateOf(passwordManager.reaskInterval) }
    var isBiometricEnabled by remember { mutableStateOf(passwordManager.isBiometricEnabled) }
    var showSetPasswordDialog by remember { mutableStateOf(false) }
    var showRemovePasswordDialog by remember { mutableStateOf(false) }
    var showIntervalDialog by remember { mutableStateOf(false) }

    // Check if biometric is available on device
    val canUseBiometric = remember {
        biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS
    }

    // Password Protection Toggle
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.password_protection),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = stringResource(R.string.password_protection_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = isPasswordEnabled && hasPassword,
                onCheckedChange = { enabled ->
                    if (enabled && !hasPassword) {
                        showSetPasswordDialog = true
                    } else if (!enabled && hasPassword) {
                        showRemovePasswordDialog = true
                    }
                }
            )
        }
    }

    // Reask Interval Setting (only if password is enabled)
    if (hasPassword && isPasswordEnabled) {
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            onClick = { showIntervalDialog = true },
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
                        text = stringResource(R.string.reask_password),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = when (reaskInterval) {
                            PasswordManager.INTERVAL_HOUR -> stringResource(R.string.interval_hour)
                            PasswordManager.INTERVAL_DAY -> stringResource(R.string.interval_day)
                            PasswordManager.INTERVAL_WEEK -> stringResource(R.string.interval_week)
                            else -> stringResource(R.string.interval_hour)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null)
            }
        }

        // Biometric toggle (only if password is enabled and device supports it)
        if (canUseBiometric) {
            Spacer(modifier = Modifier.height(8.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.biometric_unlock),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = stringResource(R.string.biometric_unlock_description),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isBiometricEnabled,
                        onCheckedChange = { enabled ->
                            isBiometricEnabled = enabled
                            passwordManager.isBiometricEnabled = enabled
                        }
                    )
                }
            }
        }
    }

    // Set Password Dialog
    if (showSetPasswordDialog) {
        SetPasswordDialog(
            onDismiss = { showSetPasswordDialog = false },
            onConfirm = { password ->
                passwordManager.setPassword(password)
                hasPassword = true
                isPasswordEnabled = true
                showSetPasswordDialog = false
            }
        )
    }

    // Remove Password Dialog
    if (showRemovePasswordDialog) {
        AlertDialog(
            onDismissRequest = { showRemovePasswordDialog = false },
            title = { Text(stringResource(R.string.remove_password)) },
            text = { Text(stringResource(R.string.remove_password_confirmation)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        passwordManager.removePassword()
                        hasPassword = false
                        isPasswordEnabled = false
                        showRemovePasswordDialog = false
                    }
                ) {
                    Text(stringResource(R.string.remove))
                }
            },
            dismissButton = {
                TextButton(onClick = { showRemovePasswordDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Interval Selection Dialog
    if (showIntervalDialog) {
        AlertDialog(
            onDismissRequest = { showIntervalDialog = false },
            title = { Text(stringResource(R.string.reask_password)) },
            text = {
                Column {
                    IntervalOption(
                        label = stringResource(R.string.interval_hour),
                        isSelected = reaskInterval == PasswordManager.INTERVAL_HOUR,
                        onClick = {
                            passwordManager.reaskInterval = PasswordManager.INTERVAL_HOUR
                            reaskInterval = PasswordManager.INTERVAL_HOUR
                            showIntervalDialog = false
                        }
                    )
                    IntervalOption(
                        label = stringResource(R.string.interval_day),
                        isSelected = reaskInterval == PasswordManager.INTERVAL_DAY,
                        onClick = {
                            passwordManager.reaskInterval = PasswordManager.INTERVAL_DAY
                            reaskInterval = PasswordManager.INTERVAL_DAY
                            showIntervalDialog = false
                        }
                    )
                    IntervalOption(
                        label = stringResource(R.string.interval_week),
                        isSelected = reaskInterval == PasswordManager.INTERVAL_WEEK,
                        onClick = {
                            passwordManager.reaskInterval = PasswordManager.INTERVAL_WEEK
                            reaskInterval = PasswordManager.INTERVAL_WEEK
                            showIntervalDialog = false
                        }
                    )
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showIntervalDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun SetPasswordDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.set_password)) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        errorMessage = null
                    },
                    label = { Text(stringResource(R.string.password)) },
                    singleLine = true,
                    visualTransformation = if (passwordVisible)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        errorMessage = null
                    },
                    label = { Text(stringResource(R.string.confirm_password)) },
                    singleLine = true,
                    visualTransformation = if (confirmPasswordVisible)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null
                            )
                        }
                    },
                    isError = errorMessage != null,
                    supportingText = errorMessage?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    when {
                        password.isEmpty() -> errorMessage = "Password cannot be empty"
                        password.length < 4 -> errorMessage = "Password must be at least 4 characters"
                        password != confirmPassword -> errorMessage = "Passwords do not match"
                        else -> onConfirm(password)
                    }
                }
            ) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
fun IntervalOption(
    label: String,
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
            text = label,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
