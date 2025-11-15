package com.murr.mywh.ui.screens

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.murr.mywh.viewmodels.StatisticsViewModel
import com.murr.mywh.viewmodels.StatisticsViewModelFactory

@Composable
fun StatisticsScreen(
    navController: NavController,
    viewModel: StatisticsViewModel = viewModel(
        factory = StatisticsViewModelFactory(LocalContext.current.applicationContext as Application)
    )
) {
    val totalFolders by viewModel.totalFolders.observeAsState(0)
    val totalStorages by viewModel.totalStorages.observeAsState(0)
    val markedFolders by viewModel.markedFolders.observeAsState(0)
    val foldersThisMonth by viewModel.foldersThisMonth.observeAsState(0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.nav_statistics),
            style = MaterialTheme.typography.headlineMedium
        )

        StatisticCard(
            title = stringResource(R.string.total_folders),
            value = totalFolders.toString()
        )

        StatisticCard(
            title = stringResource(R.string.total_storages),
            value = totalStorages.toString()
        )

        StatisticCard(
            title = stringResource(R.string.marked_folders),
            value = markedFolders.toString()
        )

        StatisticCard(
            title = stringResource(R.string.new_folders_this_month),
            value = foldersThisMonth.toString()
        )
    }
}

@Composable
fun StatisticCard(title: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

