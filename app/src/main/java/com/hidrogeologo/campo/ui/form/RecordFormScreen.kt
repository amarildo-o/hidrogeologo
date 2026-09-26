package com.hidrogeologo.campo.ui.form

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.hidrogeologo.campo.HidroCampoApp
import com.hidrogeologo.campo.ui.components.PhotoSection
import kotlinx.coroutines.launch

private val sectionTitles = listOf(
    "Administrativo",
    "Infraestructura",
    "Hidráulico",
    "Electromecánico",
    "Calidad del agua",
    "Observaciones",
    "Fotografías"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordFormScreen(
    recordId: Long,
    onBack: () -> Unit,
    onSaved: (Long) -> Unit
) {
    val app = LocalContext.current.applicationContext as HidroCampoApp
    val viewModel: RecordFormViewModel = viewModel(
        factory = viewModelFactory {
            initializer { RecordFormViewModel(app, app.repository, recordId) }
        }
    )

    val record by viewModel.record.collectAsState()
    val savedPhotos by viewModel.savedPhotos.collectAsState()
    val pendingPhotos by viewModel.pendingPhotos.collectAsState()
    val isFetchingLocation by viewModel.isFetchingLocation.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()

    val pagerState = rememberPagerState(pageCount = { sectionTitles.size })
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (record.administrative.siteName.isBlank()) "Nuevo registro" else record.administrative.siteName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.save(onSaved) }, enabled = !isSaving) {
                        if (isSaving) {
                            CircularProgressIndicator(modifier = Modifier.padding(4.dp))
                        } else {
                            Icon(Icons.Filled.Check, contentDescription = "Guardar")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            ScrollableTabRow(selectedTabIndex = pagerState.currentPage) {
                sectionTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                        text = { Text(title) }
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    when (page) {
                        0 -> AdministrativeLocationSection(
                            admin = record.administrative,
                            recordType = record.recordType,
                            onRecordTypeChange = viewModel::setRecordType,
                            onChange = viewModel::updateAdministrative,
                            isFetchingLocation = isFetchingLocation,
                            onCaptureLocation = viewModel::captureLocation
                        )

                        1 -> InfrastructureSection(
                            infra = record.infrastructure,
                            onChange = viewModel::updateInfrastructure
                        )

                        2 -> HydraulicSection(
                            hydraulic = record.hydraulic,
                            onChange = viewModel::updateHydraulic
                        )

                        3 -> ElectromechanicalSection(
                            electro = record.electromechanical,
                            onChange = viewModel::updateElectromechanical
                        )

                        4 -> WaterQualitySection(
                            quality = record.waterQuality,
                            onChange = viewModel::updateWaterQuality
                        )

                        5 -> ObservationsSection(
                            observations = record.observations,
                            onChange = viewModel::updateObservations
                        )

                        6 -> PhotoSection(
                            savedPhotos = savedPhotos,
                            pendingPhotos = pendingPhotos,
                            onPhotoCaptured = viewModel::addPendingPhoto,
                            onDeleteSaved = viewModel::removeSavedPhoto,
                            onDeletePending = viewModel::removePendingPhoto
                        )
                    }
                }
            }
        }
    }
}
