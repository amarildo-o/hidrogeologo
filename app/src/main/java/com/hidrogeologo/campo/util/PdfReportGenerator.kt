package com.hidrogeologo.campo.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import com.hidrogeologo.campo.data.model.RecordPhoto
import com.hidrogeologo.campo.data.model.WellRecord
import java.io.File
import java.io.FileOutputStream

object PdfReportGenerator {

    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 40f
    private const val LABEL_COLUMN_WIDTH = 170f
    private const val LINE_HEIGHT = 14f
    private val CONTENT_WIDTH = PAGE_WIDTH - 2 * MARGIN

    private val titlePaint = Paint().apply { textSize = 16f; isFakeBoldText = true; color = Color.BLACK }
    private val sectionPaint = Paint().apply { textSize = 13f; isFakeBoldText = true; color = Color.rgb(13, 92, 99) }
    private val dividerPaint = Paint().apply { color = Color.LTGRAY }
    private val labelPaint = Paint().apply { textSize = 10f; isFakeBoldText = true; color = Color.DKGRAY }
    private val valuePaint = Paint().apply { textSize = 10f; color = Color.BLACK }

    private class ReportCursor(private val document: PdfDocument) {
        private var pageNumber = 1
        private var page = startPage()
        var canvas = page.canvas
            private set
        var y = MARGIN
            private set

        private fun startPage(): PdfDocument.Page =
            document.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create())

        private fun newPage() {
            document.finishPage(page)
            pageNumber++
            page = startPage()
            canvas = page.canvas
            y = MARGIN
        }

        fun ensureSpace(height: Float) {
            if (y + height > PAGE_HEIGHT - MARGIN) newPage()
        }

        fun advance(amount: Float) {
            y += amount
        }

