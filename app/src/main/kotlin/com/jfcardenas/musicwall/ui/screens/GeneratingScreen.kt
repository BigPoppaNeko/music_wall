package com.jfcardenas.musicwall.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.jfcardenas.musicwall.service.CollageWallpaper
import com.jfcardenas.musicwall.ui.components.AlbumGridBackground
import com.jfcardenas.musicwall.ui.components.MWLogo
import com.jfcardenas.musicwall.ui.model.LOADING_QUOTES
import com.jfcardenas.musicwall.ui.model.Quote
import com.jfcardenas.musicwall.ui.theme.*
import com.jfcardenas.musicwall.ui.viewmodel.GeneratingViewModel
import java.io.File

private data class BuildStage(val icon: String, val message: String)

private val BUILD_STAGES = listOf(
    BuildStage("⚙", "Analizando tu música y encontrando su esencia..."),
    BuildStage("◎", "Buscando colores, texturas y recuerdos visuales..."),
    BuildStage("▦", "Organizando tus álbumes en composición..."),
    BuildStage("✦", "Añadiendo detalles finales del mural..."),
)

private enum class ScreenState { LOADING, DONE, ERROR }

@Composable
fun GeneratingScreen(
    styleId: String,
    onComplete: () -> Unit,
    onCancel: () -> Unit,
    vm: GeneratingViewModel = hiltViewModel(),
) {
    val context = LocalContext.current

    val screenState = when (vm.state) {
        is GeneratingViewModel.UiState.Done  -> ScreenState.DONE
        is GeneratingViewModel.UiState.Error -> ScreenState.ERROR
        else                                 -> ScreenState.LOADING
    }

    val progress = (vm.state as? GeneratingViewModel.UiState.Loading)?.progress ?:
        if (screenState == ScreenState.DONE) 1f else 0f

    val stageIndex = (vm.state as? GeneratingViewModel.UiState.Loading)?.stage ?:
        if (screenState == ScreenState.DONE) 3 else 0

    val quoteIndex = when {
        progress < 0.25f -> 0
        progress < 0.50f -> 1
        progress < 0.75f -> 2
        progress < 0.93f -> 3
        else             -> 4
    }

    val collagePath = remember(screenState) {
        if (screenState != ScreenState.DONE) return@remember null
        context.getSharedPreferences(CollageWallpaper.PREFS_NAME, Context.MODE_PRIVATE)
            .getString(CollageWallpaper.PREF_COLLAGE_PATH, null)
            ?.let { if (File(it).exists()) it else null }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
    ) {
        AlbumGridBackground()

        AnimatedContent(
            targetState = screenState,
            transitionSpec = {
                fadeIn(tween(700)).togetherWith(fadeOut(tween(400)))
            },
            label = "screen-state",
        ) { state ->
            when (state) {
                ScreenState.DONE  -> ReadyContent(collagePath = collagePath, onVerMural = onComplete)
                ScreenState.ERROR -> ErrorContent(
                    message = (vm.state as? GeneratingViewModel.UiState.Error)?.message ?: "",
                    onRetry  = { vm.retry() },
                    onCancel = onCancel,
                )
                ScreenState.LOADING -> LoadingContent(
                    progress   = progress,
                    quoteIndex = quoteIndex,
                    stageIndex = stageIndex,
                    onCancel   = onCancel,
                )
            }
        }
    }
}

@Composable
private fun ReadyContent(collagePath: String?, onVerMural: () -> Unit) {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "ready-scale",
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(0.25f))

        MWLogo(size = 72.dp)

        Spacer(Modifier.height(24.dp))

        Text(
            text = "¡Listo!",
            fontSize = 40.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Tu música se convirtió\nen un mural único.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = TextSecondary,
        )

        Spacer(Modifier.height(40.dp))

        Box(
            modifier = Modifier
                .scale(scale)
                .size(width = 148.dp, height = 220.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Card),
        ) {
            if (collagePath != null) {
                AsyncImage(
                    model = File(collagePath),
                    contentDescription = "Tu mural",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF2D1B69), Color(0xFF6B3FAF), Color(0xFF1A0A2E))
                            )
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    MWLogo(size = 48.dp)
                }
            }
        }

        Spacer(Modifier.weight(0.35f))

        Button(
            onClick = onVerMural,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Purple),
        ) {
            Text(
                text = "Ver mural",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
            )
        }

        Spacer(Modifier.height(52.dp))
    }
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit, onCancel: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "✕",
            fontSize = 48.sp,
            color = Color(0xFFE57373),
        )

        Spacer(Modifier.height(20.dp))

        Text(
            text = "Algo salió mal",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = TextSecondary,
        )

        Spacer(Modifier.height(40.dp))

        Button(
            onClick = onRetry,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Purple),
        ) {
            Text(text = "Reintentar", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        }

        Spacer(Modifier.height(12.dp))

        TextButton(onClick = onCancel) {
            Text(text = "Volver", color = TextMuted, fontSize = 15.sp)
        }
    }
}

@Composable
private fun LoadingContent(
    progress: Float,
    quoteIndex: Int,
    stageIndex: Int,
    onCancel: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(64.dp))

        MWLogo(size = 72.dp)

        Spacer(Modifier.weight(0.6f))

        AnimatedContent(
            targetState = quoteIndex,
            transitionSpec = {
                (fadeIn(tween(700)) + slideInVertically { it / 10 })
                    .togetherWith(fadeOut(tween(500)))
            },
            label = "quote",
        ) { idx ->
            QuoteBlock(LOADING_QUOTES[idx % LOADING_QUOTES.size])
        }

        Spacer(Modifier.weight(0.6f))

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp),
            color = Purple,
            trackColor = CardBorder,
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "${(progress * 100).toInt()}%",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = TextSecondary,
        )

        Spacer(Modifier.height(20.dp))

        AnimatedContent(
            targetState = stageIndex,
            transitionSpec = { fadeIn(tween(400)).togetherWith(fadeOut(tween(300))) },
            label = "stage",
        ) { idx ->
            val stage = BUILD_STAGES[idx % BUILD_STAGES.size]
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(text = stage.icon, fontSize = 13.sp, color = Purple)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stage.message,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    fontSize = 13.sp,
                    color = TextSecondary,
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        TextButton(onClick = onCancel) {
            Text(text = "Cancelar", color = TextMuted, fontSize = 15.sp)
        }

        Spacer(Modifier.height(48.dp))
    }
}

@Composable
private fun QuoteBlock(quote: Quote) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = quote.text,
            fontSize = 24.sp,
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Light,
            color = TextPrimary,
            textAlign = TextAlign.Center,
            lineHeight = 34.sp,
        )
        Spacer(Modifier.height(20.dp))
        Text(
            text = "— ${quote.author}",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 2.sp,
            color = TextSecondary,
        )
    }
}
