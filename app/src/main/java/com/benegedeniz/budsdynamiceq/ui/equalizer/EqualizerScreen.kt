package com.benegedeniz.budsdynamiceq.ui.equalizer

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.benegedeniz.budsdynamiceq.R
import com.benegedeniz.budsdynamiceq.data.model.CustomEqualizer
import com.benegedeniz.budsdynamiceq.data.model.EqPreset
import com.benegedeniz.budsdynamiceq.data.model.EqRule
import com.benegedeniz.budsdynamiceq.ui.components.PageHeader
import com.benegedeniz.budsdynamiceq.ui.components.bounceClick
import com.benegedeniz.budsdynamiceq.ui.components.rememberPageHeaderOverlayPadding
import com.benegedeniz.budsdynamiceq.ui.rules.RulesViewModel
import kotlinx.coroutines.delay

@Composable
fun EqualizerScreen(
    viewModel: RulesViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val uiState by viewModel.uiState.collectAsState()
    val savedBands1 by viewModel.customEqBands1.collectAsState()
    val savedBands2 by viewModel.customEqBands2.collectAsState()
    val savedBands3 by viewModel.customEqBands3.collectAsState()
    val haptic = LocalHapticFeedback.current
    val scrollState = rememberScrollState()
    val headerOverlay = rememberPageHeaderOverlayPadding()

    val supportsCustom = uiState.effectiveModel.supportsCustomEqualizer
    val effectiveEq = remember(uiState.lastMatchedRule, uiState.manualPreset) {
        effectiveEqPreset(uiState.lastMatchedRule, uiState.manualPreset)
    }
    val isCustomActive = effectiveEq.isCustom
    val slidersEnabled = uiState.isConnected && supportsCustom && isCustomActive

    val activeCustomBands = when (effectiveEq) {
        EqPreset.CUSTOM_2 -> savedBands2
        EqPreset.CUSTOM_3 -> savedBands3
        else -> savedBands1
    }

    var localBands by remember { mutableStateOf(activeCustomBands) }
    var isDragging by remember { mutableStateOf(false) }

    LaunchedEffect(effectiveEq, activeCustomBands) {
        if (!isDragging) localBands = activeCustomBands
    }

    LaunchedEffect(localBands, slidersEnabled) {
        if (!slidersEnabled) return@LaunchedEffect
        delay(250)
        viewModel.setCustomEqBands(effectiveEq, localBands)
    }

    val displayedBands = if (isCustomActive) {
        localBands
    } else {
        CustomEqualizer.previewBands(effectiveEq, activeCustomBands)
    }

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
            EqualizerChartCard(
                bands = displayedBands,
                enabled = slidersEnabled,
                dimmed = !uiState.isConnected,
                onBandChange = { index, value ->
                    if (localBands.getOrNull(index) != value) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                    localBands = localBands.toMutableList().also { it[index] = value }
                },
                onDragStateChange = { dragging -> isDragging = dragging }
            )

            Spacer(modifier = Modifier.height(24.dp))

            val presetPairs = CustomEqualizer.WEARABLE_PRESETS.chunked(2)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                presetPairs.forEach { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        pair.forEach { preset ->
                            EqPresetChip(
                                preset = preset,
                                selected = effectiveEq == preset,
                                enabled = uiState.isConnected,
                                modifier = Modifier.weight(1f),
                                onClick = { viewModel.setManualPreset(preset) }
                            )
                        }
                        if (pair.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
                if (supportsCustom) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        EqPresetChip(
                            preset = EqPreset.CUSTOM_1,
                            selected = effectiveEq == EqPreset.CUSTOM_1,
                            enabled = uiState.isConnected,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.setManualPreset(EqPreset.CUSTOM_1) }
                        )
                        EqPresetChip(
                            preset = EqPreset.CUSTOM_2,
                            selected = effectiveEq == EqPreset.CUSTOM_2,
                            enabled = uiState.isConnected,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.setManualPreset(EqPreset.CUSTOM_2) }
                        )
                        EqPresetChip(
                            preset = EqPreset.CUSTOM_3,
                            selected = effectiveEq == EqPreset.CUSTOM_3,
                            enabled = uiState.isConnected,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.setManualPreset(EqPreset.CUSTOM_3) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (supportsCustom) {
                    stringResource(R.string.equalizer_adjust_hint)
                } else {
                    stringResource(R.string.equalizer_custom_unsupported)
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        PageHeader(
            title = stringResource(R.string.equalizer),
            isScrolled = scrollState.value > 10,
            modifier = headerOverlay.headerModifier,
            actionIcon = {
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onBack()
                    }
                ) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.close))
                }
            }
        )
    }
}

