package com.signalzero.app.data.model

import com.signalzero.app.core.AppLanguage
import com.signalzero.app.core.Localization

enum class DeliveryStatus(val translationKey: String, val order: Int) {
    SAVED_LOCALLY("status_saved_locally", 1),
    QUEUED_FOR_RELAY("status_queued_relay", 2),
    SEARCHING_PEERS("status_searching_peers", 3),
    ACCEPTED_BY_PEER("status_accepted_peer", 4),
    RELAYED_BY_PEER("status_relayed_peer", 5),
    RECEIVED_BY_SERVER("status_received_server", 6),
    ACKNOWLEDGED_BY_RESPONDER("status_acknowledged", 7),
    CANCELLED("status_cancelled", 8),
    RESOLVED("status_resolved", 9),
    DELIVERY_FAILED_EXPIRED("status_failed_expired", 10);

    fun getDisplayName(language: AppLanguage): String = Localization.get(translationKey, language)

    companion object {
        fun fromString(value: String): DeliveryStatus {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: SAVED_LOCALLY
        }
    }
}
