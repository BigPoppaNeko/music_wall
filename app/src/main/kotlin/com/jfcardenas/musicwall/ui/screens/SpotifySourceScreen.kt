package com.jfcardenas.musicwall.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jfcardenas.musicwall.ui.theme.*
import com.jfcardenas.musicwall.ui.viewmodel.SpotifySourceViewModel

@Composable
fun SpotifySourceScreen(
    onBack: () -> Unit,
    onPlaylistSelected: () -> Unit,
    vm: SpotifySourceViewModel = hiltViewModel(),
) {
    var url by remember { mutableStateOf("") }
    val keyboard = LocalSoftwareKeyboardController.current

    val isLoading = vm.state is SpotifySourceViewModel.State.Loading
    val preview = vm.state as? SpotifySourceViewModel.State.Preview
    val error = (vm.state as? SpotifySourceViewModel.State.Error)?.message

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
                text = "Spotify",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 24.dp),
        ) {
            Spacer(Modifier.height(16.dp))

            Text(
                text = "Pega el enlace de una playlist pública de Spotify.",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
            )

            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = url,
                onValueChange = {
                    url = it
                    if (vm.state is SpotifySourceViewModel.State.Error) vm.clearError()
                    if (vm.state is SpotifySourceViewModel.State.Preview) vm.reset()
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("https://open.spotify.com/playlist/...", fontSize = 13.sp) },
                singleLine = true,
                isError = error != null,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = {
                    keyboard?.hide()
                    vm.fetchPreview(url)
                }),
                trailingIcon = {
                    if (url.isNotEmpty()) {
                        IconButton(onClick = { url = ""; vm.reset() }) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpiar", tint = TextSecondary)
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Purple,
                    unfocusedBorderColor = CardBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = Purple,
                ),
            )

            if (error != null) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Spacer(Modifier.height(16.dp))

            // Preview card
            AnimatedVisibility(
                visible = preview != null || isLoading,
                enter = fadeIn(tween(300)) + expandVertically(),
                exit = fadeOut(tween(200)) + shrinkVertically(),
            ) {
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Card),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Purple, strokeWidth = 2.dp)
                    }
                } else if (preview != null) {
                    PlaylistPreviewCard(title = preview.title, author = preview.author)
                }
            }
        }

        // Bottom buttons
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .navigationBarsPadding()
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (preview == null && !isLoading && url.isNotEmpty()) {
                OutlinedButton(
                    onClick = { keyboard?.hide(); vm.fetchPreview(url) },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Purple),
                ) {
                    Text("Ver vista previa", color = Purple, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(12.dp))
            }

            Button(
                onClick = onPlaylistSelected,
                enabled = preview != null,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Purple),
            ) {
                Text(
                    text = "Usar esta playlist",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                )
            }

            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = { /* Sprint 5: OAuth */ },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1DB954)),
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(Color(0xFF1DB954), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("♫", fontSize = 11.sp, color = Color.Black)
                }
                Spacer(Modifier.width(8.dp))
                Text("Conectar cuenta de Spotify", color = Color(0xFF1DB954), fontWeight = FontWeight.Medium)
            }

            Spacer(Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(text = "🔒", fontSize = 14.sp)
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Solo accedemos a la información pública de la playlist.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun PlaylistPreviewCard(title: String, author: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Card)
            .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Playlist cover placeholder
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF1DB954)),
            contentAlignment = Alignment.Center,
        ) {
            Text("♫", fontSize = 24.sp, color = Color.Black)
        }

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                maxLines = 1,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "por $author",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                maxLines = 1,
            )
            Spacer(Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(CardBorder)
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            ) {
                Text(text = "Pública", fontSize = 11.sp, color = TextSecondary)
            }
        }
    }
}
