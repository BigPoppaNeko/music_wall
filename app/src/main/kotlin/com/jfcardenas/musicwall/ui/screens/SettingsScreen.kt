package com.jfcardenas.musicwall.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.jfcardenas.musicwall.BuildConfig
import com.jfcardenas.musicwall.service.CollageWallpaper
import com.jfcardenas.musicwall.ui.theme.*
import com.jfcardenas.musicwall.ui.viewmodel.HomeViewModel

@Composable
fun SettingsScreen(homeViewModel: HomeViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(CollageWallpaper.PREFS_NAME, Context.MODE_PRIVATE) }
    var username by remember { mutableStateOf(prefs.getString(CollageWallpaper.PREF_USERNAME, "") ?: "") }
    var autoChange by remember { mutableStateOf(true) }
    val avatarUrl = homeViewModel.uiState.avatarUrl

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding(),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Text(
                text = "Ajustes",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(vertical = 8.dp),
            )
        }

        item { SectionLabel("GENERAL") }

        item {
            SettingsCard {
                SettingsRow(
                    title = "Calidad del wallpaper",
                    trailing = {
                        Text("Alta", fontSize = 14.sp, color = Purple, fontWeight = FontWeight.Medium)
                    },
                )
                HorizontalDivider(color = CardBorder, thickness = 0.5.dp)
                SettingsRow(
                    title = "Cambiar automáticamente",
                    subtitle = "Cada 24 horas",
                    trailing = {
                        Switch(
                            checked = autoChange,
                            onCheckedChange = { autoChange = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Purple),
                        )
                    },
                )
            }
        }

        item { Spacer(Modifier.height(4.dp)) }
        item { SectionLabel("CUENTA") }

        item {
            SettingsCard {
                if (username.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Purple.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (avatarUrl != null) {
                                AsyncImage(
                                    model = avatarUrl,
                                    contentDescription = "Avatar de $username",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                                )
                            } else {
                                Text(
                                    text = username.take(1).uppercase(),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Purple,
                                )
                            }
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(username, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Text("last.fm/$username", fontSize = 12.sp, color = TextSecondary)
                        }
                        TextButton(
                            onClick = {
                                prefs.edit().remove(CollageWallpaper.PREF_USERNAME).apply()
                                username = ""
                                restartToOnboarding(context)
                            },
                        ) {
                            Text("Desconectar", color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                        }
                    }
                } else {
                    SettingsRow(
                        title = "Sin cuenta conectada",
                        subtitle = "Ve a Fuente para conectar",
                        trailing = {},
                    )
                }
            }
        }

        item { Spacer(Modifier.height(4.dp)) }
        item { SectionLabel("INFORMACIÓN") }

        item {
            SettingsCard {
                SettingsRow(
                    title = "Wallpapers creados",
                    trailing = { Text("—", fontSize = 14.sp, color = TextSecondary) },
                )
                HorizontalDivider(color = CardBorder, thickness = 0.5.dp)
                SettingsRow(
                    title = "Versión",
                    trailing = { Text("${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})", fontSize = 14.sp, color = TextSecondary) },
                )
                HorizontalDivider(color = CardBorder, thickness = 0.5.dp)
                SettingsRow(
                    title = "Privacidad",
                    trailing = { Text("›", fontSize = 20.sp, color = TextSecondary) },
                    onClick = {},
                )
                HorizontalDivider(color = CardBorder, thickness = 0.5.dp)
                SettingsRow(
                    title = "Acerca de MusicWall",
                    trailing = { Text("›", fontSize = 20.sp, color = TextSecondary) },
                    onClick = {},
                )
            }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.5.sp,
        color = TextMuted,
        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp),
    )
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Card)
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp)),
        content = content,
    )
}

@Composable
private fun SettingsRow(
    title: String,
    subtitle: String? = null,
    trailing: @Composable () -> Unit,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 15.sp, color = TextPrimary)
            if (subtitle != null) {
                Text(text = subtitle, fontSize = 12.sp, color = TextSecondary)
            }
        }
        trailing()
    }
}

private fun restartToOnboarding(context: Context) {
    val pm = context.packageManager
    val intent = pm.getLaunchIntentForPackage(context.packageName)?.apply {
        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
    } ?: return
    context.startActivity(intent)
}
