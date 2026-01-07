package com.example.ble_app.ui

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ble_app.R
import com.example.ble_app.ble.BleManagerHolder
import com.example.ble_app.service.BleManager


import com.example.ble_app.service.BleScanAdapter


class ScanListFragment : Fragment(R.layout.fragment_scan_list) {

    private lateinit var bleManager: BleManager
    private lateinit var adapter: BleScanAdapter
    private val devices = mutableListOf<BluetoothDevice>()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value }) {
            startScan()
        } else {
            Toast.makeText(requireContext(), "Permissions required", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // In ScanListFragment and DeviceFragment
        bleManager = BleManagerHolder.getInstance(requireContext())
        setupRecyclerView(view)
        setupCallbacks()
        setupScanButton(view)

    }

    private fun setupRecyclerView(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.device_recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = BleScanAdapter(devices) { device ->
            connectToDevice(device)
        }
        recyclerView.adapter = adapter
    }

    private fun setupCallbacks() {
        bleManager.onDeviceFound = { device ->
            requireActivity().runOnUiThread {
                adapter.addDevice(device)
            }
        }

        bleManager.onConnected = {
            requireActivity().runOnUiThread {
                Toast.makeText(requireContext(), "Connected!", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_scan_list_to_device)
            }
        }
    }

    private fun setupScanButton(view: View) {
        view.findViewById<Button>(R.id.button).setOnClickListener {
            checkPermissionsAndScan()
        }
    }

    private fun checkPermissionsAndScan() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        val missing = permissions.filter {
            ContextCompat.checkSelfPermission(requireContext(), it) != PackageManager.PERMISSION_GRANTED
        }

        if (missing.isEmpty()) {
            startScan()
        } else {
            permissionLauncher.launch(missing.toTypedArray())
        }
    }

    private fun startScan() {
        adapter.clearDevices()
        bleManager.startScan()
        Toast.makeText(requireContext(), "Scanning for 5 seconds...", Toast.LENGTH_SHORT).show()
    }

    private fun connectToDevice(device: BluetoothDevice) {
        bleManager.stopScan()
        bleManager.connect(device.address)
        Toast.makeText(requireContext(), "Connecting...", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bleManager.stopScan()
    }
}