@Composable
private fun EqPresetChip(
    preset: EqPreset,
    selected: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val background by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        animationSpec = tween(180),
        label = "eqChipBg"
    )
    val content by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
        animationSpec = tween(180),
        label = "eqChipFg"
    )
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(background)
            .alpha(if (enabled) 1f else 0.5f)
            .bounceClick(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(preset.displayNameRes),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = content,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun EqualizerChartCard(
    bands: List<Int>,
    enabled: Boolean,
    dimmed: Boolean,
    onBandChange: (Int, Int) -> Unit,
    onDragStateChange: (Boolean) -> Unit
) {
    val curveColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)
    val zeroLineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val gainColor = MaterialTheme.colorScheme.onSurface
    val holeColor = MaterialTheme.colorScheme.surface

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 12.dp, vertical = 16.dp)
            .alpha(if (dimmed) 0.55f else 1f)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            bands.forEachIndexed { index, gain ->
                Text(
                    text = CustomEqualizer.formatGain(gain),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = gainColor,
                    fontSize = 12.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val bandWidth = size.width / bands.size
                val steps = 4
                for (i in 0..steps) {
                    val y = size.height * i / steps
                    drawLine(
                        color = if (i == steps / 2) zeroLineColor else gridColor,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = if (i == steps / 2) 1.5.dp.toPx() else 1.dp.toPx()
                    )
                }
                for (i in 0..bands.size) {
                    val x = bandWidth * i
                    drawLine(
                        color = gridColor,
                        start = Offset(x, 0f),
                        end = Offset(x, size.height),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                val points = bands.mapIndexed { index, gain ->
                    Offset(
                        x = bandWidth * (index + 0.5f),
                        y = gainToY(gain, size.height)
                    )
                }
                if (points.size >= 2) {
                    val curve = smoothPath(points)
                    val fill = Path().apply {
                        addPath(curve)
                        lineTo(points.last().x, size.height)
                        lineTo(points.first().x, size.height)
                        close()
                    }
                    drawPath(
                        path = fill,
                        brush = Brush.verticalGradient(
                            colors = listOf(curveColor.copy(alpha = 0.28f), Color.Transparent)
                        )
                    )
                    drawPath(
                        path = curve,
                        color = curveColor,
                        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                    )
                    points.forEach { point ->
                        drawCircle(color = curveColor, radius = 6.dp.toPx(), center = point)
                        drawCircle(color = holeColor, radius = 2.5.dp.toPx(), center = point)
                    }
                }
            }
            Row(modifier = Modifier.fillMaxSize()) {
                bands.forEachIndexed { index, _ ->
                    val label = CustomEqualizer.BAND_LABELS[index]
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .semantics {
                                contentDescription = label
                            }
                            .pointerInput(enabled, index) {
                                if (!enabled) return@pointerInput
                                awaitEachGesture {
                                    val down = awaitFirstDown()
                                    onDragStateChange(true)
                                    onBandChange(index, yToGain(down.position.y, size.height.toFloat()))
                                    drag(down.id) { change ->
                                        change.consume()
                                        onBandChange(index, yToGain(change.position.y, size.height.toFloat()))
                                    }
                                    onDragStateChange(false)
                                }
                            }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            CustomEqualizer.BAND_LABELS.forEach { label ->
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = labelColor,
                    maxLines = 1
                )
            }
        }
    }
}

private fun gainToY(gain: Int, height: Float): Float {
    val range = (CustomEqualizer.GAIN_MAX - CustomEqualizer.GAIN_MIN).toFloat()
    return height * (CustomEqualizer.GAIN_MAX - gain) / range
}

private fun yToGain(y: Float, height: Float): Int {
    if (height <= 0f) return 0
    val t = (y / height).coerceIn(0f, 1f)
    val gain = CustomEqualizer.GAIN_MAX - t * (CustomEqualizer.GAIN_MAX - CustomEqualizer.GAIN_MIN)
    return gain.kotlinRoundToInt()
}

private fun Float.kotlinRoundToInt(): Int = kotlin.math.round(this).toInt().coerceIn(
    CustomEqualizer.GAIN_MIN,
    CustomEqualizer.GAIN_MAX
)

private fun smoothPath(points: List<Offset>): Path {
    val path = Path()
    if (points.isEmpty()) return path
    path.moveTo(points[0].x, points[0].y)
    if (points.size == 1) return path
    for (i in 0 until points.lastIndex) {
        val p0 = points[i]
        val p1 = points[i + 1]
        val c1 = Offset(p0.x + (p1.x - p0.x) / 2f, p0.y)
        val c2 = Offset(p0.x + (p1.x - p0.x) / 2f, p1.y)
        path.cubicTo(c1.x, c1.y, c2.x, c2.y, p1.x, p1.y)
    }
    return path
}

private fun effectiveEqPreset(lastMatchedRule: EqRule?, manualPreset: EqPreset?): EqPreset {
    val rulePreset = lastMatchedRule?.preset
    return when {
        rulePreset == null || rulePreset == EqPreset.DEFAULT -> manualPreset ?: EqPreset.NORMAL
        else -> rulePreset
    }
}
