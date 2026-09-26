package com.hidrogeologo.campo.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hidrogeologo.campo.data.model.WellRecord
import com.hidrogeologo.campo.data.repository.FieldRecordRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RecordListViewModel(private val repository: FieldRecordRepository) : ViewModel() {

    val records: StateFlow<List<WellRecord>> = repository.observeRecords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteRecord(record: WellRecord) {
        viewModelScope.launch { repository.deleteRecord(record) }
    }
}
