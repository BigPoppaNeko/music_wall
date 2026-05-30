package com.jfcardenas.musicwall.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jfcardenas.musicwall.ui.components.AlbumGridBackground
import com.jfcardenas.musicwall.ui.components.MWLogo
import com.jfcardenas.musicwall.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onComenzar: () -> Unit,
    onIniciarSesion: () -> Unit,
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(150); visible = true }

    Box(modifier = Modifier.fillMaxSize().background(Background)) {

        AlbumGridBackground()

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 10 }),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(56.dp))

                MWLogo(size = 96.dp)

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "MUSICWALL",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-1).sp,
                    color = Color.White,
                )

                Spacer(Modifier.height(20.dp))

                Text(
                    text = "Tu música.\nTu mural.",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    color = Purple,
                    textAlign = TextAlign.Center,
                    lineHeight = 40.sp,
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "Transformamos lo que escuchas\nen algo que puedes ver y sentir.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = TextSecondary,
                )

                Spacer(Modifier.weight(1f))

                Button(
                    onClick = onComenzar,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Purple),
                ) {
                    Text(
                        text = "Comenzar",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                    )
                }

                Spacer(Modifier.height(20.dp))

                Row {
                    Text(
                        text = "¿Ya tienes cuenta? ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                    )
                    Text(
                        text = "Inicia sesión",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Purple,
                        modifier = Modifier.clickable { onIniciarSesion() },
                    )
                }

                Spacer(Modifier.height(52.dp))
            }
        }
    }
}
