package com.example.ble_app.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.example.ble_app.R
import com.example.ble_app.ble.BleManagerHolder
import com.example.ble_app.service.BleManager


class DeviceFragment : Fragment(R.layout.fragment_device) {

    private lateinit var bleManager: BleManager
    private lateinit var dataTextView: TextView


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // In ScanListFragment and DeviceFragment
        bleManager = BleManagerHolder.getInstance(requireContext())
        dataTextView = view.findViewById<TextView>(R.id.textData_from_device)

        setupCallbacks()
        setupSendButton(view)

    }

    private fun setupCallbacks() {
        bleManager.onDataReceived = { data ->
            requireActivity().runOnUiThread {
                dataTextView.text = "Received: $data"
            }
        }
    }

    private fun setupSendButton(view: View) {
        view.findViewById<Button>(R.id.sendButton).setOnClickListener {
            // Send your specific data here
            bleManager.sendData("0604{\"type\":\"wifi\",\"ssid\":\"iot\",\"password\":\"369369369\"}\n")
            dataTextView.text = "Sent"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bleManager.disconnect()
    }



}