        fun finish() {
            document.finishPage(page)
        }
    }

    fun generate(context: Context, record: WellRecord, photos: List<RecordPhoto>): File {
        val document = PdfDocument()
        val cursor = ReportCursor(document)

        drawTitle(cursor, record.administrative.siteName.ifBlank { "Registro de punto de agua" })
        drawRow(cursor, "Tipo", record.recordType.label)
        drawRow(cursor, "Folio", record.administrative.folio)

        drawSection(cursor, "Datos administrativos y ubicación")
        val a = record.administrative
        drawRow(cursor, "Propietario", a.owner)
        drawRow(cursor, "Comunidad", a.community)
        drawRow(cursor, "Municipio", a.municipality)
        drawRow(cursor, "Estado", a.state)
        drawRow(cursor, "Técnico responsable", a.technician)
        drawRow(cursor, "Fecha de visita", DateUtils.formatDate(a.visitDateMillis))
        drawRow(cursor, "Referencia de acceso", a.accessReference)
        val coords = if (a.latitude != null && a.longitude != null) "%.6f, %.6f".format(a.latitude, a.longitude) else ""
        drawRow(cursor, "Coordenadas", coords)
        drawRow(cursor, "Altitud", a.altitudeMeters?.let { "%.1f m".format(it) } ?: "")

        drawSection(cursor, "Estado de la infraestructura")
        val i = record.infrastructure
        drawRow(cursor, "Condición del brocal", i.wellheadCondition)
        drawRow(cursor, "Cercado perimetral", yesNo(i.hasFencing))
        drawRow(cursor, "Losa sanitaria", yesNo(i.hasSanitarySlab))
        drawRow(cursor, "Ademe de protección", yesNo(i.hasProtectiveCasing))
        drawRow(cursor, "Candado", yesNo(i.hasLock))
        drawRow(cursor, "Caseta de bombeo", yesNo(i.hasPumpHouse))
        drawRow(cursor, "Camino de acceso", i.accessRoadCondition)
        drawRow(cursor, "Radio de protección", i.protectionRadiusMeters?.let { "%.1f m".format(it) } ?: "")
        drawRow(cursor, "Condición general", i.generalCondition)
        drawParagraph(cursor, i.infrastructureNotes)

        drawSection(cursor, "Parámetros hidráulicos y del pozo")
        val h = record.hydraulic
        drawRow(cursor, "Profundidad total", h.totalDepthMeters?.let { "%.2f m".format(it) } ?: "")
        drawRow(cursor, "Diámetro", h.diameterInches?.let { "%.2f in".format(it) } ?: "")
        drawRow(cursor, "Nivel estático", h.staticWaterLevelMeters?.let { "%.2f m".format(it) } ?: "")
        drawRow(cursor, "Nivel dinámico", h.dynamicWaterLevelMeters?.let { "%.2f m".format(it) } ?: "")
        drawRow(cursor, "Caudal de bombeo", h.pumpingFlowRateLps?.let { "%.2f L/s".format(it) } ?: "")
        drawRow(cursor, "Duración de prueba", h.pumpingTestDurationHours?.let { "%.1f h".format(it) } ?: "")
        drawRow(cursor, "Capacidad específica", h.specificCapacity?.let { "%.2f L/s/m".format(it) } ?: "")
        drawRow(cursor, "Tipo de acuífero", h.aquiferType)
        drawRow(cursor, "Material de ademe", h.casingMaterial)
        drawRow(cursor, "Rejilla / filtro", h.screenInterval)
        drawRow(cursor, "Uso del agua", h.waterUse)

        drawSection(cursor, "Componentes electromecánicos")
        val e = record.electromechanical
        drawRow(cursor, "Equipo instalado", yesNo(e.hasPumpingEquipment))
        drawRow(cursor, "Tipo de bomba", e.pumpType)
        drawRow(cursor, "Marca", e.pumpBrand)
        drawRow(cursor, "Modelo", e.pumpModel)
        drawRow(cursor, "Potencia del motor", e.motorPowerHp?.let { "%.2f HP".format(it) } ?: "")
        drawRow(cursor, "Voltaje", e.voltage)
        drawRow(cursor, "Fase", e.phase)
        drawRow(cursor, "Fuente de energía", e.energySource)
        drawRow(cursor, "Panel de control", yesNo(e.hasControlPanel))
        drawRow(cursor, "Generador de respaldo", yesNo(e.hasGenerator))
        drawRow(cursor, "Profundidad de instalación", e.installedPumpDepthMeters?.let { "%.2f m".format(it) } ?: "")
        drawRow(cursor, "Diámetro de columna", e.columnPipeDiameterInches?.let { "%.2f in".format(it) } ?: "")
        drawParagraph(cursor, e.electromechanicalNotes)

        drawSection(cursor, "Calidad fisicoquímica del agua")
        val q = record.waterQuality
        drawRow(cursor, "pH", q.phValue?.let { "%.2f".format(it) } ?: "")
        drawRow(cursor, "Conductividad eléctrica", q.electricalConductivityUsCm?.let { "%.1f µS/cm".format(it) } ?: "")
        drawRow(cursor, "Sólidos disueltos totales", q.totalDissolvedSolidsMgL?.let { "%.1f mg/L".format(it) } ?: "")
        drawRow(cursor, "Temperatura", q.temperatureCelsius?.let { "%.1f °C".format(it) } ?: "")
        drawRow(cursor, "Turbidez", q.turbidityNtu?.let { "%.1f NTU".format(it) } ?: "")
        drawRow(cursor, "Oxígeno disuelto", q.dissolvedOxygenMgL?.let { "%.2f mg/L".format(it) } ?: "")
        drawRow(cursor, "Cloro libre residual", q.freeChlorineMgL?.let { "%.2f mg/L".format(it) } ?: "")
        drawRow(cursor, "Olor", q.odor)
        drawRow(cursor, "Color", q.color)
        drawRow(cursor, "Aspecto", q.appearance)

        drawSection(cursor, "Observaciones y hallazgos")
        drawParagraph(cursor, record.observations.ifBlank { "Sin observaciones registradas." })

        drawSection(cursor, "Fotografías")
        if (photos.isEmpty()) {
            drawParagraph(cursor, "Sin fotografías registradas.")
        } else {
            photos.forEach { photo -> drawPhoto(cursor, photo) }
        }

        cursor.finish()

        val dir = File(context.getExternalFilesDir(null), "exports").apply { mkdirs() }
        val fileName = "reporte_${sanitizeFileName(record.administrative.siteName)}_${record.id}.pdf"
        val file = File(dir, fileName)
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()
        return file
    }

    private fun drawTitle(cursor: ReportCursor, text: String) {
        cursor.ensureSpace(LINE_HEIGHT * 1.5f)
        cursor.canvas.drawText(text, MARGIN, cursor.y, titlePaint)
        cursor.advance(LINE_HEIGHT * 1.6f)
    }

    private fun drawSection(cursor: ReportCursor, text: String) {
        cursor.ensureSpace(LINE_HEIGHT * 2.2f)
        cursor.advance(8f)
        cursor.canvas.drawText(text, MARGIN, cursor.y, sectionPaint)
        cursor.advance(4f)
        cursor.canvas.drawLine(MARGIN, cursor.y, PAGE_WIDTH - MARGIN, cursor.y, dividerPaint)
        cursor.advance(LINE_HEIGHT)
    }

    private fun drawRow(cursor: ReportCursor, label: String, value: String) {
        if (value.isBlank()) return
        cursor.ensureSpace(LINE_HEIGHT)
        cursor.canvas.drawText(label, MARGIN, cursor.y, labelPaint)
        cursor.canvas.drawText(value, MARGIN + LABEL_COLUMN_WIDTH, cursor.y, valuePaint)
        cursor.advance(LINE_HEIGHT)
    }

    private fun drawParagraph(cursor: ReportCursor, text: String) {
        if (text.isBlank()) return
        wrapText(text, valuePaint, CONTENT_WIDTH).forEach { line ->
            cursor.ensureSpace(LINE_HEIGHT)
            cursor.canvas.drawText(line, MARGIN, cursor.y, valuePaint)
            cursor.advance(LINE_HEIGHT)
        }
    }

    private fun drawPhoto(cursor: ReportCursor, photo: RecordPhoto) {
        val bitmap = decodeSampledBitmap(photo.filePath, 900, 900)
        if (bitmap == null) {
            drawParagraph(cursor, "(Fotografía no disponible: ${photo.filePath})")
            return
        }
        val maxHeight = 220f
        val scale = minOf(CONTENT_WIDTH / bitmap.width, maxHeight / bitmap.height)
        val drawWidth = bitmap.width * scale
        val drawHeight = bitmap.height * scale
        cursor.ensureSpace(drawHeight + 12f)
        val left = MARGIN
        val top = cursor.y
        cursor.canvas.drawBitmap(bitmap, null, RectF(left, top, left + drawWidth, top + drawHeight), null)
        cursor.advance(drawHeight + 12f)
    }

    private fun decodeSampledBitmap(path: String, reqWidth: Int, reqHeight: Int): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        var sampleSize = 1
        val halfHeight = bounds.outHeight / 2
        val halfWidth = bounds.outWidth / 2
        while (halfHeight / sampleSize >= reqHeight && halfWidth / sampleSize >= reqWidth) {
            sampleSize *= 2
        }

        val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        return BitmapFactory.decodeFile(path, options)
    }

    private fun yesNo(value: Boolean) = if (value) "Sí" else "No"

    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        val lines = mutableListOf<String>()
        text.split("\n").forEach { paragraph ->
            var current = StringBuilder()
            paragraph.split(" ").forEach { word ->
                val candidate = if (current.isEmpty()) word else "$current $word"
                if (current.isNotEmpty() && paint.measureText(candidate) > maxWidth) {
                    lines.add(current.toString())
                    current = StringBuilder(word)
                } else {
                    current = StringBuilder(candidate)
                }
            }
            lines.add(current.toString())
        }
        return lines
    }
}

private fun sanitizeFileName(name: String): String {
    val base = name.trim().ifBlank { "registro" }
    return base.replace(Regex("[^A-Za-z0-9_-]"), "_").take(40)
}
