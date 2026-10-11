package com.signalzero.app.data.repository

import com.signalzero.app.data.local.dao.PeerDao
import com.signalzero.app.data.local.entity.PeerEntity
import com.signalzero.app.data.model.PeerState
import kotlinx.coroutines.flow.Flow

class PeerRepository(private val peerDao: PeerDao) {

    val allPeers: Flow<List<PeerEntity>> = peerDao.getAllPeers()
    val routablePeers: Flow<List<PeerEntity>> = peerDao.getRoutablePeers()

    suspend fun getPeer(address: String): PeerEntity? = peerDao.getPeerByAddress(address)

    suspend fun recordPeerDiscovered(address: String, pseudonym: String, rssi: Int) {
        val existing = peerDao.getPeerByAddress(address)
        val peer = PeerEntity(
            address = address,
            pseudonym = pseudonym,
            state = PeerState.AVAILABLE.name,
            rssi = rssi,
            lastSeenTimestamp = System.currentTimeMillis(),
            failureCount = 0,
            packetsSent = existing?.packetsSent ?: 0,
            packetsReceived = existing?.packetsReceived ?: 0
        )
        peerDao.upsertPeer(peer)
    }

    suspend fun updatePeerState(address: String, state: PeerState) {
        peerDao.updatePeerState(address, state.name)
    }

    suspend fun incrementSent(address: String) {
        peerDao.incrementPacketsSent(address)
    }

    suspend fun incrementReceived(address: String) {
        peerDao.incrementPacketsReceived(address)
    }

    suspend fun markFailure(address: String) {
        peerDao.markPeerFailure(address)
    }
}
