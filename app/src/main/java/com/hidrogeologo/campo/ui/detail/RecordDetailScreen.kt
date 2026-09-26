package com.hidrogeologo.campo.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import coil.compose.AsyncImage
import com.hidrogeologo.campo.HidroCampoApp
import com.hidrogeologo.campo.data.model.WellRecord
import com.hidrogeologo.campo.ui.components.SectionCard
import com.hidrogeologo.campo.util.DateUtils
import com.hidrogeologo.campo.util.PdfReportGenerator
import com.hidrogeologo.campo.util.ShareUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordDetailScreen(
    recordId: Long,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    onDeleted: () -> Unit
) {
    val app = LocalContext.current.applicationContext as HidroCampoApp
    val viewModel: RecordDetailViewModel = viewModel(
        factory = viewModelFactory { initializer { RecordDetailViewModel(app.repository, recordId) } }
    )

    val record by viewModel.record.collectAsState()
    val photos by viewModel.photos.collectAsState()
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isExportingPdf by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(record?.administrative?.siteName?.ifBlank { "Detalle del registro" } ?: "Detalle del registro") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                },
                actions = {
                    if (isExportingPdf) {
                        CircularProgressIndicator(modifier = Modifier.padding(12.dp).size(20.dp))
                    } else {
                        IconButton(
                            enabled = record != null,
                            onClick = {
                                val currentRecord = record ?: return@IconButton
                                scope.launch {
                                    isExportingPdf = true
                                    val file = withContext(Dispatchers.IO) {
                                        PdfReportGenerator.generate(context, currentRecord, photos)
                                    }
                                    isExportingPdf = false
                                    ShareUtils.shareFile(context, file, "application/pdf")
                                }
                            }
                        ) {
                            Icon(Icons.Filled.PictureAsPdf, contentDescription = "Exportar a PDF")
                        }
                    }
                    IconButton(onClick = { onEdit(recordId) }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Editar")
                    }
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Eliminar")
                    }
                }
            )
        }
    ) { padding ->
        val current = record
        if (current == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                RecordDetailContent(record = current)

                SectionCard(title = "Fotografías") {
                    if (photos.isEmpty()) {
                        Text("Sin fotografías registradas.", style = MaterialTheme.typography.bodyMedium)
                    } else {
                        photos.chunked(3).forEach { rowPhotos ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowPhotos.forEach { photo ->
                                    AsyncImage(
                                        model = photo.filePath,
                                        contentDescription = photo.caption,
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                    )
                                }
                                repeat(3 - rowPhotos.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Eliminar registro") },
            text = { Text("Esta acción eliminará el registro y sus fotografías. ¿Deseas continuar?") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    viewModel.deleteRecord(onDeleted)
                }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun RecordDetailContent(record: WellRecord) {
    SectionCard(title = "Datos administrativos y ubicación") {
        InfoRow("Tipo", record.recordType.label)
        InfoRow("Folio", record.administrative.folio)
        InfoRow("Nombre del sitio", record.administrative.siteName)
        InfoRow("Propietario", record.administrative.owner)
        InfoRow("Comunidad", record.administrative.community)
        InfoRow("Municipio", record.administrative.municipality)
        InfoRow("Estado", record.administrative.state)
        InfoRow("Técnico", record.administrative.technician)
        InfoRow("Fecha de visita", DateUtils.formatDate(record.administrative.visitDateMillis))
        InfoRow("Referencia de acceso", record.administrative.accessReference)
        val coords = if (record.administrative.latitude != null && record.administrative.longitude != null) {
            "%.6f, %.6f".format(record.administrative.latitude, record.administrative.longitude)
        } else "Sin registrar"
        InfoRow("Coordenadas", coords)
        record.administrative.altitudeMeters?.let { InfoRow("Altitud", "%.1f m".format(it)) }
    }

    SectionCard(title = "Estado de la infraestructura") {
        InfoRow("Condición del brocal", record.infrastructure.wellheadCondition)
        InfoRow("Cercado", if (record.infrastructure.hasFencing) "Sí" else "No")
        InfoRow("Losa sanitaria", if (record.infrastructure.hasSanitarySlab) "Sí" else "No")
        InfoRow("Ademe de protección", if (record.infrastructure.hasProtectiveCasing) "Sí" else "No")
        InfoRow("Candado", if (record.infrastructure.hasLock) "Sí" else "No")
        InfoRow("Caseta de bombeo", if (record.infrastructure.hasPumpHouse) "Sí" else "No")
        InfoRow("Camino de acceso", record.infrastructure.accessRoadCondition)
        record.infrastructure.protectionRadiusMeters?.let { InfoRow("Radio de protección", "%.1f m".format(it)) }
        InfoRow("Condición general", record.infrastructure.generalCondition)
        InfoRow("Notas", record.infrastructure.infrastructureNotes)
    }

    SectionCard(title = "Parámetros hidráulicos y del pozo") {
        record.hydraulic.totalDepthMeters?.let { InfoRow("Profundidad total", "%.2f m".format(it)) }
        record.hydraulic.diameterInches?.let { InfoRow("Diámetro", "%.2f in".format(it)) }
        record.hydraulic.staticWaterLevelMeters?.let { InfoRow("Nivel estático", "%.2f m".format(it)) }
        record.hydraulic.dynamicWaterLevelMeters?.let { InfoRow("Nivel dinámico", "%.2f m".format(it)) }
        record.hydraulic.pumpingFlowRateLps?.let { InfoRow("Caudal de bombeo", "%.2f L/s".format(it)) }
        record.hydraulic.pumpingTestDurationHours?.let { InfoRow("Duración de prueba", "%.1f h".format(it)) }
        record.hydraulic.specificCapacity?.let { InfoRow("Capacidad específica", "%.2f L/s/m".format(it)) }
        InfoRow("Tipo de acuífero", record.hydraulic.aquiferType)
        InfoRow("Material de ademe", record.hydraulic.casingMaterial)
        InfoRow("Rejilla / filtro", record.hydraulic.screenInterval)
        InfoRow("Uso del agua", record.hydraulic.waterUse)
    }

    SectionCard(title = "Componentes electromecánicos") {
        InfoRow("Equipo instalado", if (record.electromechanical.hasPumpingEquipment) "Sí" else "No")
        InfoRow("Tipo de bomba", record.electromechanical.pumpType)
        InfoRow("Marca", record.electromechanical.pumpBrand)
        InfoRow("Modelo", record.electromechanical.pumpModel)
        record.electromechanical.motorPowerHp?.let { InfoRow("Potencia del motor", "%.2f HP".format(it)) }
        InfoRow("Voltaje", record.electromechanical.voltage)
        InfoRow("Fase", record.electromechanical.phase)
        InfoRow("Fuente de energía", record.electromechanical.energySource)
        InfoRow("Panel de control", if (record.electromechanical.hasControlPanel) "Sí" else "No")
        InfoRow("Generador de respaldo", if (record.electromechanical.hasGenerator) "Sí" else "No")
        record.electromechanical.installedPumpDepthMeters?.let { InfoRow("Profundidad de instalación", "%.2f m".format(it)) }
        record.electromechanical.columnPipeDiameterInches?.let { InfoRow("Diámetro de columna", "%.2f in".format(it)) }
        InfoRow("Notas", record.electromechanical.electromechanicalNotes)
    }

    SectionCard(title = "Calidad fisicoquímica del agua") {
        record.waterQuality.phValue?.let { InfoRow("pH", "%.2f".format(it)) }
        record.waterQuality.electricalConductivityUsCm?.let { InfoRow("Conductividad eléctrica", "%.1f µS/cm".format(it)) }
        record.waterQuality.totalDissolvedSolidsMgL?.let { InfoRow("Sólidos disueltos totales", "%.1f mg/L".format(it)) }
        record.waterQuality.temperatureCelsius?.let { InfoRow("Temperatura", "%.1f °C".format(it)) }
        record.waterQuality.turbidityNtu?.let { InfoRow("Turbidez", "%.1f NTU".format(it)) }
        record.waterQuality.dissolvedOxygenMgL?.let { InfoRow("Oxígeno disuelto", "%.2f mg/L".format(it)) }
        record.waterQuality.freeChlorineMgL?.let { InfoRow("Cloro libre residual", "%.2f mg/L".format(it)) }
        InfoRow("Olor", record.waterQuality.odor)
        InfoRow("Color", record.waterQuality.color)
        InfoRow("Aspecto", record.waterQuality.appearance)
    }

    SectionCard(title = "Observaciones y hallazgos") {
        Text(
            text = record.observations.ifBlank { "Sin observaciones registradas." },
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    if (value.isBlank()) return
    Column(modifier = Modifier.padding(bottom = 8.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}
