package com.example.mindora.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Deprecated("Use PandaStudyMascot")
@Composable
fun MindyMascot(
    modifier: Modifier = Modifier,
    size: Dp = 96.dp,
    animate: Boolean = true
) {
    PandaStudyMascot(
        modifier = modifier,
        size = size,
        mood = PandaMood.HAPPY,
        animate = animate
    )
}
