package com.benegedeniz.budsdynamiceq.data.model

object CustomEqualizer {
    const val BAND_COUNT = 9
    const val GAIN_MIN = -10
    const val GAIN_MAX = 10

    val BAND_LABELS = listOf("63", "125", "250", "500", "1k", "2k", "4k", "8k", "16k")
    val FLAT: List<Int> = List(BAND_COUNT) { 0 }

    val WEARABLE_PRESETS = listOf(
        EqPreset.NORMAL,
        EqPreset.BASS_BOOST,
        EqPreset.SOFT,
        EqPreset.DYNAMIC,
        EqPreset.CLEAR,
        EqPreset.TREBLE_BOOST
    )

    fun clamp(bands: List<Int>): List<Int> {
        if (bands.size != BAND_COUNT) return FLAT
        return bands.map { it.coerceIn(GAIN_MIN, GAIN_MAX) }
    }

    fun parseStored(raw: String?): List<Int> {
        if (raw.isNullOrBlank()) return FLAT
        val parts = raw.split(',')
        if (parts.size != BAND_COUNT) return FLAT
        return try {
            clamp(parts.map { it.trim().toInt() })
        } catch (_: NumberFormatException) {
            FLAT
        }
    }

    fun serialize(bands: List<Int>): String = clamp(bands).joinToString(",")

    fun previewBands(preset: EqPreset, custom: List<Int>): List<Int> {
        return when (preset) {
            EqPreset.CUSTOM_1, EqPreset.CUSTOM_2, EqPreset.CUSTOM_3 -> clamp(custom)
            EqPreset.BASS_BOOST -> listOf(6, 4, 2, 0, 0, 0, 0, 0, 0)
            EqPreset.SOFT -> listOf(-2, 0, 2, 3, 2, 0, -1, -2, -3)
            EqPreset.DYNAMIC -> listOf(4, 2, 0, -1, 0, 1, 3, 4, 3)
            EqPreset.CLEAR -> listOf(-3, -1, 0, 1, 2, 3, 4, 3, 2)
            EqPreset.TREBLE_BOOST -> listOf(0, 0, 0, 0, 1, 2, 4, 6, 6)
            else -> FLAT
        }
    }

    fun formatGain(gain: Int): String = when {
        gain > 0 -> "+$gain"
        else -> gain.toString()
    }
}
