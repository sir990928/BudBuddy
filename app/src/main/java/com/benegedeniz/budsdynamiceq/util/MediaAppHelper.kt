package com.benegedeniz.budsdynamiceq.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build

data class MediaAppInfo(
    val packageName: String,
    val name: String
)

object MediaAppHelper {

    fun getInstalledMediaApps(context: Context): List<MediaAppInfo> {
        val pm = context.packageManager
        val mediaApps = mutableMapOf<String, String>()

        // 1. Query for MediaBrowserService
        val browserIntent = Intent(android.service.media.MediaBrowserService.SERVICE_INTERFACE)
        val browserServices = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.queryIntentServices(browserIntent, PackageManager.ResolveInfoFlags.of(0L))
        } else {
            @Suppress("DEPRECATION")
            pm.queryIntentServices(browserIntent, 0)
        }

        for (resolveInfo in browserServices) {
            val packageName = resolveInfo.serviceInfo.packageName
            val appInfo = try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    pm.getApplicationInfo(packageName, PackageManager.ApplicationInfoFlags.of(0L))
                } else {
                    @Suppress("DEPRECATION")
                    pm.getApplicationInfo(packageName, 0)
                }
            } catch (e: PackageManager.NameNotFoundException) {
                null
            }
            if (appInfo != null) {
                mediaApps[packageName] = pm.getApplicationLabel(appInfo).toString()
            }
        }

        // 2. Query for ACTION_MEDIA_BUTTON receivers
        val buttonIntent = Intent(Intent.ACTION_MEDIA_BUTTON)
        val buttonReceivers = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.queryBroadcastReceivers(buttonIntent, PackageManager.ResolveInfoFlags.of(0L))
        } else {
            @Suppress("DEPRECATION")
            pm.queryBroadcastReceivers(buttonIntent, 0)
        }

        for (resolveInfo in buttonReceivers) {
            val packageName = resolveInfo.activityInfo.packageName
            if (!mediaApps.containsKey(packageName)) {
                val appInfo = try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        pm.getApplicationInfo(packageName, PackageManager.ApplicationInfoFlags.of(0L))
                    } else {
                        @Suppress("DEPRECATION")
                        pm.getApplicationInfo(packageName, 0)
                    }
                } catch (e: PackageManager.NameNotFoundException) {
                    null
                }
                if (appInfo != null) {
                    mediaApps[packageName] = pm.getApplicationLabel(appInfo).toString()
                }
            }
        }

        return mediaApps.map { MediaAppInfo(it.key, it.value) }.sortedBy { it.name.lowercase() }
    }
}
