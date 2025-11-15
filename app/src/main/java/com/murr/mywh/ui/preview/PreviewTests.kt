package com.murr.mywh.ui.preview

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * Test preview file to verify Compose Preview functionality
 * If you can see previews in this file, preview is working correctly
 */

@Preview(showBackground = true, name = "Simple Text Preview")
@Composable
fun SimpleTextPreview() {
    MaterialTheme {
        Surface {
            Text(
                text = "Preview is working!",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview(showBackground = true, name = "Card Preview", widthDp = 320)
@Composable
fun SimpleCardPreview() {
    MaterialTheme {
        Surface {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Test Card",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "If you can see this preview, your Compose setup is correct!",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Button Preview")
@Composable
fun SimpleButtonPreview() {
    MaterialTheme {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(onClick = {}) {
                    Text("Primary Button")
                }

                OutlinedButton(onClick = {}) {
                    Text("Outlined Button")
                }

                TextButton(onClick = {}) {
                    Text("Text Button")
                }
            }
        }
    }
}

@Preview(
    showBackground = true,
    name = "Dark Theme Preview",
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun DarkThemePreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Dark Theme",
                    style = MaterialTheme.typography.headlineMedium
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text = "This is a card in dark theme",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

