package com.example.ble_app.ble

import android.content.Context
import com.example.ble_app.service.BleManager

object BleManagerHolder {
    private var instance: BleManager? = null

    fun getInstance(context: Context): BleManager {
        return instance ?: BleManager(context.applicationContext).also {
            instance = it
        }
    }
}