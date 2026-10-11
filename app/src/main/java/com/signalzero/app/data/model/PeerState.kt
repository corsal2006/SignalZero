package com.signalzero.app.data.model

enum class PeerState(val label: String, val isRoutable: Boolean) {
    DISCOVERING("Discovering", false),
    AVAILABLE("Available", true),
    CONNECTING("Connecting", false),
    CONNECTED("Connected", true),
    RELAYING("Relaying", true),
    SUSPECT("Suspect (Heartbeat Missed)", false),
    UNAVAILABLE("Unavailable (Disconnected)", false),
    RECOVERING("Recovering Route", false),
    EXPIRED("Expired", false);

    companion object {
        fun fromString(value: String): PeerState {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: AVAILABLE
        }
    }
}
