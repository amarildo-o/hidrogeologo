package com.hidrogeologo.campo

import android.app.Application
import com.hidrogeologo.campo.data.AppDatabase
import com.hidrogeologo.campo.data.repository.FieldRecordRepository

class HidroCampoApp : Application() {

    private val database by lazy { AppDatabase.getInstance(this) }

    val repository by lazy {
        FieldRecordRepository(database.wellRecordDao(), database.recordPhotoDao())
    }
}
