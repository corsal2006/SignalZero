package com.signalzero.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.signalzero.app.data.model.PeerState

@Entity(tableName = "peers")
data class PeerEntity(
    @PrimaryKey val address: String,
    val pseudonym: String,
    val state: String,
    val rssi: Int,
    val lastSeenTimestamp: Long,
    val failureCount: Int = 0,
    val packetsSent: Int = 0,
    val packetsReceived: Int = 0
) {
    val peerState: PeerState get() = PeerState.fromString(state)
}
