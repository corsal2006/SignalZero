package com.signalzero.app.data.model

data class Incident(
    val id: String,
    val ownerId: String? = null,
    val category: EmergencyCategory = EmergencyCategory.OTHER,
    val triggerSource: String = "MANUAL",
    val message: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val latitude: Double? = null,
    val longitude: Double? = null,
    val accuracyMeters: Float? = null,
    val locationFreshness: LocationFreshness = LocationFreshness.UNAVAILABLE,
    val deliveryStatus: DeliveryStatus = DeliveryStatus.SAVED_LOCALLY,
    val meshPeerId: String? = null,
    val hopCount: Int = 0,
    val isResolved: Boolean = false,
    val isCancelled: Boolean = false,
    val notes: String? = null,
    val severityLevel: Int = 2,
    val aiTriageRationale: String? = null,
    val syncedToCloud: Boolean = false
) {
    val mapsUrl: String?
        get() = if (latitude != null && longitude != null && !(latitude == 0.0 && longitude == 0.0)) {
            "https://maps.google.com/?q=$latitude,$longitude"
        } else null
}
