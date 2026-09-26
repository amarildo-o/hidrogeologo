package com.hidrogeologo.campo.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.hidrogeologo.campo.data.dao.RecordPhotoDao
import com.hidrogeologo.campo.data.dao.WellRecordDao
import com.hidrogeologo.campo.data.model.RecordPhoto
import com.hidrogeologo.campo.data.model.WellRecord

@Database(
    entities = [WellRecord::class, RecordPhoto::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun wellRecordDao(): WellRecordDao
    abstract fun recordPhotoDao(): RecordPhotoDao

    companion object {
        private const val DATABASE_NAME = "hidrocampo.db"

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                ).build().also { instance = it }
            }
    }
}
