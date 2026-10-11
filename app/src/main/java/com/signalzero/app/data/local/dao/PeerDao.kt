package com.signalzero.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.signalzero.app.data.local.entity.PeerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PeerDao {
    @Query("SELECT * FROM peers ORDER BY lastSeenTimestamp DESC")
    fun getAllPeers(): Flow<List<PeerEntity>>

    @Query("SELECT * FROM peers WHERE state IN ('AVAILABLE', 'CONNECTED', 'RELAYING') ORDER BY rssi DESC")
    fun getRoutablePeers(): Flow<List<PeerEntity>>

    @Query("SELECT * FROM peers WHERE address = :address LIMIT 1")
    suspend fun getPeerByAddress(address: String): PeerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPeer(peer: PeerEntity)

    @Update
    suspend fun updatePeer(peer: PeerEntity)

    @Query("UPDATE peers SET state = :newState WHERE address = :address")
    suspend fun updatePeerState(address: String, newState: String)

    @Query("UPDATE peers SET packetsSent = packetsSent + 1 WHERE address = :address")
    suspend fun incrementPacketsSent(address: String)

    @Query("UPDATE peers SET packetsReceived = packetsReceived + 1 WHERE address = :address")
    suspend fun incrementPacketsReceived(address: String)

    @Query("UPDATE peers SET failureCount = failureCount + 1, state = 'SUSPECT' WHERE address = :address")
    suspend fun markPeerFailure(address: String)
}
