package com.hidrogeologo.campo.util

import android.content.Context
import com.hidrogeologo.campo.data.model.WellRecord
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvExporter {

    private val HEADERS = listOf(
        "id", "tipo", "folio", "nombre_sitio", "propietario", "comunidad", "municipio", "estado", "tecnico",
        "fecha_visita", "latitud", "longitud", "altitud_m", "precision_m", "referencia_acceso",
        "condicion_brocal", "cercado", "losa_sanitaria", "ademe_proteccion", "candado", "caseta_bombeo",
        "camino_acceso", "radio_proteccion_m", "condicion_general", "notas_infraestructura",
        "profundidad_total_m", "diametro_in", "nivel_estatico_m", "nivel_dinamico_m", "caudal_bombeo_lps",
        "duracion_prueba_h", "capacidad_especifica_lspm", "tipo_acuifero", "material_ademe", "rejilla_filtro", "uso_agua",
        "equipo_bombeo_instalado", "tipo_bomba", "marca_bomba", "modelo_bomba", "potencia_motor_hp", "voltaje", "fase",
        "fuente_energia", "panel_control", "generador_respaldo", "profundidad_instalacion_bomba_m", "diametro_columna_in",
        "notas_electromecanico",
        "ph", "conductividad_us_cm", "sdt_mgl", "temperatura_c", "turbidez_ntu", "oxigeno_disuelto_mgl", "cloro_libre_mgl",
        "olor", "color", "aspecto",
        "observaciones", "fecha_creacion", "fecha_actualizacion"
    )

    fun buildCsv(records: List<WellRecord>): String {
        val sb = StringBuilder()
        sb.append('﻿') // UTF-8 BOM so Excel detects accented characters correctly
        sb.append(HEADERS.joinToString(",") { field(it) }).append("\r\n")
        records.forEach { record -> sb.append(toRow(record)).append("\r\n") }
        return sb.toString()
    }

    private fun toRow(record: WellRecord): String {
        val a = record.administrative
        val i = record.infrastructure
        val h = record.hydraulic
        val e = record.electromechanical
        val q = record.waterQuality
        val values = listOf(
            record.id.toString(),
            record.recordType.label,
            a.folio, a.siteName, a.owner, a.community, a.municipality, a.state, a.technician,
            DateUtils.formatDate(a.visitDateMillis),
            a.latitude?.toString().orEmpty(), a.longitude?.toString().orEmpty(),
            a.altitudeMeters?.toString().orEmpty(), a.locationAccuracyMeters?.toString().orEmpty(),
            a.accessReference,
            i.wellheadCondition, yesNo(i.hasFencing), yesNo(i.hasSanitarySlab), yesNo(i.hasProtectiveCasing),
            yesNo(i.hasLock), yesNo(i.hasPumpHouse), i.accessRoadCondition,
            i.protectionRadiusMeters?.toString().orEmpty(), i.generalCondition, i.infrastructureNotes,
            h.totalDepthMeters?.toString().orEmpty(), h.diameterInches?.toString().orEmpty(),
            h.staticWaterLevelMeters?.toString().orEmpty(), h.dynamicWaterLevelMeters?.toString().orEmpty(),
            h.pumpingFlowRateLps?.toString().orEmpty(), h.pumpingTestDurationHours?.toString().orEmpty(),
            h.specificCapacity?.toString().orEmpty(), h.aquiferType, h.casingMaterial, h.screenInterval, h.waterUse,
            yesNo(e.hasPumpingEquipment), e.pumpType, e.pumpBrand, e.pumpModel,
            e.motorPowerHp?.toString().orEmpty(), e.voltage, e.phase, e.energySource,
            yesNo(e.hasControlPanel), yesNo(e.hasGenerator),
            e.installedPumpDepthMeters?.toString().orEmpty(), e.columnPipeDiameterInches?.toString().orEmpty(),
            e.electromechanicalNotes,
            q.phValue?.toString().orEmpty(), q.electricalConductivityUsCm?.toString().orEmpty(),
            q.totalDissolvedSolidsMgL?.toString().orEmpty(), q.temperatureCelsius?.toString().orEmpty(),
            q.turbidityNtu?.toString().orEmpty(), q.dissolvedOxygenMgL?.toString().orEmpty(),
            q.freeChlorineMgL?.toString().orEmpty(), q.odor, q.color, q.appearance,
            record.observations,
            DateUtils.formatDateTime(record.createdAtMillis), DateUtils.formatDateTime(record.updatedAtMillis)
        )
        return values.joinToString(",") { field(it) }
    }

    private fun yesNo(value: Boolean) = if (value) "Sí" else "No"

    private fun field(value: String): String {
        val needsQuoting = value.any { it == ',' || it == '"' || it == '\n' || it == '\r' }
        return if (needsQuoting) "\"" + value.replace("\"", "\"\"") + "\"" else value
    }

    fun writeToFile(context: Context, records: List<WellRecord>): File {
        val dir = File(context.getExternalFilesDir(null), "exports").apply { mkdirs() }
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val file = File(dir, "hidrocampo_registros_$timestamp.csv")
        FileOutputStream(file).use { it.write(buildCsv(records).toByteArray(Charsets.UTF_8)) }
        return file
    }
}
