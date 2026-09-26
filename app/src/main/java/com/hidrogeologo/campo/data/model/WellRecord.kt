package com.hidrogeologo.campo.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "well_records")
data class WellRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val recordType: RecordType = RecordType.POZO,

    @Embedded(prefix = "admin_")
    val administrative: AdministrativeInfo = AdministrativeInfo(),

    @Embedded(prefix = "infra_")
    val infrastructure: InfrastructureStatus = InfrastructureStatus(),

    @Embedded(prefix = "hyd_")
    val hydraulic: HydraulicParameters = HydraulicParameters(),

    @Embedded(prefix = "elec_")
    val electromechanical: ElectromechanicalComponents = ElectromechanicalComponents(),

    @Embedded(prefix = "wq_")
    val waterQuality: WaterQuality = WaterQuality(),

    val observations: String = "",

    val createdAtMillis: Long = System.currentTimeMillis(),
    val updatedAtMillis: Long = System.currentTimeMillis()
)
