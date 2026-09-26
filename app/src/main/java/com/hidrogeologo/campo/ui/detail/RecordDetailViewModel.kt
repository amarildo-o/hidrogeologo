package com.hidrogeologo.campo.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hidrogeologo.campo.data.model.RecordPhoto
import com.hidrogeologo.campo.data.model.WellRecord
import com.hidrogeologo.campo.data.repository.FieldRecordRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RecordDetailViewModel(
    private val repository: FieldRecordRepository,
    private val recordId: Long
) : ViewModel() {

    val record: StateFlow<WellRecord?> = repository.observeRecord(recordId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val photos: StateFlow<List<RecordPhoto>> = repository.observePhotos(recordId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteRecord(onDeleted: () -> Unit) {
        val current = record.value ?: return
        viewModelScope.launch {
            repository.deleteRecord(current)
            onDeleted()
        }
    }
}
