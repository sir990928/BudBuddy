package com.benegedeniz.budsdynamiceq.service

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import com.benegedeniz.budsdynamiceq.bluetooth.BudsController
import com.benegedeniz.budsdynamiceq.ui.widget.NoiseControlWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class WidgetCoordinator(
    private val context: Context,
    private val scope: CoroutineScope,
    private val budsController: BudsController
) {
    fun start() {
        scope.launch {
            val batteryPlacementFlow = combine(
                budsController.batteryL,
                budsController.batteryR,
                budsController.placementL,
                budsController.placementR
            ) { bL, bR, pL, pR ->
                object {
                    val bL = bL
                    val bR = bR
                    val pL = pL
                    val pR = pR
                }
            }

            val deviceStateFlow = combine(
                budsController.isConnected,
                batteryPlacementFlow,
                budsController.oneEarbudNoiseControlEnabled
            ) { connected, bp, oneEarbudEnabled ->
                object {
                    val connected = connected
                    val bL = bp.bL
                    val bR = bp.bR
                    val pL = bp.pL
                    val pR = bp.pR
                    val oneEarbudEnabled = oneEarbudEnabled
                }
            }

            val widgetUpdateFlow = combine(
                deviceStateFlow,
                budsController.activeNoiseControl
            ) { device, activeNc ->
                object {
                    val device = device
                    val activeNc = activeNc
                }
            }

            widgetUpdateFlow.collectLatest {
                delay(200L) // Debounce rapid state changes like battery
                try {
                    val manager = GlanceAppWidgetManager(context)
                    val widget = NoiseControlWidget()
                    manager.getGlanceIds(widget.javaClass).forEach { glanceId ->
                        updateAppWidgetState(context, glanceId) { prefs ->
                            val key = booleanPreferencesKey("force_update")
                            val current = prefs[key] ?: false
                            prefs[key] = !current
                        }
                        widget.update(context, glanceId)
                    }
                } catch (e: Exception) {
                    Log.e("WidgetCoordinator", "Failed to update widget", e)
                }
            }
        }
    }
}
