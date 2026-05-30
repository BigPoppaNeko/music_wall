package com.jfcardenas.musicwall.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jfcardenas.musicwall.ui.theme.Purple

@Composable
fun MWLogo(modifier: Modifier = Modifier, size: Dp = 96.dp) {
    Box(
        modifier = modifier
            .size(size)
            .border(2.dp, Purple, CircleShape)
            .background(Color(0xFF1A0A2E), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "MW",
            fontSize = (size.value * 0.26f).sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            letterSpacing = (-0.5).sp,
        )
    }
}
