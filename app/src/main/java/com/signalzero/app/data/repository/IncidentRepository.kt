package com.signalzero.app.data.repository

import com.signalzero.app.data.local.dao.IncidentDao
import com.signalzero.app.data.local.entity.IncidentEntity
import com.signalzero.app.data.model.DeliveryStatus
import com.signalzero.app.data.model.Incident
import com.signalzero.app.data.model.LocationData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class IncidentRepository(private val incidentDao: IncidentDao) {

    val allIncidents: Flow<List<Incident>> = incidentDao.getAllIncidents().map { list ->
        list.map { it.toDomain() }
    }

    val activeIncidents: Flow<List<Incident>> = incidentDao.getActiveIncidents().map { list ->
        list.map { it.toDomain() }
    }

    val recentIncidents: Flow<List<Incident>> = allIncidents

    fun observeIncident(id: String): Flow<Incident?> = incidentDao.observeIncidentById(id).map { it?.toDomain() }

    suspend fun getIncident(id: String): Incident? = incidentDao.getIncidentById(id)?.toDomain()

    suspend fun saveIncident(incident: Incident) {
        incidentDao.insertIncident(IncidentEntity.fromDomain(incident))
    }

    suspend fun updateDeliveryStatus(id: String, status: DeliveryStatus) {
        incidentDao.updateDeliveryStatus(id, status.name)
    }

    suspend fun updateLocation(id: String, location: LocationData) {
        if (location.hasValidCoordinates) {
            incidentDao.updateLocation(
                incidentId = id,
                lat = location.latitude!!,
                lng = location.longitude!!,
                accuracy = location.accuracyMeters ?: 0f,
                freshness = location.freshness.name
            )
        }
    }

    suspend fun cancelIncident(id: String) {
        incidentDao.cancelIncident(id)
    }

    suspend fun resolveIncident(id: String) {
        incidentDao.resolveIncident(id)
    }

    suspend fun getUnsyncedIncidents(): List<Incident> {
        return incidentDao.getUnsyncedIncidents().map { it.toDomain() }
    }

    suspend fun markSynced(id: String) {
        incidentDao.markSyncedToCloud(id)
    }
}
