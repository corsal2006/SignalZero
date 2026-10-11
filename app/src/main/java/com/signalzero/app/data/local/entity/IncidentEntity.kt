package com.signalzero.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.signalzero.app.data.model.DeliveryStatus
import com.signalzero.app.data.model.EmergencyCategory
import com.signalzero.app.data.model.Incident
import com.signalzero.app.data.model.LocationFreshness

@Entity(tableName = "incidents")
data class IncidentEntity(
    @PrimaryKey val id: String,
    val ownerId: String?,
    val category: String,
    val triggerSource: String,
    val message: String,
    val createdAt: Long,
    val updatedAt: Long,
    val latitude: Double?,
    val longitude: Double?,
    val accuracyMeters: Float?,
    val locationFreshness: String,
    val deliveryStatus: String,
    val meshPeerId: String?,
    val hopCount: Int,
    val isResolved: Boolean,
    val isCancelled: Boolean,
    val notes: String?,
    val severityLevel: Int,
    val aiTriageRationale: String?,
    val syncedToCloud: Boolean
) {
    fun toDomain(): Incident {
        return Incident(
            id = id,
            ownerId = ownerId,
            category = EmergencyCategory.fromString(category),
            triggerSource = triggerSource,
            message = message,
            createdAt = createdAt,
            updatedAt = updatedAt,
            latitude = latitude,
            longitude = longitude,
            accuracyMeters = accuracyMeters,
            locationFreshness = LocationFreshness.valueOf(locationFreshness),
            deliveryStatus = DeliveryStatus.fromString(deliveryStatus),
            meshPeerId = meshPeerId,
            hopCount = hopCount,
            isResolved = isResolved,
            isCancelled = isCancelled,
            notes = notes,
            severityLevel = severityLevel,
            aiTriageRationale = aiTriageRationale,
            syncedToCloud = syncedToCloud
        )
    }

    companion object {
        fun fromDomain(domain: Incident): IncidentEntity {
            return IncidentEntity(
                id = domain.id,
                ownerId = domain.ownerId,
                category = domain.category.name,
                triggerSource = domain.triggerSource,
                message = domain.message,
                createdAt = domain.createdAt,
                updatedAt = domain.updatedAt,
                latitude = domain.latitude,
                longitude = domain.longitude,
                accuracyMeters = domain.accuracyMeters,
                locationFreshness = domain.locationFreshness.name,
                deliveryStatus = domain.deliveryStatus.name,
                meshPeerId = domain.meshPeerId,
                hopCount = domain.hopCount,
                isResolved = domain.isResolved,
                isCancelled = domain.isCancelled,
                notes = domain.notes,
                severityLevel = domain.severityLevel,
                aiTriageRationale = domain.aiTriageRationale,
                syncedToCloud = domain.syncedToCloud
            )
        }
    }
}
