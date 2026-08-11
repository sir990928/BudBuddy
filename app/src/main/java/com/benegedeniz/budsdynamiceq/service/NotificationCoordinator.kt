package com.benegedeniz.budsdynamiceq.service

import android.content.Context
import com.benegedeniz.budsdynamiceq.R
import com.benegedeniz.budsdynamiceq.bluetooth.BudsController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class NotificationCoordinator(
    private val context: Context,
    private val scope: CoroutineScope,
    private val budsController: BudsController,
    private val transientNotificationFlow: MutableStateFlow<Pair<String, String>?>,
    private val notificationManagerHelper: NotificationManagerHelper
) {
    private val languageFlow = MutableStateFlow(0L)
    private val prefListener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == "AppLanguage") {
            languageFlow.value = System.currentTimeMillis()
        }
    }

    fun start() {
        context.getSharedPreferences("BudsPrefs", Context.MODE_PRIVATE)
            .registerOnSharedPreferenceChangeListener(prefListener)
        scope.launch {
            val batteryPlacementFlow = combine(
                budsController.batteryL,
                budsController.batteryR,
                budsController.placementL,
                budsController.placementR
            ) { bL, bR, pL, pR ->
                object {
                    val bL = bL; val bR = bR; val pL = pL; val pR = pR
                }
            }

            val deviceStateFlow = combine(
                budsController.isConnected,
                batteryPlacementFlow,
                budsController.oneEarbudNoiseControlEnabled
            ) { connected, bp, oneEarbudEnabled ->
                object {
                    val connected = connected
                    val bL = bp.bL; val bR = bp.bR
                    val pL = bp.pL; val pR = bp.pR
                    val oneEarbudEnabled = oneEarbudEnabled
                }
            }
            
            val ruleInfoFlow = combine(
                budsController.lastMatchedRule,
                budsController.manualPreset,
                budsController.manualNoiseControl
            ) { matchedRule, manualEq, manualNc ->
                object {
                    val matchedRule = matchedRule
                    val manualEq = manualEq
                    val manualNc = manualNc
                }
            }

            combine(
                transientNotificationFlow,
                ruleInfoFlow,
                deviceStateFlow,
                budsController.activeNoiseControl,
                languageFlow
            ) { transient, ruleInfo, deviceState, activeNc, _ ->
                object {
                    val transient = transient
                    val ruleInfo = ruleInfo
                    val deviceState = deviceState
                    val activeNc = activeNc
                }
            }.collect { state ->
                val localizedContext = com.benegedeniz.budsdynamiceq.util.LanguageUtils.setLocale(context)
                val connected = state.deviceState.connected
                
                if (!connected) {
                    notificationManagerHelper.updateNotification(
                        titleText = localizedContext.getString(R.string.disconnected), 
                        ruleNcText = localizedContext.getString(R.string.waiting_for_buds), 
                        hardwareNcText = "", 
                        lBatteryText = "", 
                        rBatteryText = "", 
                        isLWorn = false,
                        isRWorn = false,
                        isConnected = false,
                        toggleButtonText = ""
                    )
                    return@collect
                }

                val isLWorn = state.deviceState.pL == com.benegedeniz.budsdynamiceq.data.model.PlacementState.WEARING
                val isRWorn = state.deviceState.pR == com.benegedeniz.budsdynamiceq.data.model.PlacementState.WEARING
                
                var activeRuleTitle = ""
                var activeRuleText = ""

                val matchingRule = state.ruleInfo.matchedRule
                val manualEq = state.ruleInfo.manualEq
                val manualNc = state.ruleInfo.manualNc

                if (matchingRule != null) {
                    val activeRuleInheritsEq = matchingRule.preset == com.benegedeniz.budsdynamiceq.data.model.EqPreset.DEFAULT
                    val activeRuleInheritsNc = matchingRule.noiseControl == com.benegedeniz.budsdynamiceq.data.model.NoiseControlMode.DEFAULT
                    
                    val eqToSend = if (activeRuleInheritsEq) manualEq else matchingRule.preset
                    val ncToSend = if (activeRuleInheritsNc) manualNc else matchingRule.noiseControl
                    
                    activeRuleTitle = localizedContext.getString(R.string.active_rule, matchingRule.keyword)
                    activeRuleText = localizedContext.getString(R.string.settings_format, eqToSend?.let { localizedContext.getString(it.displayNameRes) } ?: localizedContext.getString(R.string.none), ncToSend?.let { localizedContext.getString(it.displayNameRes) } ?: localizedContext.getString(R.string.none))
                } else {
                    activeRuleTitle = localizedContext.getString(R.string.default_settings)
                    activeRuleText = localizedContext.getString(R.string.settings_format, manualEq?.let { localizedContext.getString(it.displayNameRes) } ?: localizedContext.getString(R.string.none), manualNc?.let { localizedContext.getString(it.displayNameRes) } ?: localizedContext.getString(R.string.none))
                }
                
                val lText = localizedContext.getString(R.string.percentage_format, state.deviceState.bL.toString())
                val rText = localizedContext.getString(R.string.percentage_format, state.deviceState.bR.toString())
                
                val wearingOne = (isLWorn && !isRWorn) || (isRWorn && !isLWorn)
                val toggleText = if (wearingOne && !state.deviceState.oneEarbudEnabled) {
                    localizedContext.getString(R.string.toggle_off_ambient)
                } else if (!budsController.effectiveModel.value.supportsTransparencyNC) {
                    localizedContext.getString(R.string.toggle_anc_off)
                } else {
                    localizedContext.getString(R.string.toggle_anc_ambient)
                }
                val hardwareNcText = localizedContext.getString(R.string.active_nc_format, state.activeNc?.let { localizedContext.getString(it.displayNameRes) } ?: localizedContext.getString(R.string.unknown))

                if (state.transient != null) {
                    notificationManagerHelper.updateNotification(
                        titleText = state.transient.first,
                        ruleNcText = state.transient.second,
                        hardwareNcText = hardwareNcText,
                        lBatteryText = lText,
                        rBatteryText = rText,
                        isLWorn = isLWorn,
                        isRWorn = isRWorn,
                        isConnected = connected,
                        toggleButtonText = toggleText
                    )
                } else {
                    notificationManagerHelper.updateNotification(
                        titleText = activeRuleTitle,
                        ruleNcText = activeRuleText,
                        hardwareNcText = hardwareNcText,
                        lBatteryText = lText,
                        rBatteryText = rText,
                        isLWorn = isLWorn,
                        isRWorn = isRWorn,
                        isConnected = connected,
                        toggleButtonText = toggleText
                    )
                }
            }
        }
    }
}
