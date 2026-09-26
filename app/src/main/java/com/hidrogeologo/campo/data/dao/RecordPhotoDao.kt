package com.hidrogeologo.campo.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.hidrogeologo.campo.data.model.RecordPhoto
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordPhotoDao {

    @Query("SELECT * FROM record_photos WHERE recordId = :recordId ORDER BY takenAtMillis ASC")
    fun observeForRecord(recordId: Long): Flow<List<RecordPhoto>>

    @Query("SELECT * FROM record_photos WHERE recordId = :recordId ORDER BY takenAtMillis ASC")
    suspend fun getForRecord(recordId: Long): List<RecordPhoto>

    @Insert
    suspend fun insert(photo: RecordPhoto): Long

    @Delete
    suspend fun delete(photo: RecordPhoto)
}
