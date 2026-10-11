package com.signalzero.app

import com.signalzero.app.data.model.EmergencyCategory
import com.signalzero.app.data.model.Incident
import com.signalzero.app.data.model.LocationData
import com.signalzero.app.service.AiTriageEngine
import org.junit.Assert.*
import org.junit.Test

class AiTriageEngineTest {

    private val triageEngine = AiTriageEngine()

    @Test
    fun testSeverityScoringPrioritization() {
        val trappedIncident = Incident(
            id = "inc-1",
            category = EmergencyCategory.TRAPPED,
            message = "I am trapped under debris",
            createdAt = System.currentTimeMillis()
        )
        val medicalIncident = Incident(
            id = "inc-2",
            category = EmergencyCategory.MEDICAL,
            message = "Severe injury medical emergency",
            createdAt = System.currentTimeMillis()
        )
        val otherIncident = Incident(
            id = "inc-3",
            category = EmergencyCategory.OTHER,
            message = "Minor request",
            createdAt = System.currentTimeMillis()
        )

        val trappedResult = triageEngine.evaluateIncident(trappedIncident, emptyList())
        val medicalResult = triageEngine.evaluateIncident(medicalIncident, emptyList())
        val otherResult = triageEngine.evaluateIncident(otherIncident, emptyList())

        assertEquals("TRAPPED must be Critical level (1)", 1, trappedResult.severityLevel)
        assertEquals("MEDICAL must be Critical level (1)", 1, medicalResult.severityLevel)
        assertTrue("OTHER must have lower priority (> 1)", otherResult.severityLevel > 1)
    }

    @Test
    fun testDuplicateIncidentClustering() {
        val baseTime = System.currentTimeMillis()
        val locA = LocationData(latitude = 28.6139, longitude = 77.2090, accuracyMeters = 5.0f, fixTimestamp = baseTime)
        // 30 meters away
        val locNearby = LocationData(latitude = 28.6141, longitude = 77.2092, accuracyMeters = 6.0f, fixTimestamp = baseTime + 1000)
        // 5 km away
        val locDistant = LocationData(latitude = 28.6500, longitude = 77.2500, accuracyMeters = 5.0f, fixTimestamp = baseTime + 1000)

        val incidentOriginal = Incident(
            id = "inc-orig",
            category = EmergencyCategory.FIRE,
            message = "Fire detected",
            createdAt = baseTime,
            latitude = locA.latitude,
            longitude = locA.longitude
        )

        val incidentDuplicate = Incident(
            id = "inc-dup",
            category = EmergencyCategory.FIRE,
            message = "Smoke and fire nearby",
            createdAt = baseTime + 60_000, // 1 minute later
            latitude = locNearby.latitude,
            longitude = locNearby.longitude
        )

        val incidentDistant = Incident(
            id = "inc-dist",
            category = EmergencyCategory.FIRE,
            message = "Another fire far away",
            createdAt = baseTime + 60_000,
            latitude = locDistant.latitude,
            longitude = locDistant.longitude
        )

        val dupResult = triageEngine.evaluateIncident(incidentDuplicate, listOf(incidentOriginal))
        assertTrue("Incidents at same location and time must be flagged as duplicates", dupResult.isClusterDuplicate)
        assertEquals("Parent incident ID should match", incidentOriginal.id, dupResult.duplicateClusterIncidentId)

        val distResult = triageEngine.evaluateIncident(incidentDistant, listOf(incidentOriginal))
        assertFalse("Incidents kilometers apart must NOT be clustered as duplicates", distResult.isClusterDuplicate)
    }
}
