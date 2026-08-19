package com.benegedeniz.budsdynamiceq.data.model

import androidx.annotation.StringRes
import com.benegedeniz.budsdynamiceq.R

enum class EqPreset(@param:StringRes val displayNameRes: Int, val payloadByte: Byte) {
    DEFAULT(R.string.eq_default, -2),
    IGNORE(R.string.eq_ignore, -1),
    NORMAL(R.string.eq_normal, 0x00),
    BASS_BOOST(R.string.eq_bass_boost, 0x01),
    SOFT(R.string.eq_soft, 0x02),
    DYNAMIC(R.string.eq_dynamic, 0x03),
    CLEAR(R.string.eq_clear, 0x04),
    TREBLE_BOOST(R.string.eq_treble_boost, 0x05),
    // Wearable "Custom": EQUALIZER payload 0x07 (preset index 6, sent as index+1).
    CUSTOM(R.string.eq_custom, 0x07);
}
