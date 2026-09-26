package com.hidrogeologo.campo.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.hidrogeologo.campo.data.model.PhotoCategory
import com.hidrogeologo.campo.data.model.RecordPhoto
import com.hidrogeologo.campo.util.PhotoUtils

data class PendingPhoto(val filePath: String, val category: PhotoCategory)

@Composable
fun PhotoSection(
    savedPhotos: List<RecordPhoto>,
    pendingPhotos: List<PendingPhoto>,
    onPhotoCaptured: (PendingPhoto) -> Unit,
    onDeleteSaved: (RecordPhoto) -> Unit,
    onDeletePending: (PendingPhoto) -> Unit
) {
    val context = LocalContext.current
    var pendingFilePath by remember { mutableStateOf<String?>(null) }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val filePath = pendingFilePath
        if (success && filePath != null) {
            onPhotoCaptured(PendingPhoto(filePath = filePath, category = PhotoCategory.GENERAL))
        }
        pendingFilePath = null
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val file = PhotoUtils.createImageFile(context)
            val uri = PhotoUtils.uriForFile(context, file)
            pendingFilePath = file.absolutePath
            takePictureLauncher.launch(uri)
        }
    }

    Column {
        Text(
            "Fotografías del sitio, infraestructura, equipo, calidad de agua o hallazgos.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Button(onClick = {
            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        }) {
            Icon(Icons.Filled.AddAPhoto, contentDescription = null)
            Text(" Tomar fotografía", modifier = Modifier.padding(start = 8.dp))
        }

        PhotoGrid(
            savedPhotos = savedPhotos,
            pendingPhotos = pendingPhotos,
            onDeleteSaved = onDeleteSaved,
            onDeletePending = onDeletePending
        )
    }
}

private sealed class GalleryItem {
    data class Saved(val photo: RecordPhoto) : GalleryItem()
    data class Pending(val photo: PendingPhoto) : GalleryItem()
}

@Composable
private fun PhotoGrid(
    savedPhotos: List<RecordPhoto>,
    pendingPhotos: List<PendingPhoto>,
    onDeleteSaved: (RecordPhoto) -> Unit,
    onDeletePending: (PendingPhoto) -> Unit
) {
    if (savedPhotos.isEmpty() && pendingPhotos.isEmpty()) return

    val items: List<GalleryItem> =
        savedPhotos.map { GalleryItem.Saved(it) } + pendingPhotos.map { GalleryItem.Pending(it) }

    Column(modifier = Modifier.padding(top = 12.dp)) {
        items.chunked(3).forEach { rowItems ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                rowItems.forEachIndexed { index, item ->
                    if (index > 0) Spacer(modifier = Modifier.width(8.dp))
                    val path = when (item) {
                        is GalleryItem.Saved -> item.photo.filePath
                        is GalleryItem.Pending -> item.photo.filePath
                    }
                    PhotoThumbnail(
                        path = path,
                        modifier = Modifier.weight(1f),
                        onRemove = {
                            when (item) {
                                is GalleryItem.Saved -> onDeleteSaved(item.photo)
                                is GalleryItem.Pending -> onDeletePending(item.photo)
                            }
                        }
                    )
                }
                repeat(3 - rowItems.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun PhotoThumbnail(path: String, modifier: Modifier = Modifier, onRemove: () -> Unit) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
    ) {
        AsyncImage(
            model = path,
            contentDescription = null,
            modifier = Modifier.fillMaxWidth()
        )
        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(28.dp)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f), RoundedCornerShape(50))
        ) {
            Icon(Icons.Filled.Close, contentDescription = "Eliminar foto")
        }
    }
}
