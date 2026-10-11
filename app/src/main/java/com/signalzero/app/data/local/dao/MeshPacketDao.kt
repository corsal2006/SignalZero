package com.signalzero.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.signalzero.app.data.local.entity.MeshPacketEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MeshPacketDao {
    @Query("SELECT * FROM mesh_packets ORDER BY createdAt DESC")
    fun getAllPackets(): Flow<List<MeshPacketEntity>>

    @Query("SELECT * FROM mesh_packets WHERE isDelivered = 0 AND expiresAt > :now ORDER BY createdAt ASC")
    fun getPendingPackets(now: Long = System.currentTimeMillis()): Flow<List<MeshPacketEntity>>

    @Query("SELECT * FROM mesh_packets WHERE isDelivered = 0 AND expiresAt > :now ORDER BY createdAt ASC")
    suspend fun getPendingPacketsList(now: Long = System.currentTimeMillis()): List<MeshPacketEntity>

    @Query("SELECT * FROM mesh_packets WHERE packetId = :packetId LIMIT 1")
    suspend fun getPacketById(packetId: String): MeshPacketEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM mesh_packets WHERE packetId = :packetId)")
    suspend fun packetExists(packetId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPacket(packet: MeshPacketEntity): Long

    @Update
    suspend fun updatePacket(packet: MeshPacketEntity)

    @Query("UPDATE mesh_packets SET isDelivered = 1 WHERE packetId = :packetId")
    suspend fun markDelivered(packetId: String)

    @Query("UPDATE mesh_packets SET retryCount = retryCount + 1 WHERE packetId = :packetId")
    suspend fun incrementRetry(packetId: String)

    @Query("DELETE FROM mesh_packets WHERE expiresAt < :now")
    suspend fun deleteExpiredPackets(now: Long = System.currentTimeMillis())
}
