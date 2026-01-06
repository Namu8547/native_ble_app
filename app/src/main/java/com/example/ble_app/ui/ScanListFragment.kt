package com.example.ble_app.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ble_app.R
import com.example.ble_app.service.BleScanAdapter


class ScanListFragment : Fragment(R.layout.fragment_scan_list) {

    val deviceList: List<String> = listOf("adw", "dce","d3ve","rvwed3")


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.device_recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        val deviceAdapter = BleScanAdapter(deviceList){

            // Navigation triggered here
            findNavController().navigate(
                R.id.action_scan_list_to_device
            )
        }
        recyclerView.adapter = deviceAdapter

    }
}