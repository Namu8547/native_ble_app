package com.example.ble_app.ble

import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context


class BleScanner(context: Context, private val onDeviceFound: (ScanResult) -> Unit) {
    private val scanner = context.getSystemService(BluetoothLeScanner::class.java)

    private val callback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            onDeviceFound(result)
        }
    }

    fun startScan() {
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        scanner.startScan(null, settings, callback)
    }

    fun stopScan() {
        scanner.stopScan(callback)
    }
}