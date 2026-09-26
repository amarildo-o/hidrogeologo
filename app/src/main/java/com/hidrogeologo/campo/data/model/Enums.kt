package com.hidrogeologo.campo.data.model

enum class RecordType(val label: String) {
    POZO("Pozo"),
    NORIA("Noria / pozo excavado"),
    MANANTIAL("Manantial"),
    OTRO("Otro")
}

enum class PhotoCategory(val label: String) {
    GENERAL("General"),
    INFRAESTRUCTURA("Infraestructura"),
    EQUIPO("Equipo electromecánico"),
    CALIDAD_AGUA("Calidad del agua"),
    HALLAZGO("Hallazgo")
}

enum class SyncStatus {
    PENDIENTE,
    SINCRONIZADO
}
