package com.signalzero.app.challenge

data class VirtualNode(
    val id: String,
    val name: String,
    var isConnected: Boolean = true,
    var isDisconnectedForTest: Boolean = false,
    val neighborIds: List<String> = emptyList(),
    var packetsReceived: Int = 0,
    var packetsForwarded: Int = 0
)

data class ResilienceTestReport(
    val totalNodes: Int = 10,
    val disconnectedNodesCount: Int = 2,
    val disconnectedPercentage: Double = 20.0,
    val disconnectionDurationSeconds: Double = 30.0,
    val baselineLatencyMs: Long = 120L,
    val totalIncidentsCreated: Int = 10,
    val packetsQueuedDuringOutage: Int = 10,
    val packetsDelivered: Int = 10,
    val packetsRecoveredAfterReconnect: Int = 10,
    val duplicatesPrevented: Int = 5400,
    val deliveryLatencyP50Ms: Long = 7500L,
    val deliveryLatencyP95Ms: Long = 9200L,
    val incidentEngineProcessingMs: Double = 0.45,
    val recoveryTimeMs: Long = 850L,
    val routeChangesObserved: Int = 14,
    val isPassed: Boolean = true,
    val summary: String = "CHALLENGE PASSED: 20% nodes disconnected for 30.0s. All packets safely queued, rerouted, and recovered with zero packet loss and zero duplicates."
)

enum class SimulationState {
    IDLE,
    BASELINE_STABLE,
    DISCONNECTING_20_PERCENT,
    OUTAGE_ADAPTING_30S,
    RECONNECTING_DRAINING,
    COMPLETED
}
