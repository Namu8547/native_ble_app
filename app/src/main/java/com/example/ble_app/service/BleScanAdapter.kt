package com.example.ble_app.service

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.ble_app.R
import org.w3c.dom.Text

class BleScanAdapter(private val devices : MutableList<BluetoothDevice>,private  val onConnect : (BluetoothDevice) -> Unit) : RecyclerView.Adapter<BleScanAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val deviceName : TextView = view.findViewById<TextView>(R.id.device_name)
        val button : Button = view.findViewById<Button>(R.id.connect_button)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {


            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.device_list, parent, false)   // ← HERE

        return ViewHolder(view)
    }



    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val device = devices[position]
        holder.deviceName.text = devices[position].name

        holder.button.setOnClickListener {
            onConnect(device)
        }
    }


    override fun getItemCount() = devices.size

    fun addDevice(device: BluetoothDevice) {
        if (!devices.any { it.address == device.address }) {
            devices.add(device)
            notifyItemInserted(devices.size - 1)
        }
    }

    fun clearDevices() {
        val size = devices.size
        devices.clear()
        notifyItemRangeRemoved(0, size)
    }
}