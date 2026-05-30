package com.jfcardenas.musicwall.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jfcardenas.musicwall.ui.model.STYLE_OPTIONS
import com.jfcardenas.musicwall.ui.model.StyleOption
import com.jfcardenas.musicwall.ui.theme.*

@Composable
fun StyleSelectionScreen(
    onBack: (() -> Unit)? = null,
    onContinuar: (styleId: String) -> Unit,
) {
    var selected by remember { mutableStateOf(STYLE_OPTIONS.first()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding(),
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = TextPrimary)
                }
            } else {
                Spacer(Modifier.width(16.dp))
            }
            Column {
                Text(
                    text = "Elige tu atmósfera",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Cada estilo transforma tu música de forma distinta.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            items(STYLE_OPTIONS) { style ->
                AtmosphericStyleCard(
                    style = style,
                    selected = style.id == selected.id,
                    onClick = { selected = style },
                )
            }
            item { Spacer(Modifier.height(4.dp)) }
        }

        // Bottom CTA
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .navigationBarsPadding(),
        ) {
            Button(
                onClick = { onContinuar(selected.id) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Purple),
            ) {
                Text(
                    text = "Generar mural",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                )
            }
        }
    }
}

@Composable
private fun AtmosphericStyleCard(
    style: StyleOption,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val borderColor = if (selected) Purple else Color.Transparent
    val borderWidth = if (selected) 2.dp else 0.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(20.dp))
            .border(borderWidth, borderColor, RoundedCornerShape(20.dp))
            .clickable { onClick() },
    ) {
        // Atmospheric gradient background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        listOf(style.gradientStart, style.gradientEnd)
                    )
                )
        )

        // Subtle noise texture overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        listOf(Color.White.copy(alpha = 0.06f), Color.Transparent)
                    )
                )
        )

        // Bottom scrim for text readability
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.5f)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f)))
                )
        )

        // Text content
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(18.dp),
        ) {
            Text(
                text = style.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = style.tagline.replace("\n", " · "),
                fontSize = 12.sp,
                fontStyle = FontStyle.Italic,
                color = Color.White.copy(alpha = 0.7f),
            )
        }

        // Selected checkmark
        AnimatedVisibility(
            visible = selected,
            enter = fadeIn(tween(200)) + scaleIn(tween(200)),
            exit = fadeOut(tween(150)) + scaleOut(tween(150)),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(Purple, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Seleccionado",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}
