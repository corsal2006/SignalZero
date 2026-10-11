package com.signalzero.app.service

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.signalzero.app.core.Constants
import com.signalzero.app.data.model.LocationData
import com.signalzero.app.data.model.LocationFreshness
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

class LocationService(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    fun hasLocationPermission(): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fineLocation || coarseLocation
    }

    fun isGpsEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        return locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true ||
                locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true
    }

    private fun isValidLocation(location: Location?): Boolean {
        if (location == null) return false
        val lat = location.latitude
        val lng = location.longitude
        if (lat.isNaN() || lng.isNaN()) return false
        if (lat < -90.0 || lat > 90.0) return false
        if (lng < -180.0 || lng > 180.0) return false
        if (lat == 0.0 && lng == 0.0) return false
        return true
    }

    private fun isFreshLocation(location: Location): Boolean {
        val ageMs = System.currentTimeMillis() - location.time
        return ageMs in 0..Constants.FRESH_LOCATION_MAX_AGE_MS &&
                location.accuracy <= Constants.FRESH_LOCATION_MAX_ACCURACY_M
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(timeoutMs: Long = Constants.LOCATION_TIMEOUT_MS): LocationData {
        if (!hasLocationPermission()) {
            return LocationData.unavailable(LocationFreshness.PERMISSION_DENIED)
        }

        if (!isGpsEnabled()) {
            return LocationData.unavailable(LocationFreshness.GPS_DISABLED)
        }

        return try {
            val cancellationTokenSource = CancellationTokenSource()

            // 1. Request current high accuracy location with timeout
            val location: Location? = withTimeoutOrNull(timeoutMs) {
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cancellationTokenSource.token
                ).await()
            }

            if (location != null && isValidLocation(location)) {
                val freshness = if (isFreshLocation(location)) LocationFreshness.FRESH else LocationFreshness.STALE
                LocationData(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    accuracyMeters = location.accuracy,
                    fixTimestamp = location.time,
                    freshness = freshness,
                    provider = location.provider
                )
            } else {
                // 2. Fallback to recent last-known location
                val lastKnown: Location? = try {
                    fusedLocationClient.lastLocation.await()
                } catch (e: Exception) {
                    null
                }

                if (lastKnown != null && isValidLocation(lastKnown)) {
                    val freshness = if (isFreshLocation(lastKnown)) LocationFreshness.LAST_KNOWN else LocationFreshness.STALE
                    LocationData(
                        latitude = lastKnown.latitude,
                        longitude = lastKnown.longitude,
                        accuracyMeters = lastKnown.accuracy,
                        fixTimestamp = lastKnown.time,
                        freshness = freshness,
                        provider = lastKnown.provider
                    )
                } else {
                    LocationData.unavailable(LocationFreshness.UNAVAILABLE)
                }
            }
        } catch (e: SecurityException) {
            LocationData.unavailable(LocationFreshness.PERMISSION_DENIED)
        } catch (e: Exception) {
            LocationData.unavailable(LocationFreshness.UNAVAILABLE)
        }
    }
}
