package com.hidrogeologo.campo.data.repository

import com.hidrogeologo.campo.data.dao.RecordPhotoDao
import com.hidrogeologo.campo.data.dao.WellRecordDao
import com.hidrogeologo.campo.data.model.RecordPhoto
import com.hidrogeologo.campo.data.model.WellRecord
import kotlinx.coroutines.flow.Flow

class FieldRecordRepository(
    private val wellRecordDao: WellRecordDao,
    private val recordPhotoDao: RecordPhotoDao
) {

    fun observeRecords(): Flow<List<WellRecord>> = wellRecordDao.observeAll()

    fun observeRecord(recordId: Long): Flow<WellRecord?> = wellRecordDao.observeById(recordId)

    fun observePhotos(recordId: Long): Flow<List<RecordPhoto>> =
        recordPhotoDao.observeForRecord(recordId)

    suspend fun getRecord(recordId: Long): WellRecord? = wellRecordDao.getById(recordId)

    suspend fun saveRecord(record: WellRecord): Long {
        val toSave = record.copy(updatedAtMillis = System.currentTimeMillis())
        return if (toSave.id == 0L) {
            wellRecordDao.insert(toSave)
        } else {
            wellRecordDao.update(toSave)
            toSave.id
        }
    }

    suspend fun deleteRecord(record: WellRecord) = wellRecordDao.delete(record)

    suspend fun addPhoto(photo: RecordPhoto): Long = recordPhotoDao.insert(photo)

    suspend fun deletePhoto(photo: RecordPhoto) = recordPhotoDao.delete(photo)

    suspend fun getPhotos(recordId: Long): List<RecordPhoto> = recordPhotoDao.getForRecord(recordId)
}
