package com.jfcardenas.musicwall.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.jfcardenas.musicwall.service.ScrobbleForegroundService
import com.jfcardenas.musicwall.ui.theme.*

@Composable
fun ScrobbleSetupScreen(
    onBack: () -> Unit,
    onContinue: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var listenerEnabled by remember {
        mutableStateOf(ScrobbleForegroundService.isListenerEnabled(context))
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                listenerEnabled = ScrobbleForegroundService.isListenerEnabled(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = TextPrimary)
            }
            Text("Escucha activa", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(24.dp))
            Text(
                text = "Detectamos lo que suena en tu teléfono",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Activa el acceso a notificaciones para que Music Wall pueda leer el Now Playing de Spotify, YouTube Music y otros reproductores. Filtramos y emparejamos cada canción con Last.fm.",
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
            )
            Spacer(Modifier.height(28.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Card)
                    .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                    .padding(20.dp),
            ) {
                Column {
                    Text(
                        text = if (listenerEnabled) "✓ Acceso activado" else "Pendiente de activar",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (listenerEnabled) Color(0xFF1DB954) else TextPrimary,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "En Ajustes → Acceso a notificaciones → Music Wall",
                        fontSize = 13.sp,
                        color = TextSecondary,
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            if (!listenerEnabled) {
                Button(
                    onClick = { ScrobbleForegroundService.openListenerSettings(context) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Purple),
                ) {
                    Text("Abrir ajustes del sistema")
                }
            } else {
                Button(
                    onClick = {
                        ScrobbleForegroundService.start(context)
                        onContinue()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Purple),
                ) {
                    Text("Continuar")
                }
            }

            Spacer(Modifier.height(12.dp))
            TextButton(onClick = onContinue) {
                Text("Configurar después", color = TextMuted, fontSize = 13.sp)
            }
        }
    }
}
