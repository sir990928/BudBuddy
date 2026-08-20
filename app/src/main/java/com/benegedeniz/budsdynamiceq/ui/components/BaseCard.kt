package com.benegedeniz.budsdynamiceq.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun BaseCard(
    modifier: Modifier = Modifier,
    containerColor: Color? = null,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    val actualContainerColor = containerColor ?: MaterialTheme.colorScheme.surface
    val cardModifier = modifier
        .fillMaxWidth()
        .then(if (onClick != null) Modifier.bounceClick(enabled = enabled) { onClick() } else Modifier)

    Card(
        modifier = cardModifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = actualContainerColor),
        content = content
    )
}
