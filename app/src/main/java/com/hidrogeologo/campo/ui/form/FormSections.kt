package com.hidrogeologo.campo.ui.form

import android.Manifest
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.hidrogeologo.campo.data.model.AdministrativeInfo
import com.hidrogeologo.campo.data.model.ElectromechanicalComponents
import com.hidrogeologo.campo.data.model.HydraulicParameters
import com.hidrogeologo.campo.data.model.InfrastructureStatus
import com.hidrogeologo.campo.data.model.RecordType
import com.hidrogeologo.campo.data.model.WaterQuality
import com.hidrogeologo.campo.ui.components.LabeledDropdown
import com.hidrogeologo.campo.ui.components.LabeledNumberField
import com.hidrogeologo.campo.ui.components.LabeledSwitch
import com.hidrogeologo.campo.ui.components.LabeledTextField
import com.hidrogeologo.campo.ui.components.SectionCard
import com.hidrogeologo.campo.util.DateUtils

private val conditionOptions = listOf("Bueno", "Regular", "Malo")

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AdministrativeLocationSection(
    admin: AdministrativeInfo,
    recordType: RecordType,
    onRecordTypeChange: (RecordType) -> Unit,
    onChange: ((AdministrativeInfo) -> AdministrativeInfo) -> Unit,
    isFetchingLocation: Boolean,
    onCaptureLocation: () -> Unit
) {
    SectionCard(title = "Datos administrativos y ubicación") {
        LabeledDropdown(
            label = "Tipo de punto de agua",
            options = RecordType.entries.map { it.label },
            selected = recordType.label,
            onSelectedChange = { label ->
                RecordType.entries.firstOrNull { it.label == label }?.let(onRecordTypeChange)
            }
        )
        LabeledTextField("Nombre del sitio / pozo", admin.siteName, { onChange { a -> a.copy(siteName = it) } })
        LabeledTextField("Folio de campo", admin.folio, { onChange { a -> a.copy(folio = it) } })
        LabeledTextField("Propietario / usuario", admin.owner, { onChange { a -> a.copy(owner = it) } })
        LabeledTextField("Comunidad / localidad", admin.community, { onChange { a -> a.copy(community = it) } })
        LabeledTextField("Municipio", admin.municipality, { onChange { a -> a.copy(municipality = it) } })
        LabeledTextField("Estado", admin.state, { onChange { a -> a.copy(state = it) } })
        LabeledTextField("Técnico responsable", admin.technician, { onChange { a -> a.copy(technician = it) } })
        LabeledTextField(
            "Referencia de acceso",
            admin.accessReference,
            { onChange { a -> a.copy(accessReference = it) } },
            singleLine = false,
            minLines = 2
        )

        VisitDatePicker(admin.visitDateMillis) { millis -> onChange { it.copy(visitDateMillis = millis) } }

        Column(modifier = Modifier.padding(top = 8.dp)) {
            Text("Ubicación geográfica", style = MaterialTheme.typography.titleMedium)
            val locationText = if (admin.latitude != null && admin.longitude != null) {
                "Lat: %.6f  Lon: %.6f".format(admin.latitude, admin.longitude) +
                    (admin.altitudeMeters?.let { "\nAltitud: %.1f m".format(it) } ?: "") +
                    (admin.locationAccuracyMeters?.let { "\nPrecisión: ±%.0f m".format(it) } ?: "")
            } else {
                "Sin coordenadas registradas"
            }
            Text(locationText, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 8.dp))

            val locationPermissions = rememberMultiplePermissionsState(
                listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(onClick = {
                    if (locationPermissions.allPermissionsGranted) {
                        onCaptureLocation()
                    } else {
                        locationPermissions.launchMultiplePermissionRequest()
                    }
                }) {
                    Text(if (locationPermissions.allPermissionsGranted) "Usar mi ubicación actual" else "Conceder permiso de ubicación")
                }
                if (isFetchingLocation) {
                    CircularProgressIndicator(modifier = Modifier.padding(start = 12.dp).size(20.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VisitDatePicker(currentMillis: Long, onDateSelected: (Long) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Fecha de visita: ${DateUtils.formatDate(currentMillis)}", modifier = Modifier.weight(1f))
        TextButton(onClick = { showDialog = true }) { Text("Cambiar") }
    }

    if (showDialog) {
        val state = rememberDatePickerState(initialSelectedDateMillis = currentMillis)
        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let(onDateSelected)
                    showDialog = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = state)
        }
    }
}

@Composable
fun InfrastructureSection(
    infra: InfrastructureStatus,
    onChange: ((InfrastructureStatus) -> InfrastructureStatus) -> Unit
) {
    SectionCard(title = "Estado de la infraestructura") {
        LabeledDropdown("Condición del brocal / cabezal", conditionOptions, infra.wellheadCondition) {
            onChange { s -> s.copy(wellheadCondition = it) }
        }
        LabeledSwitch("Cuenta con cercado perimetral", infra.hasFencing) { onChange { s -> s.copy(hasFencing = it) } }
        LabeledSwitch("Cuenta con losa sanitaria", infra.hasSanitarySlab) { onChange { s -> s.copy(hasSanitarySlab = it) } }
        LabeledSwitch("Cuenta con ademe / revestimiento de protección", infra.hasProtectiveCasing) {
            onChange { s -> s.copy(hasProtectiveCasing = it) }
        }
        LabeledSwitch("Cuenta con candado / seguridad", infra.hasLock) { onChange { s -> s.copy(hasLock = it) } }
        LabeledSwitch("Cuenta con caseta de bombeo", infra.hasPumpHouse) { onChange { s -> s.copy(hasPumpHouse = it) } }
        LabeledTextField("Condición del camino de acceso", infra.accessRoadCondition, {
            onChange { s -> s.copy(accessRoadCondition = it) }
        })
        LabeledNumberField("Radio de protección sanitaria (m)", infra.protectionRadiusMeters) {
            onChange { s -> s.copy(protectionRadiusMeters = it) }
        }
        LabeledDropdown("Condición general", conditionOptions, infra.generalCondition) {
            onChange { s -> s.copy(generalCondition = it) }
        }
        LabeledTextField(
            "Notas de infraestructura",
            infra.infrastructureNotes,
            { onChange { s -> s.copy(infrastructureNotes = it) } },
            singleLine = false,
            minLines = 2
        )
    }
}

@Composable
fun HydraulicSection(
    hydraulic: HydraulicParameters,
    onChange: ((HydraulicParameters) -> HydraulicParameters) -> Unit
) {
    SectionCard(title = "Parámetros hidráulicos y del pozo") {
        LabeledNumberField("Profundidad total (m)", hydraulic.totalDepthMeters) { onChange { h -> h.copy(totalDepthMeters = it) } }
        LabeledNumberField("Diámetro (pulgadas)", hydraulic.diameterInches) { onChange { h -> h.copy(diameterInches = it) } }
        LabeledNumberField("Nivel estático (m)", hydraulic.staticWaterLevelMeters) {
            onChange { h -> h.copy(staticWaterLevelMeters = it) }
        }
        LabeledNumberField("Nivel dinámico (m)", hydraulic.dynamicWaterLevelMeters) {
            onChange { h -> h.copy(dynamicWaterLevelMeters = it) }
        }
        LabeledNumberField("Caudal de bombeo (L/s)", hydraulic.pumpingFlowRateLps) {
            onChange { h -> h.copy(pumpingFlowRateLps = it) }
        }
        LabeledNumberField("Duración de prueba de bombeo (h)", hydraulic.pumpingTestDurationHours) {
            onChange { h -> h.copy(pumpingTestDurationHours = it) }
        }
        LabeledNumberField("Capacidad específica (L/s/m)", hydraulic.specificCapacity) {
            onChange { h -> h.copy(specificCapacity = it) }
        }
        LabeledTextField("Tipo de acuífero", hydraulic.aquiferType, { onChange { h -> h.copy(aquiferType = it) } })
        LabeledTextField("Material de ademe", hydraulic.casingMaterial, { onChange { h -> h.copy(casingMaterial = it) } })
        LabeledTextField("Intervalo(s) de rejilla / filtro", hydraulic.screenInterval, {
            onChange { h -> h.copy(screenInterval = it) }
        })
        LabeledTextField("Uso del agua", hydraulic.waterUse, { onChange { h -> h.copy(waterUse = it) } })
    }
}

@Composable
fun ElectromechanicalSection(
    electro: ElectromechanicalComponents,
    onChange: ((ElectromechanicalComponents) -> ElectromechanicalComponents) -> Unit
) {
    SectionCard(title = "Componentes electromecánicos") {
        LabeledSwitch("Cuenta con equipo de bombeo instalado", electro.hasPumpingEquipment) {
            onChange { e -> e.copy(hasPumpingEquipment = it) }
        }
        LabeledTextField("Tipo de bomba (sumergible, superficie, manual)", electro.pumpType, {
            onChange { e -> e.copy(pumpType = it) }
        })
        LabeledTextField("Marca de la bomba", electro.pumpBrand, { onChange { e -> e.copy(pumpBrand = it) } })
        LabeledTextField("Modelo de la bomba", electro.pumpModel, { onChange { e -> e.copy(pumpModel = it) } })
        LabeledNumberField("Potencia del motor (HP)", electro.motorPowerHp) { onChange { e -> e.copy(motorPowerHp = it) } }
        LabeledTextField("Voltaje", electro.voltage, { onChange { e -> e.copy(voltage = it) } })
        LabeledTextField("Fase (monofásico / trifásico)", electro.phase, { onChange { e -> e.copy(phase = it) } })
        LabeledTextField("Fuente de energía", electro.energySource, { onChange { e -> e.copy(energySource = it) } })
        LabeledSwitch("Cuenta con panel de control", electro.hasControlPanel) { onChange { e -> e.copy(hasControlPanel = it) } }
        LabeledSwitch("Cuenta con generador de respaldo", electro.hasGenerator) { onChange { e -> e.copy(hasGenerator = it) } }
        LabeledNumberField("Profundidad de instalación de la bomba (m)", electro.installedPumpDepthMeters) {
            onChange { e -> e.copy(installedPumpDepthMeters = it) }
        }
        LabeledNumberField("Diámetro de columna / tubería (pulgadas)", electro.columnPipeDiameterInches) {
            onChange { e -> e.copy(columnPipeDiameterInches = it) }
        }
        LabeledTextField(
            "Notas del equipo electromecánico",
            electro.electromechanicalNotes,
            { onChange { e -> e.copy(electromechanicalNotes = it) } },
            singleLine = false,
            minLines = 2
        )
    }
}

@Composable
fun WaterQualitySection(
    quality: WaterQuality,
    onChange: ((WaterQuality) -> WaterQuality) -> Unit
) {
    SectionCard(title = "Calidad fisicoquímica del agua", subtitle = "Parámetros medidos en campo") {
        LabeledNumberField("pH", quality.phValue) { onChange { q -> q.copy(phValue = it) } }
        LabeledNumberField("Conductividad eléctrica (µS/cm)", quality.electricalConductivityUsCm) {
            onChange { q -> q.copy(electricalConductivityUsCm = it) }
        }
        LabeledNumberField("Sólidos disueltos totales (mg/L)", quality.totalDissolvedSolidsMgL) {
            onChange { q -> q.copy(totalDissolvedSolidsMgL = it) }
        }
        LabeledNumberField("Temperatura (°C)", quality.temperatureCelsius) { onChange { q -> q.copy(temperatureCelsius = it) } }
        LabeledNumberField("Turbidez (NTU)", quality.turbidityNtu) { onChange { q -> q.copy(turbidityNtu = it) } }
        LabeledNumberField("Oxígeno disuelto (mg/L)", quality.dissolvedOxygenMgL) {
            onChange { q -> q.copy(dissolvedOxygenMgL = it) }
        }
        LabeledNumberField("Cloro libre residual (mg/L)", quality.freeChlorineMgL) {
            onChange { q -> q.copy(freeChlorineMgL = it) }
        }
        LabeledTextField("Olor", quality.odor, { onChange { q -> q.copy(odor = it) } })
        LabeledTextField("Color", quality.color, { onChange { q -> q.copy(color = it) } })
        LabeledTextField("Aspecto / apariencia", quality.appearance, { onChange { q -> q.copy(appearance = it) } })
    }
}

@Composable
fun ObservationsSection(observations: String, onChange: (String) -> Unit) {
    SectionCard(title = "Observaciones y hallazgos") {
        LabeledTextField(
            label = "Describe hallazgos relevantes, riesgos o recomendaciones",
            value = observations,
            onValueChange = onChange,
            singleLine = false,
            minLines = 5
        )
    }
}
