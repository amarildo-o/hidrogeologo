package com.hidrogeologo.campo.data

import androidx.room.TypeConverter
import com.hidrogeologo.campo.data.model.PhotoCategory
import com.hidrogeologo.campo.data.model.RecordType

class Converters {

    @TypeConverter
    fun fromRecordType(value: RecordType): String = value.name

    @TypeConverter
    fun toRecordType(value: String): RecordType =
        RecordType.entries.firstOrNull { it.name == value } ?: RecordType.POZO

    @TypeConverter
    fun fromPhotoCategory(value: PhotoCategory): String = value.name

    @TypeConverter
    fun toPhotoCategory(value: String): PhotoCategory =
        PhotoCategory.entries.firstOrNull { it.name == value } ?: PhotoCategory.GENERAL
}
