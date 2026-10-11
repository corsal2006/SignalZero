package com.signalzero.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.signalzero.app.data.local.dao.ChatMessageDao
import com.signalzero.app.data.local.dao.ContactDao
import com.signalzero.app.data.local.dao.IncidentDao
import com.signalzero.app.data.local.dao.MeshPacketDao
import com.signalzero.app.data.local.dao.PeerDao
import com.signalzero.app.data.local.entity.ChatMessageEntity
import com.signalzero.app.data.local.entity.ContactEntity
import com.signalzero.app.data.local.entity.IncidentEntity
import com.signalzero.app.data.local.entity.MeshPacketEntity
import com.signalzero.app.data.local.entity.PeerEntity

@Database(
    entities = [
        IncidentEntity::class,
        MeshPacketEntity::class,
        PeerEntity::class,
        ChatMessageEntity::class,
        ContactEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SignalZeroDatabase : RoomDatabase() {

    abstract fun incidentDao(): IncidentDao
    abstract fun meshPacketDao(): MeshPacketDao
    abstract fun peerDao(): PeerDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun contactDao(): ContactDao

    companion object {
        @Volatile
        private var INSTANCE: SignalZeroDatabase? = null

        fun getInstance(context: Context): SignalZeroDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SignalZeroDatabase::class.java,
                    "signalzero_master.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
