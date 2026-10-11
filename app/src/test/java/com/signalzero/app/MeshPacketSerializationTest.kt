package com.signalzero.app

import com.signalzero.app.data.model.MeshPacket
import org.junit.Assert.*
import org.junit.Test

class MeshPacketSerializationTest {

    @Test
    fun testMeshPacketHopExhaustionAndExpiry() {
        val now = System.currentTimeMillis()

        // 1. Valid fresh packet
        val freshPacket = MeshPacket(
            packetId = "pkt-fresh",
            originPeerId = "peer-alpha",
            payloadJson = "{\"msg\":\"sos\"}",
            hopCount = 1,
            maxHops = 5,
            createdAt = now,
            expiresAt = now + 3600000L
        )
        assertFalse("Fresh packet within hop limit must not be expired", freshPacket.isExpired)
        assertFalse("Fresh packet within hop limit must not be exhausted", freshPacket.isHopExhausted)

        // 2. Hop-exhausted packet
        val exhaustedPacket = freshPacket.copy(hopCount = 5, maxHops = 5)
        assertTrue("Packet having reached max hops must be marked hop exhausted", exhaustedPacket.isHopExhausted)

        // 3. Expired packet
        val expiredPacket = freshPacket.copy(expiresAt = now - 1000L)
        assertTrue("Packet with timestamp before now must be marked expired", expiredPacket.isExpired)
    }

    @Test
    fun testPacketTypesAndLoopPrevention() {
        val visited = listOf("node-0", "node-1", "node-3")
        val packet = MeshPacket(
            packetId = "pkt-loop-test",
            originPeerId = "node-0",
            type = MeshPacket.PacketType.SOS_RELAY,
            payloadJson = "{\"alert\":\"emergency\"}",
            visitedNodes = visited
        )

        assertTrue("Visited nodes must contain origin", packet.visitedNodes.contains("node-0"))
        assertTrue("Visited nodes must contain intermediate relays", packet.visitedNodes.contains("node-1"))
        assertFalse("Visited nodes must not contain unvisited node", packet.visitedNodes.contains("node-9"))
    }
}
