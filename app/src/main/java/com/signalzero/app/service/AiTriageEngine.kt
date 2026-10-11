package com.signalzero.app.service

import com.signalzero.app.data.model.EmergencyCategory
import com.signalzero.app.data.model.Incident
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class TriageResult(
    val severityLevel: Int, // 1 (Critical), 2 (High), 3 (Medium), 4 (Low)
    val severityLabel: String,
    val rationale: String,
    val isClusterDuplicate: Boolean,
    val duplicateClusterIncidentId: String?,
    val recommendedAction: String
)

class AiTriageEngine {

    fun evaluateIncident(incident: Incident, existingIncidents: List<Incident>): TriageResult {
        val text = incident.message.lowercase()

        // 1. Critical keyword scanning
        val isLifeThreatening = text.contains("trapped") || text.contains("bleeding") ||
                text.contains("unconscious") || text.contains("heart") || text.contains("breathing") ||
                text.contains("फँसा") || text.contains("खून") || text.contains("बेहोश")

        val isHazardous = text.contains("fire") || text.contains("explosion") ||
                text.contains("flood") || text.contains("earthquake") || text.contains("collapse") ||
                text.contains("आग") || text.contains("बाढ़") || text.contains("भूकंप")

        val severityLevel = when {
            isLifeThreatening -> 1
            incident.category == EmergencyCategory.MEDICAL || incident.category == EmergencyCategory.TRAPPED -> 1
            isHazardous || incident.category == EmergencyCategory.FIRE || incident.category == EmergencyCategory.FLOOD -> 2
            incident.category == EmergencyCategory.PERSONAL_SAFETY -> 2
            else -> 3
        }

        val severityLabel = when (severityLevel) {
            1 -> "CRITICAL (Immediate Life Threat)"
            2 -> "HIGH (Active Hazard / Urgent)"
            3 -> "MEDIUM (Standard Emergency)"
            else -> "LOW (Advisory)"
        }

        val rationale = buildString {
            append("Category: ${incident.category.name}. ")
            if (isLifeThreatening) append("Life-threatening indicators detected in report. ")
            if (isHazardous) append("Environmental hazard detected. ")
            if (incident.latitude != null && incident.longitude != null) {
                append("Verified GPS location attached. ")
            } else {
                append("No GPS fix available. ")
            }
        }

        // 2. Spatial & Temporal Duplicate Clustering
        var isDuplicate = false
        var duplicateParentId: String? = null

        if (incident.latitude != null && incident.longitude != null) {
            for (other in existingIncidents) {
                if (other.id == incident.id) continue
                if (other.latitude != null && other.longitude != null) {
                    val distanceMeters = calculateHaversineDistance(
                        incident.latitude, incident.longitude,
                        other.latitude, other.longitude
                    )
                    val timeDiffMinutes = Math.abs(incident.createdAt - other.createdAt) / 60000L

                    // Clustered if within 500m and 30 minutes of same emergency category
                    if (distanceMeters < 500.0 && timeDiffMinutes < 30L && other.category == incident.category) {
                        isDuplicate = true
                        duplicateParentId = other.id
                        break
                    }
                }
            }
        }

        val action = when (severityLevel) {
            1 -> "Dispatch nearest paramedic or search-and-rescue team immediately."
            2 -> "Dispatch fire/rescue units and alert regional disaster coordinator."
            else -> "Log incident to community mesh queue and monitor peer acknowledgements."
        }

        return TriageResult(
            severityLevel = severityLevel,
            severityLabel = severityLabel,
            rationale = rationale,
            isClusterDuplicate = isDuplicate,
            duplicateClusterIncidentId = duplicateParentId,
            recommendedAction = action
        )
    }

    private fun calculateHaversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371000.0 // Earth radius in meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}
