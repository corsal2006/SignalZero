package com.signalzero.app.mesh

import java.util.UUID

object BleConstants {
    // Registered 128-bit Service UUID for SIGNALZERO
    val SERVICE_UUID: UUID = UUID.fromString("0000FE70-0000-1000-8000-00805F9B34FB")

    // Characteristic UUID for incoming packet chunk writes
    val CHAR_WRITE_UUID: UUID = UUID.fromString("0000FE71-0000-1000-8000-00805F9B34FB")

    // Characteristic UUID for notifications & acknowledgements
    val CHAR_NOTIFY_UUID: UUID = UUID.fromString("0000FE72-0000-1000-8000-00805F9B34FB")

    const val CHUNK_HEADER_SIZE = 14
    const val MAGIC_BYTE_1: Byte = 0x53 // 'S'
    const val MAGIC_BYTE_2: Byte = 0x5A // 'Z'
    const val TARGET_MTU = 247
    const val NOTIFICATION_CHANNEL_ID = "signalzero_mesh_channel"
    const val NOTIFICATION_ID = 9110
}
