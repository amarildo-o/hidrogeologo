package com.hidrogeologo.campo.ui.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Water
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.hidrogeologo.campo.HidroCampoApp
import com.hidrogeologo.campo.data.model.WellRecord
import com.hidrogeologo.campo.util.CsvExporter
import com.hidrogeologo.campo.util.DateUtils
import com.hidrogeologo.campo.util.ShareUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordListScreen(
    onAddRecord: () -> Unit,
    onOpenRecord: (Long) -> Unit
) {
    val app = LocalContext.current.applicationContext as HidroCampoApp
    val viewModel: RecordListViewModel = viewModel(
        factory = viewModelFactory { initializer { RecordListViewModel(app.repository) } }
    )
    val records by viewModel.records.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isExporting by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registros de campo") },
                actions = {
                    if (isExporting) {
                        CircularProgressIndicator(modifier = Modifier.padding(12.dp).size(20.dp))
                    } else {
                        IconButton(
                            enabled = records.isNotEmpty(),
                            onClick = {
                                scope.launch {
                                    isExporting = true
                                    val file = withContext(Dispatchers.IO) {
                                        CsvExporter.writeToFile(context, records)
                                    }
                                    isExporting = false
                                    ShareUtils.shareFile(context, file, "text/csv")
                                }
                            }
                        ) {
                            Icon(Icons.Filled.FileDownload, contentDescription = "Exportar a CSV")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddRecord) {
                Icon(Icons.Filled.Add, contentDescription = "Nuevo registro")
            }
        }
    ) { padding ->
        if (records.isEmpty()) {
            EmptyState(padding)
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(records, key = { it.id }) { record ->
                    RecordCard(record = record, onClick = { onOpenRecord(record.id) })
                }
            }
        }
    }
}

@Composable
private fun EmptyState(padding: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Filled.Water,
                contentDescription = null,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text("Aún no hay registros.")
            Text("Toca + para capturar un pozo o manantial.")
        }
    }
}

@Composable
private fun RecordCard(record: WellRecord, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = record.administrative.siteName.ifBlank { "(Sin nombre)" },
                style = androidx.compose.material3.MaterialTheme.typography.titleMedium
            )
            Text(
                text = "${record.recordType.label} · ${record.administrative.community}".trim(' ', '·'),
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Icon(
                    Icons.Filled.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 4.dp)
                )
                val locationText = if (record.administrative.latitude != null && record.administrative.longitude != null) {
                    "%.5f, %.5f".format(record.administrative.latitude, record.administrative.longitude)
                } else {
                    "Sin coordenadas"
                }
                Text(text = locationText, style = androidx.compose.material3.MaterialTheme.typography.bodyMedium)
            }
            Text(
                text = "Visita: ${DateUtils.formatDate(record.administrative.visitDateMillis)}",
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
            )
        }
    }
}
