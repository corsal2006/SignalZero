package com.signalzero.app.mesh

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.os.ParcelUuid
import android.util.Log

class BleAdvertiserManager(private val bluetoothAdapter: BluetoothAdapter?) {

    private val tag = "SignalZero_Advertiser"
    private var advertiser: BluetoothLeAdvertiser? = null
    var isAdvertising = false
        private set

    private val callback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            super.onStartSuccess(settingsInEffect)
            isAdvertising = true
            Log.i(tag, "BLE advertising started successfully with service UUID: ${BleConstants.SERVICE_UUID}")
        }

        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)
            isAdvertising = false
            Log.e(tag, "BLE advertising failed to start. Error code: $errorCode")
        }
    }

    @SuppressLint("MissingPermission")
    fun startAdvertising(nodePseudonym: String) {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            Log.w(tag, "Bluetooth is disabled or adapter is null. Cannot advertise.")
            return
        }

        advertiser = bluetoothAdapter.bluetoothLeAdvertiser
        if (advertiser == null) {
            Log.w(tag, "Device does not support BLE peripheral advertising.")
            return
        }

        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .setConnectable(true)
            .setTimeout(0)
            .build()

        val serviceUuid = ParcelUuid(BleConstants.SERVICE_UUID)
        val data = AdvertiseData.Builder()
            .setIncludeDeviceName(false)
            .setIncludeTxPowerLevel(false)
            .addServiceUuid(serviceUuid)
            .addServiceData(serviceUuid, nodePseudonym.toByteArray().take(4).toByteArray())
            .build()

        try {
            advertiser?.startAdvertising(settings, data, callback)
        } catch (e: Exception) {
            Log.e(tag, "Exception starting advertising: ${e.message}")
        }
    }

    @SuppressLint("MissingPermission")
    fun stopAdvertising() {
        try {
            if (isAdvertising && advertiser != null) {
                advertiser?.stopAdvertising(callback)
                isAdvertising = false
                Log.i(tag, "BLE advertising stopped.")
            }
        } catch (e: Exception) {
            Log.e(tag, "Exception stopping advertising: ${e.message}")
        }
    }
}
