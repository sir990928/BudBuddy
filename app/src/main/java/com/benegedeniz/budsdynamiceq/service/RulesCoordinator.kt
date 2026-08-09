package com.benegedeniz.budsdynamiceq.service

import android.content.Context
import com.benegedeniz.budsdynamiceq.bluetooth.BudsController
import com.benegedeniz.budsdynamiceq.data.RulesRepository
import com.benegedeniz.budsdynamiceq.data.model.EqPreset
import com.benegedeniz.budsdynamiceq.data.model.NoiseControlMode
import com.benegedeniz.budsdynamiceq.media.MediaObserver
import com.benegedeniz.budsdynamiceq.rules.RulesEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class RulesCoordinator(
    private val context: Context,
    private val scope: CoroutineScope,
    private val budsController: BudsController,
    private val mediaObserver: MediaObserver,
    private val rulesRepository: RulesRepository
) {
    private val rulesEngine = RulesEngine()

    fun start() {
        scope.launch {
            var lastSongWithDefault: String? = null
            var wasConnected = false
            var wasBothInEar = false
            var lastAppliedManualEq: EqPreset? = null
            var lastAppliedManualNc: NoiseControlMode? = null

            val ruleStateFlow = combine(
                mediaObserver.currentMetadata,
                rulesRepository.rules,
                budsController.manualPreset,
                budsController.manualNoiseControl
            ) { metadata, rulesList, manualEq, manualNc ->
                object {
                    val metadata = metadata
                    val rulesList = rulesList
                    val manualEq = manualEq
                    val manualNc = manualNc
                }
            }
            
            val ruleEvalFlow = combine(
                budsController.isConnected,
                budsController.placementL,
                budsController.placementR,
                ruleStateFlow
            ) { connected, pL, pR, ruleState ->
                object {
                    val connected = connected
                    val pL = pL
                    val pR = pR
                    val ruleState = ruleState
                }
            }

            ruleEvalFlow.collect { state ->
                if (!state.connected) {
                    wasConnected = false
                    return@collect
                }
                
                val justConnected = !wasConnected
                wasConnected = true

                val isLWorn = state.pL == com.benegedeniz.budsdynamiceq.data.model.PlacementState.WEARING
                val isRWorn = state.pR == com.benegedeniz.budsdynamiceq.data.model.PlacementState.WEARING
                val bothInEar = isLWorn && isRWorn
                val justPutBothInEar = bothInEar && !wasBothInEar
                wasBothInEar = bothInEar

                val metadata = state.ruleState.metadata
                val manualEq = state.ruleState.manualEq
                val manualNc = state.ruleState.manualNc

                val matchingRule = rulesEngine.evaluate(metadata, state.ruleState.rulesList)
                if (matchingRule != null) {
                    val activeRuleInheritsEq = matchingRule.preset == EqPreset.DEFAULT
                    val activeRuleInheritsNc = matchingRule.noiseControl == NoiseControlMode.DEFAULT
                    
                    val ruleDefaultChanged = (activeRuleInheritsEq && lastAppliedManualEq != manualEq) || 
                                             (activeRuleInheritsNc && lastAppliedManualNc != manualNc)
                                             
                    val eqToSend = if (activeRuleInheritsEq) manualEq else matchingRule.preset
                    val ncToSend = if (activeRuleInheritsNc) manualNc else matchingRule.noiseControl
                                             
                    if (justConnected || justPutBothInEar || budsController.lastMatchedRule.value != matchingRule || ruleDefaultChanged) {
                        budsController.setLastMatchedRule(matchingRule)
                        if (eqToSend != null) budsController.sendEqualizer(eqToSend)
                        if (ncToSend != null) budsController.sendNoiseControl(ncToSend)
                        
                        lastAppliedManualEq = manualEq
                        lastAppliedManualNc = manualNc
                    }
                    lastSongWithDefault = null
                } else {
                    val justDroppedOut = budsController.lastMatchedRule.value != null
                    budsController.setLastMatchedRule(null)
                    
                    val songDisplayString = metadata?.displayString ?: ""
                    val defaultChanged = lastAppliedManualEq != manualEq || lastAppliedManualNc != manualNc
                    if (justConnected || justDroppedOut || justPutBothInEar || lastSongWithDefault != songDisplayString || defaultChanged) {
                        if (manualEq != null) budsController.sendEqualizer(manualEq)
                        if (manualNc != null) budsController.sendNoiseControl(manualNc)
                        
                        lastSongWithDefault = songDisplayString
                        lastAppliedManualEq = manualEq
                        lastAppliedManualNc = manualNc
                    }
                }
            }
        }
    }
}
