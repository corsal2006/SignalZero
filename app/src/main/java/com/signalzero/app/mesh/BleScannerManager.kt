package com.signalzero.app.mesh

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.os.ParcelUuid
import android.util.Log

interface PeerDiscoveryListener {
    fun onPeerFound(deviceAddress: String, deviceName: String?, rssi: Int, serviceData: ByteArray?)
}

class BleScannerManager(
    private val bluetoothAdapter: BluetoothAdapter?,
    private val listener: PeerDiscoveryListener
) {
    private val tag = "SignalZero_Scanner"
    private var scanner: BluetoothLeScanner? = null
    var isScanning = false
        private set

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            result?.let {
                val device = it.device
                val rssi = it.rssi
                val scanRecord = it.scanRecord
                val serviceData = scanRecord?.getServiceData(ParcelUuid(BleConstants.SERVICE_UUID))
                listener.onPeerFound(device.address, device.name, rssi, serviceData)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            isScanning = false
            Log.e(tag, "BLE Scan failed with error code: $errorCode")
        }
    }

    @SuppressLint("MissingPermission")
    fun startScanning() {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            Log.w(tag, "Bluetooth is disabled. Cannot start scanning.")
            return
        }

        scanner = bluetoothAdapter.bluetoothLeScanner
        if (scanner == null) {
            Log.w(tag, "Scanner is null.")
            return
        }

        val filter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(BleConstants.SERVICE_UUID))
            .build()

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setReportDelay(0)
            .build()

        try {
            scanner?.startScan(listOf(filter), settings, scanCallback)
            isScanning = true
            Log.i(tag, "BLE scanning started with filter: ${BleConstants.SERVICE_UUID}")
        } catch (e: Exception) {
            Log.e(tag, "Exception starting scan: ${e.message}")
        }
    }

    @SuppressLint("MissingPermission")
    fun stopScanning() {
        try {
            if (isScanning && scanner != null) {
                scanner?.stopScan(scanCallback)
                isScanning = false
                Log.i(tag, "BLE scanning stopped.")
            }
        } catch (e: Exception) {
            Log.e(tag, "Exception stopping scan: ${e.message}")
        }
    }
}
