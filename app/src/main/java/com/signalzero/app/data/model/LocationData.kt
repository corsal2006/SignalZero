package com.signalzero.app.data.model

import com.signalzero.app.core.AppLanguage
import com.signalzero.app.core.Localization

enum class LocationFreshness(val translationKey: String) {
    FRESH("loc_fresh"),
    STALE("loc_stale"),
    LAST_KNOWN("loc_last_known"),
    UNAVAILABLE("loc_unavailable"),
    PERMISSION_DENIED("loc_denied"),
    GPS_DISABLED("loc_unavailable");

    fun getDisplayName(language: AppLanguage): String = Localization.get(translationKey, language)
}

data class LocationData(
    val latitude: Double? = null,
    val longitude: Double? = null,
    val accuracyMeters: Float? = null,
    val fixTimestamp: Long = 0L,
    val freshness: LocationFreshness = LocationFreshness.UNAVAILABLE,
    val provider: String? = null
) {
    val hasValidCoordinates: Boolean
        get() = latitude != null && longitude != null &&
                !latitude.isNaN() && !longitude.isNaN() &&
                latitude in -90.0..90.0 && longitude in -180.0..180.0 &&
                !(latitude == 0.0 && longitude == 0.0)

    val mapsUrl: String?
        get() = if (hasValidCoordinates) "https://maps.google.com/?q=$latitude,$longitude" else null

    companion object {
        fun unavailable(freshness: LocationFreshness = LocationFreshness.UNAVAILABLE): LocationData {
            return LocationData(freshness = freshness)
        }
    }
}
