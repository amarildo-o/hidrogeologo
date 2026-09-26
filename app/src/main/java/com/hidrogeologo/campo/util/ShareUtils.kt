package com.hidrogeologo.campo.util

import android.content.Context
import android.content.Intent
import java.io.File

object ShareUtils {

    fun shareFile(context: Context, file: File, mimeType: String) {
        val uri = PhotoUtils.uriForFile(context, file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Compartir"))
    }
}
