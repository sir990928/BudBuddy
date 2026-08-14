package com.benegedeniz.budsdynamiceq.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.benegedeniz.budsdynamiceq.util.PermissionManager

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            val prefs = context.getSharedPreferences("BudsPrefs", Context.MODE_PRIVATE)
            val startOnBoot = prefs.getBoolean("start_on_boot", false)
            val hasSavedDevice = prefs.getString("saved_mac_address", null) != null

            if (startOnBoot && hasSavedDevice && PermissionManager.hasRequiredPermissions(context)) {
                val serviceIntent = Intent(context, BudsService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
            }
        }
    }
}
