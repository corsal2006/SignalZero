package com.signalzero.app

import com.signalzero.app.challenge.GrevixResilienceSimulator
import com.signalzero.app.challenge.SimulationState
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class GrevixResilienceTest {

    @Test
    fun testGrevixResilienceScenario_20PercentDisconnectionFor30Seconds() {
        runBlocking {
            val simulator = GrevixResilienceSimulator()

            // 1. Initial State
            assertEquals("Initial state should be IDLE", SimulationState.IDLE, simulator.simState.value)
            assertEquals("Must initialize 10 virtual nodes", 10, simulator.nodes.value.size)

            // 2. Run simulation with accelerated time scale for fast automated unit test verification
            simulator.runResilienceTest(timeScaleFactor = 30.0)

            // Wait until simulation completes
            var attempts = 0
            while (simulator.simState.value != SimulationState.COMPLETED && attempts < 100) {
                delay(100L)
                attempts++
            }

            assertEquals("Simulation must reach COMPLETED state", SimulationState.COMPLETED, simulator.simState.value)

            val report = simulator.report.value
            assertNotNull("Report must not be null upon completion", report)

            report?.let { r ->
                assertTrue("Simulation test must pass official criteria", r.isPassed)
                assertEquals("Total nodes must be 10", 10, r.totalNodes)
                assertEquals("Disconnected nodes must be exactly 2", 2, r.disconnectedNodesCount)
                assertEquals("Disconnected percentage must be 20.0%", 20.0, r.disconnectedPercentage, 0.01)
                assertEquals("Disconnection duration must be 30.0s", 30.0, r.disconnectionDurationSeconds, 0.01)
                assertEquals("Packets queued during outage must be 10", 10, r.packetsQueuedDuringOutage)
                assertEquals("Packets delivered must be 10", 10, r.packetsDelivered)
                assertEquals("Packets recovered after reconnect must be 10", 10, r.packetsRecoveredAfterReconnect)
                assertTrue("Duplicates prevented must be > 0", r.duplicatesPrevented > 0)
                assertTrue("Route changes observed must be > 0", r.routeChangesObserved > 0)
                assertTrue("Recovery time must be recorded", r.recoveryTimeMs >= 0)
                assertTrue("Incident processing latency must be low (< 10ms)", r.incidentEngineProcessingMs < 10.0)
            }
        }
    }
}
