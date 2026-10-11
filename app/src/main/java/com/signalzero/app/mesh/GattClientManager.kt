package com.signalzero.app.mesh

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.nio.ByteBuffer
import java.util.LinkedList
import java.util.Queue

interface PacketTransferCallback {
    fun onTransferSuccess(deviceAddress: String, messageId: String)
    fun onTransferFailure(deviceAddress: String, messageId: String, reason: String)
}

class GattClientManager(
    private val context: Context,
    private val bluetoothAdapter: BluetoothAdapter?,
    private val callback: PacketTransferCallback
) {
    private val tag = "SignalZero_GattClient"
    private data class Transfer(val address: String, val messageId: String, val json: String)
    private val transfers: Queue<Transfer> = LinkedList()
    private var busy = false
    private val watchdog = Handler(Looper.getMainLooper())
    private var activeGatt: BluetoothGatt? = null
    private val pendingChunks: Queue<ByteArray> = LinkedList()
    private var writeCharacteristic: BluetoothGattCharacteristic? = null
    private var currentPacketJson: String = ""
    private var currentMessageId: String = ""
    private var currentTargetAddress: String = ""

    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            if (gatt !== activeGatt) {
                gatt?.close()
                return
            }

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(tag, "Connected to GATT peer ${gatt?.device?.address}. Requesting MTU ${BleConstants.TARGET_MTU}...")
                gatt?.requestMtu(BleConstants.TARGET_MTU)
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(tag, "Disconnected from GATT peer.")
                if (pendingChunks.isNotEmpty()) {
                    callback.onTransferFailure(currentTargetAddress, currentMessageId, "DISCONNECTED_BEFORE_COMPLETION")
                }
                gatt?.close()
                watchdog.removeCallbacksAndMessages(null)
                activeGatt = null
                synchronized(this@GattClientManager) {
                    busy = false
                    val next = transfers.poll()
                    if (next != null) sendPacket(next.address, next.messageId, next.json)
                }
            }
        }

        @SuppressLint("MissingPermission")
        override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
            super.onMtuChanged(gatt, mtu, status)
            Log.i(tag, "MTU changed to $mtu. Discovering services...")
            gatt?.discoverServices()
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val service = gatt?.getService(BleConstants.SERVICE_UUID)
                writeCharacteristic = service?.getCharacteristic(BleConstants.CHAR_WRITE_UUID)

                if (writeCharacteristic != null) {
                    Log.i(tag, "Service and Write Characteristic discovered. Framing chunks...")
                    prepareChunks(currentPacketJson)
                    sendNextChunk()
                } else {
                    Log.e(tag, "Write characteristic not found.")
                    callback.onTransferFailure(currentTargetAddress, currentMessageId, "CHAR_NOT_FOUND")
                    disconnect()
                }
            } else {
                Log.e(tag, "Service discovery failed with status $status")
                callback.onTransferFailure(currentTargetAddress, currentMessageId, "SERVICE_DISCOVERY_FAILED")
                disconnect()
            }
        }

        @SuppressLint("MissingPermission")
        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (pendingChunks.isNotEmpty()) {
                    sendNextChunk()
                } else {
                    Log.i(tag, "All chunks transmitted successfully to $currentTargetAddress")
                    callback.onTransferSuccess(currentTargetAddress, currentMessageId)
                    disconnect()
                }
            } else {
                Log.e(tag, "Write failed with status $status")
                callback.onTransferFailure(currentTargetAddress, currentMessageId, "WRITE_FAILED_$status")
                disconnect()
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun sendPacket(targetAddress: String, messageId: String, packetJson: String) {
        synchronized(this) {
            if (busy) {
                transfers.offer(Transfer(targetAddress, messageId, packetJson))
                return
            }
            busy = true
        }

        currentTargetAddress = targetAddress
        currentMessageId = messageId
        currentPacketJson = packetJson

        val device: BluetoothDevice? = try {
            bluetoothAdapter?.getRemoteDevice(targetAddress)
        } catch (e: Exception) {
            null
        }

        if (device == null) {
            callback.onTransferFailure(targetAddress, messageId, "INVALID_DEVICE_ADDRESS")
            finishCurrentTransfer()
            return
        }

        watchdog.postDelayed({
            Log.w(tag, "Transfer watchdog timed out for $targetAddress")
            callback.onTransferFailure(targetAddress, messageId, "TRANSFER_TIMEOUT")
            disconnect()
        }, 12000L)

        Log.i(tag, "Connecting to GATT peer $targetAddress...")
        activeGatt = device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
    }

    private fun prepareChunks(json: String) {
        pendingChunks.clear()
        val data = json.toByteArray(Charsets.UTF_8)
        val maxChunkPayload = 180 // Fits comfortably in MTU 247 with header
        val totalChunks = ((data.size + maxChunkPayload - 1) / maxChunkPayload).coerceAtLeast(1)
        val hash = json.hashCode()

        for (i in 0 until totalChunks) {
            val start = i * maxChunkPayload
            val end = (start + maxChunkPayload).coerceAtMost(data.size)
            val chunkPayload = data.copyOfRange(start, end)
            val crc = GattServerManager.calculateCrc16(chunkPayload)

            val buffer = ByteBuffer.allocate(BleConstants.CHUNK_HEADER_SIZE + chunkPayload.size)
            buffer.put(BleConstants.MAGIC_BYTE_1)
            buffer.put(BleConstants.MAGIC_BYTE_2)
            buffer.putShort(i.toShort())
            buffer.putShort(totalChunks.toShort())
            buffer.putInt(hash)
            buffer.putShort(chunkPayload.size.toShort())
            buffer.putShort(crc.toShort())
            buffer.put(chunkPayload)

            pendingChunks.offer(buffer.array())
        }
    }

    @SuppressLint("MissingPermission")
    private fun sendNextChunk() {
        val chunk = pendingChunks.poll() ?: return
        val gatt = activeGatt ?: return
        val char = writeCharacteristic ?: return

        char.value = chunk
        char.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        val initiated = gatt.writeCharacteristic(char)
        if (!initiated) {
            Log.e(tag, "Failed to initiate chunk write.")
            callback.onTransferFailure(currentTargetAddress, currentMessageId, "WRITE_INITIATION_FAILED")
            disconnect()
        }
    }

    @SuppressLint("MissingPermission")
    private fun disconnect() {
        watchdog.removeCallbacksAndMessages(null)
        try {
            activeGatt?.disconnect()
            activeGatt?.close()
        } catch (e: Exception) {
            Log.e(tag, "Exception disconnecting: ${e.message}")
        }
        activeGatt = null
        writeCharacteristic = null
        finishCurrentTransfer()
    }

    private fun finishCurrentTransfer() {
        synchronized(this) {
            busy = false
            val next = transfers.poll()
            if (next != null) {
                sendPacket(next.address, next.messageId, next.json)
            }
        }
    }
}
