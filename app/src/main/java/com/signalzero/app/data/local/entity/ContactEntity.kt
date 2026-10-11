package com.signalzero.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.signalzero.app.data.model.TrustedContact

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey val id: String,
    val name: String,
    val phoneNumber: String,
    val isPrimary: Boolean,
    val notifyBySms: Boolean,
    val notifyByShare: Boolean,
    val createdAt: Long
) {
    fun toDomain(): TrustedContact {
        return TrustedContact(
            id = id,
            name = name,
            phoneNumber = phoneNumber,
            isPrimary = isPrimary,
            notifyBySms = notifyBySms,
            notifyByShare = notifyByShare,
            createdAt = createdAt
        )
    }

    companion object {
        fun fromDomain(domain: TrustedContact): ContactEntity {
            return ContactEntity(
                id = domain.id,
                name = domain.name,
                phoneNumber = domain.phoneNumber,
                isPrimary = domain.isPrimary,
                notifyBySms = domain.notifyBySms,
                notifyByShare = domain.notifyByShare,
                createdAt = domain.createdAt
            )
        }
    }
}
