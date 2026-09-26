package com.hidrogeologo.campo.data.model

/** Datos administrativos y de ubicación del punto de agua. */
data class AdministrativeInfo(
    val siteName: String = "",
    val folio: String = "",
    val owner: String = "",
    val community: String = "",
    val municipality: String = "",
    val state: String = "",
    val technician: String = "",
    val visitDateMillis: Long = System.currentTimeMillis(),
    val latitude: Double? = null,
    val longitude: Double? = null,
    val altitudeMeters: Double? = null,
    val locationAccuracyMeters: Float? = null,
    val accessReference: String = ""
)

/** Estado físico de la infraestructura del pozo o manantial. */
data class InfrastructureStatus(
    val wellheadCondition: String = "",
    val hasFencing: Boolean = false,
    val hasSanitarySlab: Boolean = false,
    val hasProtectiveCasing: Boolean = false,
    val hasLock: Boolean = false,
    val hasPumpHouse: Boolean = false,
    val accessRoadCondition: String = "",
    val protectionRadiusMeters: Double? = null,
    val generalCondition: String = "",
    val infrastructureNotes: String = ""
)

/** Parámetros hidráulicos y constructivos del pozo. */
data class HydraulicParameters(
    val totalDepthMeters: Double? = null,
    val diameterInches: Double? = null,
    val staticWaterLevelMeters: Double? = null,
    val dynamicWaterLevelMeters: Double? = null,
    val pumpingFlowRateLps: Double? = null,
    val pumpingTestDurationHours: Double? = null,
    val specificCapacity: Double? = null,
    val aquiferType: String = "",
    val casingMaterial: String = "",
    val screenInterval: String = "",
    val waterUse: String = ""
)

/** Componentes electromecánicos instalados (equipo de bombeo). */
data class ElectromechanicalComponents(
    val hasPumpingEquipment: Boolean = false,
    val pumpType: String = "",
    val pumpBrand: String = "",
    val pumpModel: String = "",
    val motorPowerHp: Double? = null,
    val voltage: String = "",
    val phase: String = "",
    val energySource: String = "",
    val hasControlPanel: Boolean = false,
    val hasGenerator: Boolean = false,
    val installedPumpDepthMeters: Double? = null,
    val columnPipeDiameterInches: Double? = null,
    val electromechanicalNotes: String = ""
)

/** Parámetros fisicoquímicos medidos en campo. */
data class WaterQuality(
    val phValue: Double? = null,
    val electricalConductivityUsCm: Double? = null,
    val totalDissolvedSolidsMgL: Double? = null,
    val temperatureCelsius: Double? = null,
    val turbidityNtu: Double? = null,
    val dissolvedOxygenMgL: Double? = null,
    val freeChlorineMgL: Double? = null,
    val odor: String = "",
    val color: String = "",
    val appearance: String = ""
)
