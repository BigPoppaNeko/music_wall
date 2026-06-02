package com.jfcardenas.musicwall.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.jfcardenas.musicwall.auth.GoogleAuthManager
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.launch
import com.jfcardenas.musicwall.ui.theme.*

enum class MusicSource { GOOGLE, LOCAL, LASTFM, SPOTIFY, EXPLORE }

@EntryPoint
@InstallIn(SingletonComponent::class)
interface GoogleAuthEntryPoint {
    fun googleAuthManager(): GoogleAuthManager
}

@Composable
fun SourceSelectionScreen(
    onBack: (() -> Unit)? = null,
    onSourceSelected: (MusicSource) -> Unit,
    onGoogleSignedIn: () -> Unit = {},
    onLocalSelected: () -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var googleLoading by remember { mutableStateOf(false) }
    var googleError by remember { mutableStateOf<String?>(null) }
    val googleAuth = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            GoogleAuthEntryPoint::class.java,
        ).googleAuthManager()
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding(),
    ) {
        if (onBack != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = TextPrimary)
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(8.dp))

            Text(
                text = "¿Cómo quieres descubrir portadas?",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Recomendado: Explorar artistas o conectar Last.fm. El resto es opcional.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(28.dp))

            SourceCard(
                icon = { ExploreIcon() },
                title = "Explorar artistas",
                description = "Elige bandas y descubre portadas al instante",
                onClick = { onSourceSelected(MusicSource.EXPLORE) },
            )

            Spacer(Modifier.height(12.dp))

            SourceCard(
                icon = { LastFmIcon() },
                title = "Last.fm",
                description = "Tu historial real para recomendaciones",
                onClick = { onSourceSelected(MusicSource.LASTFM) },
            )

            Spacer(Modifier.height(12.dp))

            SourceCard(
                icon = { GoogleIcon() },
                title = "Continuar con Google",
                description = "Opcional · cuenta rápida",
                onClick = {
                    if (googleLoading) return@SourceCard
                    val activity = context as? ComponentActivity ?: return@SourceCard
                    scope.launch {
                        googleLoading = true
                        googleError = null
                        googleAuth.signIn(activity).fold(
                            onSuccess = { onGoogleSignedIn() },
                            onFailure = { googleError = it.message ?: "No se pudo conectar con Google" },
                        )
                        googleLoading = false
                    }
                },
            )

            if (googleLoading) {
                Spacer(Modifier.height(8.dp))
                CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Purple, strokeWidth = 2.dp)
            }
            googleError?.let { msg ->
                Spacer(Modifier.height(8.dp))
                Text(msg, fontSize = 12.sp, color = Color(0xFFFF6B6B), textAlign = TextAlign.Center)
            }

            Spacer(Modifier.height(12.dp))

            SourceCard(
                icon = { ScrobblerIcon() },
                title = "Scrobbler integrado",
                description = "Opcional · detecta tu Now Playing",
                onClick = { onLocalSelected() },
            )

            Spacer(Modifier.height(12.dp))

            SourceCard(
                icon = { SpotifyIcon() },
                title = "Spotify",
                description = "Opcional · playlist pública",
                onClick = { onSourceSelected(MusicSource.SPOTIFY) },
            )

            Spacer(Modifier.weight(1f))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(text = "🔒", fontSize = 14.sp)
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "No guardamos tus contraseñas. Todo es local y privado.",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = TextMuted,
                )
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun SourceCard(
    icon: @Composable () -> Unit,
    title: String,
    description: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Card)
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            icon()
        }

        Spacer(Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
        }

        Text(text = "›", fontSize = 22.sp, color = TextSecondary)
    }
}

@Composable
private fun GoogleIcon() {
    Box(
        modifier = Modifier
            .size(48.dp)
            .background(Color.White, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = "G", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4285F4))
    }
}

@Composable
private fun ScrobblerIcon() {
    Box(
        modifier = Modifier
            .size(48.dp)
            .background(Purple.copy(alpha = 0.25f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = "♪", fontSize = 22.sp, color = Purple)
    }
}

@Composable
private fun LastFmIcon() {
    Box(
        modifier = Modifier
            .size(48.dp)
            .background(Color(0xFFD92323), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = "as", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color.White)
    }
}

@Composable
private fun SpotifyIcon() {
    Box(
        modifier = Modifier
            .size(48.dp)
            .background(Color(0xFF1DB954), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = "♫", fontSize = 22.sp, color = Color.Black)
    }
}

@Composable
private fun ExploreIcon() {
    Box(
        modifier = Modifier
            .size(48.dp)
            .background(Color(0xFF2A2A3A), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = "♡", fontSize = 22.sp, color = Purple)
    }
}
