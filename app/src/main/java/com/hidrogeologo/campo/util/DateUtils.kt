package com.hidrogeologo.campo.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {

    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale("es", "MX"))
    private val dateTimeFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "MX"))

    fun formatDate(millis: Long): String = dateFormatter.format(Date(millis))

    fun formatDateTime(millis: Long): String = dateTimeFormatter.format(Date(millis))
}
