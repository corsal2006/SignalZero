package com.signalzero.app.service

import android.content.Context
import android.util.Log
import com.signalzero.app.core.AppLanguage
import com.signalzero.app.data.model.DeliveryStatus
import com.signalzero.app.data.model.EmergencyCategory
import com.signalzero.app.data.model.Incident
import com.signalzero.app.data.model.LocationData
import com.signalzero.app.data.model.LocationFreshness
import com.signalzero.app.data.preferences.UserPreferences
import com.signalzero.app.data.repository.ContactRepository
import com.signalzero.app.data.repository.IncidentRepository
import com.signalzero.app.mesh.MeshRouter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class EmergencyOrchestrator(
    private val context: Context,
    private val incidentRepository: IncidentRepository,
    private val contactRepository: ContactRepository,
    private val locationService: LocationService,
    private val contactNotificationService: ContactNotificationService,
    private val meshRouter: MeshRouter,
    private val firebaseSyncService: FirebaseSyncService,
    private val userPreferences: UserPreferences,
    private val aiTriageEngine: AiTriageEngine,
    private val scope: CoroutineScope
) {
    private val tag = "SignalZero_Orchestrator"

    private val _activeIncident = MutableStateFlow<Incident?>(null)
    val activeIncident: StateFlow<Incident?> = _activeIncident.asStateFlow()

    private var lastTriggerTime = 0L
    private val debounceCooldownMs = 5000L

    init {
        // Collect latest active incident on start
        scope.launch(Dispatchers.IO) {
            incidentRepository.activeIncidents.collect { list ->
                if (list.isNotEmpty()) {
                    _activeIncident.value = list.first()
                }
            }
        }
    }

    fun triggerEmergency(
        category: EmergencyCategory = EmergencyCategory.OTHER,
        triggerSource: String = "MANUAL",
        customNote: String? = null
    ): String {
        val now = System.currentTimeMillis()
        val timestampFormat = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US)
        val incidentId = "INC-${timestampFormat.format(Date(now))}-${UUID.randomUUID().toString().take(4).uppercase()}"

        if (now - lastTriggerTime < debounceCooldownMs) {
            Log.w(tag, "Emergency trigger suppressed by 5s debounce cooldown.")
            return _activeIncident.value?.id ?: incidentId
        }
        lastTriggerTime = now

        scope.launch(Dispatchers.IO) {
            val language = userPreferences.languageFlow.first()
            val nodeId = userPreferences.nodePseudonymFlow.first()

            // 1. Obtain location with bounded timeout (8s)
            val location: LocationData = try {
                locationService.getCurrentLocation(8000L)
            } catch (e: Exception) {
                LocationData.unavailable(LocationFreshness.UNAVAILABLE)
            }

            // 2. Build authoritative localized prewritten message
            val baseMessage = category.getTemplateMessage(language)
            val fullMessage = buildString {
                append(baseMessage)
                if (!customNote.isNullOrBlank()) {
                    append("\nNote: $customNote")
                }
                if (location.hasValidCoordinates) {
                    append("\nLocation: ${location.mapsUrl} (Accuracy: ±${location.accuracyMeters?.toInt()}m)")
                }
            }

            // 3. Persist incident to Room BEFORE any network operation
            val initialIncident = Incident(
                id = incidentId,
                category = category,
                triggerSource = triggerSource,
                message = fullMessage,
                createdAt = now,
                updatedAt = now,
                latitude = location.latitude,
                longitude = location.longitude,
                accuracyMeters = location.accuracyMeters,
                locationFreshness = location.freshness,
                deliveryStatus = DeliveryStatus.SAVED_LOCALLY,
                hopCount = 0,
                notes = customNote
            )

            // 4. Run explainable offline AI triage
            val allExisting = incidentRepository.allIncidents.first()
            val triage = aiTriageEngine.evaluateIncident(initialIncident, allExisting)
            val evaluatedIncident = initialIncident.copy(
                severityLevel = triage.severityLevel,
                aiTriageRationale = triage.rationale
            )

            incidentRepository.saveIncident(evaluatedIncident)
            _activeIncident.value = evaluatedIncident
            Log.i(tag, "Emergency Incident $incidentId SAVED LOCALLY.")

            // 5. Enqueue into BLE Mesh Router
            try {
                meshRouter.enqueueIncident(evaluatedIncident, nodeId)
            } catch (e: Exception) {
                Log.w(tag, "Mesh enqueue failed: ${e.message}")
            }

            // 6. Notify selected trusted contacts via SMS / Share
            val contacts = contactRepository.allContacts.first()
            if (contacts.isNotEmpty()) {
                contactNotificationService.dispatchEmergencyAlert(evaluatedIncident, contacts)
            }

            // 7. Sync to Firebase if internet is active
            if (firebaseSyncService.isOnline()) {
                try {
                    firebaseSyncService.syncPendingIncidents()
                    incidentRepository.updateDeliveryStatus(incidentId, DeliveryStatus.RECEIVED_BY_SERVER)
                } catch (e: Exception) {
                    Log.w(tag, "Firebase sync deferred: ${e.message}")
                }
            }
        }
        return incidentId
    }

    fun triggerSos(
        category: EmergencyCategory = EmergencyCategory.OTHER,
        triggerSource: String = "MANUAL",
        optionalNote: String? = null
    ): String = triggerEmergency(category, triggerSource, optionalNote)

    fun cancelEmergency(incidentId: String) {
        scope.launch(Dispatchers.IO) {
            incidentRepository.cancelIncident(incidentId)
            if (_activeIncident.value?.id == incidentId) {
                _activeIncident.value = null
            }
        }
    }

    fun resolveEmergency(incidentId: String) {
        scope.launch(Dispatchers.IO) {
            incidentRepository.resolveIncident(incidentId)
            if (_activeIncident.value?.id == incidentId) {
                _activeIncident.value = null
            }
        }
    }
}
