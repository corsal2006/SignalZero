package com.signalzero.app.data.model

import java.util.UUID

data class TrustedContact(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val phoneNumber: String,
    val isPrimary: Boolean = false,
    val notifyBySms: Boolean = true,
    val notifyByShare: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {
    val notifyViaSms: Boolean get() = notifyBySms
}
