package com.signalzero.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.signalzero.app.data.local.entity.IncidentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IncidentDao {
    @Query("SELECT * FROM incidents ORDER BY createdAt DESC")
    fun getAllIncidents(): Flow<List<IncidentEntity>>

    @Query("SELECT * FROM incidents WHERE isResolved = 0 AND isCancelled = 0 ORDER BY createdAt DESC")
    fun getActiveIncidents(): Flow<List<IncidentEntity>>

    @Query("SELECT * FROM incidents WHERE id = :incidentId LIMIT 1")
    suspend fun getIncidentById(incidentId: String): IncidentEntity?

    @Query("SELECT * FROM incidents WHERE id = :incidentId LIMIT 1")
    fun observeIncidentById(incidentId: String): Flow<IncidentEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncident(incident: IncidentEntity)

    @Update
    suspend fun updateIncident(incident: IncidentEntity)

    @Query("UPDATE incidents SET deliveryStatus = :status, updatedAt = :updatedAt WHERE id = :incidentId")
    suspend fun updateDeliveryStatus(incidentId: String, status: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE incidents SET latitude = :lat, longitude = :lng, accuracyMeters = :accuracy, locationFreshness = :freshness, updatedAt = :updatedAt WHERE id = :incidentId")
    suspend fun updateLocation(incidentId: String, lat: Double, lng: Double, accuracy: Float, freshness: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE incidents SET isCancelled = 1, deliveryStatus = 'CANCELLED', updatedAt = :updatedAt WHERE id = :incidentId")
    suspend fun cancelIncident(incidentId: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE incidents SET isResolved = 1, deliveryStatus = 'RESOLVED', updatedAt = :updatedAt WHERE id = :incidentId")
    suspend fun resolveIncident(incidentId: String, updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT * FROM incidents WHERE syncedToCloud = 0")
    suspend fun getUnsyncedIncidents(): List<IncidentEntity>

    @Query("UPDATE incidents SET syncedToCloud = 1 WHERE id = :incidentId")
    suspend fun markSyncedToCloud(incidentId: String)
}
