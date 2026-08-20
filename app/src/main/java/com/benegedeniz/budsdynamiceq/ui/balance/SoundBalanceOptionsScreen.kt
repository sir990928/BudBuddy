package com.benegedeniz.budsdynamiceq.ui.balance

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.benegedeniz.budsdynamiceq.R
import com.benegedeniz.budsdynamiceq.data.model.PlacementState
import com.benegedeniz.budsdynamiceq.ui.buds.BudsViewModel
import com.benegedeniz.budsdynamiceq.ui.components.PageHeader
import com.benegedeniz.budsdynamiceq.ui.components.bounceClick
import com.benegedeniz.budsdynamiceq.ui.components.rememberPageHeaderOverlayPadding

@Composable
fun SoundBalanceOptionsScreen(
    viewModel: BudsViewModel,
    onBack: () -> Unit,
    onTakeTestClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val uiState by viewModel.uiState.collectAsState()
    val haptic = LocalHapticFeedback.current
    val scrollState = rememberScrollState()
    val headerOverlay = rememberPageHeaderOverlayPadding()

    val isConnected = uiState.isConnected
    val stereoBalance = uiState.stereoBalance
    val placementL = uiState.placementL
    val placementR = uiState.placementR
    val bothInEar = isConnected && placementL == PlacementState.WEARING && placementR == PlacementState.WEARING

    var isDraggingBalance by remember { mutableStateOf(false) }
    var localBalance by remember(stereoBalance) { mutableFloatStateOf(stereoBalance.toFloat()) }
    var lastSentBalanceTime by remember { mutableLongStateOf(0L) }
    var lastHapticValue by remember { mutableIntStateOf(stereoBalance) }

    val currentIntBalance = if (isDraggingBalance) localBalance.toInt() else stereoBalance

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = headerOverlay.listTop,
                    bottom = 32.dp
                )
        ) {
            // Card 1: Balance Adjustment Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(if (isConnected) 1f else 0.5f),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SwapHoriz,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = stringResource(R.string.left_right_sound_balance),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = when (currentIntBalance) {
                                        16 -> stringResource(R.string.buds_balanced)
                                        in 0..15 -> stringResource(R.string.buds_battery_l, ((16 - currentIntBalance) / 16f * 100).toInt())
                                        else -> stringResource(R.string.buds_battery_r, ((currentIntBalance - 16) / 16f * 100).toInt())
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Slider(
                        value = if (isDraggingBalance) localBalance else stereoBalance.toFloat(),
                        onValueChange = { newValue ->
                            isDraggingBalance = true
                            val snapped = if (newValue in 15f..17f) 16f else newValue
                            val newInt = snapped.toInt()

                            if (newInt != lastHapticValue) {
                                if (newInt == 16) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                } else {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                lastHapticValue = newInt
                            }

                            localBalance = snapped
                            val currentTime = System.currentTimeMillis()
                            if (currentTime - lastSentBalanceTime > 250) {
                                viewModel.setStereoBalance(newInt)
                                lastSentBalanceTime = currentTime
                            }
                        },
                        onValueChangeFinished = {
                            isDraggingBalance = false
                            viewModel.setStereoBalance(localBalance.toInt())
                        },
                        valueRange = 0f..32f,
                        enabled = isConnected,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.l),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = stringResource(R.string.buds_balanced),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Text(
                            text = stringResource(R.string.r),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    AnimatedVisibility(
                        visible = currentIntBalance != 16,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Column {
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    localBalance = 16f
                                    viewModel.setStereoBalance(16)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp)
                                    .bounceClick(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.reset_to_center),
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Card 2: Hearing Test Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Hearing,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.sound_balance_test),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = stringResource(R.string.this_test_will_determine_the_optimal_lef),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onTakeTestClick()
                        },
                        enabled = bothInEar,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .bounceClick(enabled = bothInEar),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Hearing,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.take_hearing_test),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (!bothInEar && isConnected) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.requires_both_earbuds_to_be_worn),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Header overlay (positioned at bottom of Box so it renders above scroll view)
        PageHeader(
            title = stringResource(R.string.left_right_sound_balance),
            isScrolled = scrollState.value > 10,
            modifier = headerOverlay.headerModifier,
            actionIcon = {
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onBack()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.close)
                    )
                }
            }
        )
    }
}
