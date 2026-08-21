package com.benegedeniz.budsdynamiceq.bluetooth

enum class BudsModel(
    @param:androidx.annotation.StringRes val displayNameRes: Int,
    val supportsAdaptiveNC: Boolean,
    val supportsTransparencyNC: Boolean,
    val supportsConversationDetection: Boolean,
    val supportsFitTest: Boolean,
    val supportsHeadGestures: Boolean,
    val isExperimentalGestures: Boolean,
    val supportsFmgRingWhileWearing: Boolean,
    val supportsDoubleTapEdge: Boolean,
    val supportsCustomEqualizer: Boolean
) {
    BUDS_4_PRO(
        displayNameRes = com.benegedeniz.budsdynamiceq.R.string.model_buds_4_pro,
        supportsAdaptiveNC = true,
        supportsTransparencyNC = true,
        supportsConversationDetection = true,
        supportsFitTest = true,
        supportsHeadGestures = true,
        isExperimentalGestures = false,
        supportsFmgRingWhileWearing = false,
        supportsDoubleTapEdge = false,
        supportsCustomEqualizer = true
    ),
    BUDS_4(
        displayNameRes = com.benegedeniz.budsdynamiceq.R.string.model_buds_4,
        supportsAdaptiveNC = false,
        supportsTransparencyNC = false,
        supportsConversationDetection = false,
        supportsFitTest = false,
        supportsHeadGestures = true,
        isExperimentalGestures = false,
        supportsFmgRingWhileWearing = false,
        supportsDoubleTapEdge = false,
        supportsCustomEqualizer = true
    ),
    BUDS_3_PRO(
        displayNameRes = com.benegedeniz.budsdynamiceq.R.string.model_buds_3_pro,
        supportsAdaptiveNC = true,
        supportsTransparencyNC = true,
        supportsConversationDetection = true,
        supportsFitTest = true,
        supportsHeadGestures = true,
        isExperimentalGestures = true,
        supportsFmgRingWhileWearing = false,
        supportsDoubleTapEdge = false,
        supportsCustomEqualizer = true
    ),
    BUDS_3(
        displayNameRes = com.benegedeniz.budsdynamiceq.R.string.model_buds_3,
        supportsAdaptiveNC = false,
        supportsTransparencyNC = false,
        supportsConversationDetection = false,
        supportsFitTest = false,
        supportsHeadGestures = true,
        isExperimentalGestures = true,
        supportsFmgRingWhileWearing = false,
        supportsDoubleTapEdge = false,
        supportsCustomEqualizer = true
    ),
    BUDS_3_FE(
        displayNameRes = com.benegedeniz.budsdynamiceq.R.string.model_buds_3_fe,
        supportsAdaptiveNC = false,
        supportsTransparencyNC = true,
        supportsConversationDetection = false,
        supportsFitTest = true,
        supportsHeadGestures = false,
        isExperimentalGestures = false,
        supportsFmgRingWhileWearing = true,
        supportsDoubleTapEdge = false,
        supportsCustomEqualizer = false
    ),
    BUDS_2_PRO(
        displayNameRes = com.benegedeniz.budsdynamiceq.R.string.model_buds_2_pro,
        supportsAdaptiveNC = false,
        supportsTransparencyNC = true,
        supportsConversationDetection = true,
        supportsFitTest = true,
        supportsHeadGestures = true,
        isExperimentalGestures = true,
        supportsFmgRingWhileWearing = true,
        supportsDoubleTapEdge = true,
        supportsCustomEqualizer = false
    ),
    BUDS_2(
        displayNameRes = com.benegedeniz.budsdynamiceq.R.string.model_buds_2,
        supportsAdaptiveNC = false,
        supportsTransparencyNC = true,
        supportsConversationDetection = false,
        supportsFitTest = true,
        supportsHeadGestures = true,
        isExperimentalGestures = true,
        supportsFmgRingWhileWearing = true,
        supportsDoubleTapEdge = true,
        supportsCustomEqualizer = false
    ),
    BUDS_FE(
        displayNameRes = com.benegedeniz.budsdynamiceq.R.string.model_buds_fe,
        supportsAdaptiveNC = false,
        supportsTransparencyNC = true,
        supportsConversationDetection = false,
        supportsFitTest = true,
        supportsHeadGestures = false,
        isExperimentalGestures = false,
        supportsFmgRingWhileWearing = true,
        supportsDoubleTapEdge = false,
        supportsCustomEqualizer = false
    ),
    UNKNOWN(
        displayNameRes = com.benegedeniz.budsdynamiceq.R.string.model_buds_unknown,
        supportsAdaptiveNC = true,
        supportsTransparencyNC = true,
        supportsConversationDetection = true,
        supportsFitTest = true,
        supportsHeadGestures = true,
        isExperimentalGestures = true,
        supportsFmgRingWhileWearing = true,
        supportsDoubleTapEdge = true,
        supportsCustomEqualizer = true
    )
}
