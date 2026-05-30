package com.jfcardenas.musicwall.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jfcardenas.musicwall.ui.theme.*
import com.jfcardenas.musicwall.ui.viewmodel.LastFmSourceViewModel

@Composable
fun LastFmSourceScreen(
    onBack: () -> Unit,
    onConnected: () -> Unit,
    vm: LastFmSourceViewModel = hiltViewModel(),
) {
    var username by remember { mutableStateOf("") }
    val keyboard = LocalSoftwareKeyboardController.current
    val context = LocalContext.current

    LaunchedEffect(vm.state) {
        if (vm.state is LastFmSourceViewModel.State.Success) {
            onConnected()
        }
    }

    val isLoading = vm.state is LastFmSourceViewModel.State.Loading
    val errorMsg = (vm.state as? LastFmSourceViewModel.State.Error)?.message

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
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = TextPrimary)
            }
            Text(
                text = "Last.fm",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(32.dp))

            // Last.fm logo
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .background(Color(0xFFD92323), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "as",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                )
            }

            Spacer(Modifier.height(28.dp))

            Text(
                text = "Conecta tu cuenta de Last.fm",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = TextPrimary,
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Importa tu historial de scrobbles en tiempo real para crear un mural único.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = TextSecondary,
            )

            Spacer(Modifier.height(36.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it; vm.clearError() },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Tu usuario de Last.fm") },
                placeholder = { Text("ej. nekomamushu") },
                singleLine = true,
                isError = errorMsg != null,
                supportingText = {
                    if (errorMsg != null) {
                        Text(errorMsg, color = MaterialTheme.colorScheme.error)
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = {
                    keyboard?.hide()
                    vm.connect(username)
                }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Purple,
                    unfocusedBorderColor = CardBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = Purple,
                    focusedLabelColor = Purple,
                    unfocusedLabelColor = TextSecondary,
                ),
            )

            Spacer(Modifier.weight(1f))
        }

        // Bottom section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .navigationBarsPadding()
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(
                onClick = { keyboard?.hide(); vm.connect(username) },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Purple),
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(
                        text = "Conectar con Last.fm",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Row {
                Text(
                    text = "¿No tienes cuenta? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
                Text(
                    text = "Crear cuenta en Last.fm ↗",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Purple,
                    modifier = Modifier.clickable {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse("https://www.last.fm/join"))
                        )
                    },
                )
            }

            Spacer(Modifier.height(20.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(text = "🔒", fontSize = 14.sp)
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Solo leemos tus scrobbles. Nunca publicamos nada.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
