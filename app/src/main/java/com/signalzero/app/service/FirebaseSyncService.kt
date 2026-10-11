package com.signalzero.app.service

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.signalzero.app.data.model.Incident
import com.signalzero.app.data.repository.IncidentRepository
import kotlinx.coroutines.tasks.await

class FirebaseSyncService(
    private val context: Context,
    private val incidentRepository: IncidentRepository
) {
    private val tag = "SignalZero_Firebase"
    private val auth by lazy { try { FirebaseAuth.getInstance() } catch (e: Exception) { null } }
    private val firestore by lazy { try { FirebaseFirestore.getInstance() } catch (e: Exception) { null } }

    fun isOnline(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    suspend fun syncPendingIncidents(): Int {
        if (!isOnline()) return 0
        val db = firestore ?: return 0
        val unsynced = incidentRepository.getUnsyncedIncidents()
        var syncedCount = 0

        for (incident in unsynced) {
            try {
                val data = hashMapOf(
                    "id" to incident.id,
                    "category" to incident.category.name,
                    "message" to incident.message,
                    "triggerSource" to incident.triggerSource,
                    "createdAt" to incident.createdAt,
                    "updatedAt" to incident.updatedAt,
                    "latitude" to incident.latitude,
                    "longitude" to incident.longitude,
                    "accuracy" to incident.accuracyMeters,
                    "deliveryStatus" to incident.deliveryStatus.name,
                    "hopCount" to incident.hopCount,
                    "isResolved" to incident.isResolved,
                    "isCancelled" to incident.isCancelled,
                    "severityLevel" to incident.severityLevel,
                    "userId" to (auth?.currentUser?.uid ?: "anonymous_rescuer")
                )

                db.collection("incidents").document(incident.id)
                    .set(data, SetOptions.merge())
                    .await()

                incidentRepository.markSynced(incident.id)
                syncedCount++
                Log.i(tag, "Incident ${incident.id} synced to Cloud Firestore.")
            } catch (e: Exception) {
                Log.w(tag, "Failed to sync incident ${incident.id}: ${e.message}")
            }
        }
        return syncedCount
    }
}
