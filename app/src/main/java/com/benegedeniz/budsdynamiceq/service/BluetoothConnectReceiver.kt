package com.benegedeniz.budsdynamiceq.service

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.benegedeniz.budsdynamiceq.util.PermissionManager

class BluetoothConnectReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == BluetoothDevice.ACTION_ACL_CONNECTED) {
            val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
            val prefs = context.getSharedPreferences("BudsPrefs", Context.MODE_PRIVATE)
            val autoConnect = prefs.getBoolean("auto_connect", false)
            val savedMacAddress = prefs.getString("saved_mac_address", null)

            if (autoConnect && savedMacAddress != null && device != null && device.address == savedMacAddress) {
                if (PermissionManager.hasRequiredPermissions(context)) {
                    val serviceIntent = Intent(context, BudsService::class.java).apply {
                        action = "com.benegedeniz.budsdynamiceq.AUTO_CONNECT"
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(serviceIntent)
                    } else {
                        context.startService(serviceIntent)
                    }
                }
            }
        }
    }
}
