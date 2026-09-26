package com.hidrogeologo.campo.ui.form

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hidrogeologo.campo.data.model.AdministrativeInfo
import com.hidrogeologo.campo.data.model.ElectromechanicalComponents
import com.hidrogeologo.campo.data.model.HydraulicParameters
import com.hidrogeologo.campo.data.model.InfrastructureStatus
import com.hidrogeologo.campo.data.model.RecordPhoto
import com.hidrogeologo.campo.data.model.RecordType
import com.hidrogeologo.campo.data.model.WaterQuality
import com.hidrogeologo.campo.data.model.WellRecord
import com.hidrogeologo.campo.data.repository.FieldRecordRepository
import com.hidrogeologo.campo.ui.components.PendingPhoto
import com.hidrogeologo.campo.ui.navigation.Screen
import com.hidrogeologo.campo.util.LocationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

class RecordFormViewModel(
    application: Application,
    private val repository: FieldRecordRepository,
    initialRecordId: Long
) : ViewModel() {

    private val locationHelper = LocationHelper(application)

    private val existingRecordId: Long? =
        if (initialRecordId == Screen.RecordForm.NEW_RECORD_ID) null else initialRecordId

    private val _recordId = MutableStateFlow(existingRecordId)

    private val _record = MutableStateFlow(WellRecord())
    val record: StateFlow<WellRecord> = _record

    private val _pendingPhotos = MutableStateFlow<List<PendingPhoto>>(emptyList())
    val pendingPhotos: StateFlow<List<PendingPhoto>> = _pendingPhotos

    val savedPhotos: StateFlow<List<RecordPhoto>> = _recordId
        .flatMapLatest { id -> if (id == null) flowOf(emptyList()) else repository.observePhotos(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isFetchingLocation = MutableStateFlow(false)
    val isFetchingLocation: StateFlow<Boolean> = _isFetchingLocation

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving

    init {
        if (existingRecordId != null) {
            viewModelScope.launch {
                repository.getRecord(existingRecordId)?.let { _record.value = it }
            }
        }
    }

    fun setRecordType(type: RecordType) = _record.update { it.copy(recordType = type) }

    fun updateAdministrative(update: (AdministrativeInfo) -> AdministrativeInfo) =
        _record.update { it.copy(administrative = update(it.administrative)) }

    fun updateInfrastructure(update: (InfrastructureStatus) -> InfrastructureStatus) =
        _record.update { it.copy(infrastructure = update(it.infrastructure)) }

    fun updateHydraulic(update: (HydraulicParameters) -> HydraulicParameters) =
        _record.update { it.copy(hydraulic = update(it.hydraulic)) }

    fun updateElectromechanical(update: (ElectromechanicalComponents) -> ElectromechanicalComponents) =
        _record.update { it.copy(electromechanical = update(it.electromechanical)) }

    fun updateWaterQuality(update: (WaterQuality) -> WaterQuality) =
        _record.update { it.copy(waterQuality = update(it.waterQuality)) }

    fun updateObservations(text: String) = _record.update { it.copy(observations = text) }

    fun captureLocation() {
        viewModelScope.launch {
            _isFetchingLocation.value = true
            val location = locationHelper.getCurrentLocation()
            if (location != null) {
                updateAdministrative {
                    it.copy(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        altitudeMeters = if (location.hasAltitude()) location.altitude else it.altitudeMeters,
                        locationAccuracyMeters = if (location.hasAccuracy()) location.accuracy else it.locationAccuracyMeters
                    )
                }
            }
            _isFetchingLocation.value = false
        }
    }

    fun addPendingPhoto(photo: PendingPhoto) {
        _pendingPhotos.update { it + photo }
    }

    fun removePendingPhoto(photo: PendingPhoto) {
        _pendingPhotos.update { it - photo }
        File(photo.filePath).delete()
    }

    fun removeSavedPhoto(photo: RecordPhoto) {
        viewModelScope.launch {
            repository.deletePhoto(photo)
            File(photo.filePath).delete()
        }
    }

    fun save(onSaved: (Long) -> Unit) {
        viewModelScope.launch {
            _isSaving.value = true
            val toSave = _record.value.copy(id = _recordId.value ?: 0L)
            val savedId = repository.saveRecord(toSave)
            _recordId.value = savedId

            _pendingPhotos.value.forEach { pending ->
                repository.addPhoto(
                    RecordPhoto(
                        recordId = savedId,
                        filePath = pending.filePath,
                        category = pending.category
                    )
                )
            }
            _pendingPhotos.value = emptyList()

            _isSaving.value = false
            onSaved(savedId)
        }
    }
}
