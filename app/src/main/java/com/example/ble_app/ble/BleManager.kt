package com.example.ble_app.service

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import java.util.UUID

class BleManager(private val context: Context) {

    private val bluetoothManager: BluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
    private var bluetoothGatt: BluetoothGatt? = null
    private val handler = Handler(Looper.getMainLooper())

    private val scannedDevices = mutableMapOf<String, BluetoothDevice>()

    // Callbacks
    var onDeviceFound: ((BluetoothDevice) -> Unit)? = null
    var onConnected: (() -> Unit)? = null
    var onDisconnected: (() -> Unit)? = null
    var onDataReceived: ((String) -> Unit)? = null

    // UUIDs - Replace these with your device's UUIDs
    companion object {
        private const val TAG = "SimpleBleManager"
        private const val SCAN_PERIOD: Long = 5000 // 5 seconds

        // Replace these with your actual service and characteristic UUIDs
//        val SERVICE_UUID: UUID = UUID.fromString("0000FFE0-0000-1000-8000-00805F9B34FB")
//        val NOTIFY_UUID: UUID = UUID.fromString("0000FFE1-0000-1000-8000-00805F9B34FB")
//        val WRITE_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
        val SERVICE_UUID : UUID = UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b")
        val WRITE_UUID : UUID = UUID.fromString("22222222-2222-2222-2222-222222222222")
        val NOTIFY_UUID : UUID = UUID.fromString("33333333-3333-3333-3333-333333333333")

        val CCCD_UUID :UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
    }

    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            result?.device?.let { device ->
                if (!scannedDevices.containsKey(device.address)) {
                    scannedDevices[device.address] = device
                    val name = device.name ?: "Unknown Device"
                    onDeviceFound?.invoke(device)
                    Log.d(TAG, "Device found: $name (${device.address})")
                }
            }
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.d(TAG, "Connected")
                    gatt?.discoverServices()
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.d(TAG, "Disconnected")
                    onDisconnected?.invoke()
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Services discovered")
                enableNotifications()
                onConnected?.invoke()
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            val data = String(value)
            Log.d(TAG, "Data received: $data")
            onDataReceived?.invoke(data)
        }

//        @Deprecated("Deprecated in Java")
//        override fun onCharacteristicChanged(
//            gatt: BluetoothGatt?,
//            characteristic: BluetoothGattCharacteristic?
//        ) {
//            characteristic?.value?.let { value ->
//                val data = String(value)
//                Log.d(TAG, "Data received: $data")
//                onDataReceived?.invoke(data)
//            }
//        }
    }

    @SuppressLint("MissingPermission")
    fun startScan() {
        scannedDevices.clear()
        bluetoothAdapter?.bluetoothLeScanner?.startScan(scanCallback)
        Log.d(TAG, "Scan started")

        // Stop scan after 5 seconds
        handler.postDelayed({
            stopScan()
        }, SCAN_PERIOD)
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
        Log.d(TAG, "Scan stopped")
    }

    @SuppressLint("MissingPermission")
    fun connect(address: String) {
        val device = scannedDevices[address] ?: return
        bluetoothGatt = device.connectGatt(context, false, gattCallback)
    }

    @SuppressLint("MissingPermission")
//     fun enableNotifications() {
//        val service = bluetoothGatt?.getService(SERVICE_UUID) ?: return
//        val characteristic = service.getCharacteristic(NOTIFY_UUID) ?: return
//
//        bluetoothGatt?.setCharacteristicNotification(characteristic, true)
//
//        val descriptor = characteristic.getDescriptor(CCCD_UUID)
//        descriptor?.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
//        bluetoothGatt?.writeDescriptor(descriptor)
//    }
    fun enableNotifications() {
        val service = bluetoothGatt?.getService(SERVICE_UUID) ?: run {
            Log.e(TAG, "Service not found")
            return
        }
        val characteristic = service.getCharacteristic(NOTIFY_UUID) ?: run {
            Log.e(TAG, "Notify characteristic not found")
            return
        }

        bluetoothGatt?.setCharacteristicNotification(characteristic, true)

        val descriptor = characteristic.getDescriptor(CCCD_UUID) ?: run {
            Log.e(TAG, "CCCD descriptor not found")
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            bluetoothGatt?.writeDescriptor(descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
        } else {
            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            bluetoothGatt?.writeDescriptor(descriptor)
        }
        Log.d(TAG, "Notifications enabled")
    }

    @SuppressLint("MissingPermission")
    fun sendData(data: String) {
        val service = bluetoothGatt?.getService(SERVICE_UUID) ?: return
        val characteristic = service.getCharacteristic(WRITE_UUID) ?: return

        characteristic.value = data.toByteArray()
        bluetoothGatt?.writeCharacteristic(characteristic)
        Log.d(TAG, "Data sent: $data")
    }

    @SuppressLint("MissingPermission")
    fun disconnect() {
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null
    }
}