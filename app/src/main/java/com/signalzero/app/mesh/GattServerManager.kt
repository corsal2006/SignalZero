package com.signalzero.app.mesh

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.content.Context
import android.util.Log
import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentHashMap

interface PacketReceivedListener {
    fun onRawPacketReceived(packetJson: String, originDeviceAddress: String)
}

class GattServerManager(
    private val context: Context,
    private val bluetoothManager: BluetoothManager,
    private val listener: PacketReceivedListener
) {
    private val tag = "SignalZero_GattServer"
    private var gattServer: BluetoothGattServer? = null

    // In-flight reassembly buffers keyed by messageIdHash
    private val inFlightReassembly = ConcurrentHashMap<String, MutableMap<Int, ByteArray>>()
    private val assemblyTimes = ConcurrentHashMap<String, Long>()
    private val totalChunksMap = ConcurrentHashMap<String, Int>()

    private val serverCallback = object : BluetoothGattServerCallback() {
        @SuppressLint("MissingPermission")
        override fun onCharacteristicWriteRequest(
            device: BluetoothDevice?,
            requestId: Int,
            characteristic: BluetoothGattCharacteristic?,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray?
        ) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value)

            if (characteristic?.uuid == BleConstants.CHAR_WRITE_UUID && value != null) {
                processIncomingChunk(value, device?.address ?: "UNKNOWN")
            }

            if (responseNeeded) {
                gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value)
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startGattServer() {
        gattServer = bluetoothManager.openGattServer(context, serverCallback)
        if (gattServer == null) {
            Log.e(tag, "Failed to open GATT Server.")
            return
        }

        val service = BluetoothGattService(BleConstants.SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)

        val writeChar = BluetoothGattCharacteristic(
            BleConstants.CHAR_WRITE_UUID,
            BluetoothGattCharacteristic.PROPERTY_WRITE or BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE,
            BluetoothGattCharacteristic.PERMISSION_WRITE
        )

        val notifyChar = BluetoothGattCharacteristic(
            BleConstants.CHAR_NOTIFY_UUID,
            BluetoothGattCharacteristic.PROPERTY_NOTIFY,
            BluetoothGattCharacteristic.PERMISSION_READ
        )

        service.addCharacteristic(writeChar)
        service.addCharacteristic(notifyChar)

        gattServer?.addService(service)
        Log.i(tag, "GATT Server started with Write and Notify characteristics.")
    }

    @SuppressLint("MissingPermission")
    fun stopGattServer() {
        try {
            gattServer?.close()
            gattServer = null
            Log.i(tag, "GATT Server closed.")
        } catch (e: Exception) {
            Log.e(tag, "Exception closing GATT Server: ${e.message}")
        }
    }

    private fun processIncomingChunk(chunk: ByteArray, originAddress: String) {
        if (chunk.size < BleConstants.CHUNK_HEADER_SIZE) return

        val buffer = ByteBuffer.wrap(chunk)
        val m1 = buffer.get()
        val m2 = buffer.get()
        if (m1 != BleConstants.MAGIC_BYTE_1 || m2 != BleConstants.MAGIC_BYTE_2) return

        val seqIndex = buffer.short.toInt()
        val totalChunks = buffer.short.toInt()
        val hash = buffer.int
        val dataLen = buffer.short.toInt()
        val receivedCrc = buffer.short.toInt()

        val payload = ByteArray(dataLen)
        buffer.get(payload)

        // Validate payload CRC
        val calculatedCrc = calculateCrc16(payload)
        if (calculatedCrc != receivedCrc) {
            Log.w(tag, "CRC mismatch for chunk $seqIndex of hash $hash")
            return
        }

        val key = "$originAddress:$hash"
        assemblyTimes.putIfAbsent(key, System.currentTimeMillis())
        totalChunksMap.putIfAbsent(key, totalChunks)

        val chunksMap = inFlightReassembly.computeIfAbsent(key) { ConcurrentHashMap() }
        chunksMap[seqIndex] = payload

        if (chunksMap.size == totalChunks) {
            // Reassembly complete
            val fullBytes = mutableListOf<Byte>()
            for (i in 0 until totalChunks) {
                chunksMap[i]?.let { fullBytes.addAll(it.toList()) }
            }

            inFlightReassembly.remove(key)
            assemblyTimes.remove(key)
            totalChunksMap.remove(key)

            val fullJson = String(fullBytes.toByteArray(), Charsets.UTF_8)
            Log.i(tag, "Packet fully reassembled (${fullBytes.size} bytes) from $originAddress")
            listener.onRawPacketReceived(fullJson, originAddress)
        }
    }

    companion object {
        fun calculateCrc16(data: ByteArray): Int {
            var crc = 0xFFFF
            for (b in data) {
                crc = crc xor (b.toInt() and 0xFF)
                for (i in 0 until 8) {
                    crc = if ((crc and 1) != 0) (crc ushr 1) xor 0xA001 else (crc ushr 1)
                }
            }
            return crc and 0xFFFF
        }
    }
}
