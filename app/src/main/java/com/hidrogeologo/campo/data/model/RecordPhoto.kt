package com.hidrogeologo.campo.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "record_photos",
    foreignKeys = [
        ForeignKey(
            entity = WellRecord::class,
            parentColumns = ["id"],
            childColumns = ["recordId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("recordId")]
)
data class RecordPhoto(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val recordId: Long,
    val filePath: String,
    val category: PhotoCategory = PhotoCategory.GENERAL,
    val caption: String = "",
    val takenAtMillis: Long = System.currentTimeMillis()
)
