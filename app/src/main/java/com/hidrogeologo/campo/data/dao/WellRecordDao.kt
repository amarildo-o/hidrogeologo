package com.hidrogeologo.campo.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.hidrogeologo.campo.data.model.WellRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface WellRecordDao {

    @Query("SELECT * FROM well_records ORDER BY updatedAtMillis DESC")
    fun observeAll(): Flow<List<WellRecord>>

    @Query("SELECT * FROM well_records WHERE id = :recordId")
    fun observeById(recordId: Long): Flow<WellRecord?>

    @Query("SELECT * FROM well_records WHERE id = :recordId")
    suspend fun getById(recordId: Long): WellRecord?

    @Insert
    suspend fun insert(record: WellRecord): Long

    @Update
    suspend fun update(record: WellRecord)

    @Delete
    suspend fun delete(record: WellRecord)
}
