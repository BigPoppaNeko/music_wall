package com.jfcardenas.musicwall.ui.screens

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.BorderStroke
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.*
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.jfcardenas.musicwall.ui.model.CURATED_WALLS
import com.jfcardenas.musicwall.ui.model.STYLE_OPTIONS
import com.jfcardenas.musicwall.ui.model.StyleOption
import com.jfcardenas.musicwall.ui.model.WallItem
import com.jfcardenas.musicwall.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MuralesScreen(
    initialTab: Int = 0,
    onBack: (() -> Unit)? = null,
    onSelectRenderer: (styleId: String) -> Unit,
) {
    var selectedTab      by remember { mutableStateOf(initialTab) }
    var selectedRenderer by remember { mutableStateOf(STYLE_OPTIONS.first()) }
    var previewWall      by remember { mutableStateOf<WallItem?>(null) }
    var showPeriodSheet  by remember { mutableStateOf(false) }

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
                    text       = "Murales",
                    style      = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text  = "Elige tu fondo.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }
        }

        // Pill tabs
        Row(
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Card)
                .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                .padding(4.dp),
        ) {
            listOf("Murales", "Lab").forEachIndexed { index, label ->
                val isSelected = selectedTab == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) Purple else Color.Transparent)
                        .clickable { selectedTab = index }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text       = label,
                        fontSize   = 13.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color      = if (isSelected) Color.White else TextSecondary,
                    )
                }
            }
        }

        // Content
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                0 -> CuratedWallsPage(onWallClick = { previewWall = it })
                1 -> LabPage(
                    selectedRenderer = selectedRenderer,
                    onSelectRenderer = { selectedRenderer = it },
                    onGenerate       = { showPeriodSheet = true },
                )
            }
        }
    }

    if (showPeriodSheet) {
        PeriodSelectionSheet(
            onDismiss = { showPeriodSheet = false },
            onConfirm = {
                showPeriodSheet = false
                onSelectRenderer(selectedRenderer.id)
            },
        )
    }

    previewWall?.let { wall ->
        WallPreviewSheet(wall = wall, onDismiss = { previewWall = null })
    }
}

// ── Walls tab ──────────────────────────────────────────────────────────────────

@Composable
private fun CuratedWallsPage(onWallClick: (WallItem) -> Unit) {
    if (CURATED_WALLS.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("✦", fontSize = 36.sp, color = TextMuted)
                Spacer(Modifier.height(12.dp))
                Text("Walls en camino", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                Spacer(Modifier.height(4.dp))
                Text(
                    text      = "Los primeros fondos estarán aquí pronto.",
                    fontSize  = 12.sp,
                    color     = TextMuted,
                    textAlign = TextAlign.Center,
                    modifier  = Modifier.padding(horizontal = 40.dp),
                )
            }
        }
        return
    }

    LazyVerticalGrid(
        columns               = GridCells.Fixed(2),
        modifier              = Modifier.fillMaxSize(),
        contentPadding        = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement   = Arrangement.spacedBy(12.dp),
    ) {
        items(CURATED_WALLS, key = { it.id }) { wall ->
            CuratedWallCard(wall = wall, onClick = { onWallClick(wall) })
        }
    }
}

@Composable
private fun CuratedWallCard(wall: WallItem, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(9f / 16f)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
    ) {
        if (wall.imageUrl != null) {
            AsyncImage(
                model              = wall.imageUrl,
                contentDescription = wall.title,
                contentScale       = ContentScale.Crop,
                modifier           = Modifier.fillMaxSize(),
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize().background(
                    Brush.verticalGradient(listOf(wall.gradientStart, wall.gradientEnd))
                )
            )
        }
        // Bottom scrim + labels
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.45f)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)))
                )
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp),
        ) {
            Text(
                text       = wall.title,
                fontSize   = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color      = Color.White,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis,
            )
            if (wall.description.isNotEmpty()) {
                Text(
                    text     = wall.description,
                    fontSize = 10.sp,
                    color    = Color.White.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

// ── Lab tab ────────────────────────────────────────────────────────────────────

@Composable
private fun LabPage(
    selectedRenderer: StyleOption,
    onSelectRenderer: (StyleOption) -> Unit,
    onGenerate: () -> Unit,
) {
    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding      = PaddingValues(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Column(modifier = Modifier.padding(bottom = 2.dp)) {
                    Text(
                        text       = "Experimentos",
                        fontSize   = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = TextPrimary,
                    )
                    Text(
                        text     = "Renderers generativos con tu música.",
                        fontSize = 12.sp,
                        color    = TextSecondary,
                    )
                }
            }
            items(STYLE_OPTIONS, key = { it.id }) { style ->
                RendererCard(
                    style    = style,
                    selected = style.id == selectedRenderer.id,
                    onClick  = { onSelectRenderer(style) },
                )
            }
        }
        // Floating CTA with gradient fade
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(listOf(Color.Transparent, Background.copy(alpha = 0.96f), Background))
                )
                .padding(horizontal = 24.dp, vertical = 20.dp),
        ) {
            Button(
                onClick  = onGenerate,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape  = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Purple),
            ) {
                Text(
                    text       = "Generar mural",
                    fontSize   = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = Color.White,
                )
            }
        }
    }
}

@Composable
private fun RendererCard(
    style: StyleOption,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(20.dp))
            .border(
                width = if (selected) 2.dp else 0.dp,
                color = if (selected) Purple else Color.Transparent,
                shape = RoundedCornerShape(20.dp),
            )
            .clickable { onClick() },
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(listOf(style.gradientStart, style.gradientEnd)))
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.radialGradient(listOf(Color.White.copy(alpha = 0.06f), Color.Transparent)))
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.5f)
                .align(Alignment.BottomCenter)
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f))))
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(18.dp),
        ) {
            Text(
                text       = style.name,
                fontSize   = 18.sp,
                fontWeight = FontWeight.Bold,
                color      = Color.White,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text      = style.tagline.replace("\n", " · "),
                fontSize  = 12.sp,
                fontStyle = FontStyle.Italic,
                color     = Color.White.copy(alpha = 0.7f),
            )
        }
        AnimatedVisibility(
            visible  = selected,
            enter    = fadeIn(tween(200)) + scaleIn(tween(200)),
            exit     = fadeOut(tween(150)) + scaleOut(tween(150)),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp),
        ) {
            Box(
                modifier         = Modifier
                    .size(32.dp)
                    .background(Purple, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Seleccionado",
                    tint     = Color.White,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

// ── Wall preview sheet ─────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WallPreviewSheet(wall: WallItem, onDismiss: () -> Unit) {
    val context    = LocalContext.current
    val scope      = rememberCoroutineScope()
    var applying   by remember { mutableStateOf(false) }
    var applyError by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor   = Color(0xFF1A1A1A),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .navigationBarsPadding(),
        ) {
            // Preview image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Surface),
            ) {
                if (wall.imageUrl != null) {
                    AsyncImage(
                        model              = wall.imageUrl,
                        contentDescription = wall.title,
                        contentScale       = ContentScale.Crop,
                        modifier           = Modifier.fillMaxSize(),
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize().background(
                            Brush.verticalGradient(listOf(wall.gradientStart, wall.gradientEnd))
                        ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("Próximamente", fontSize = 14.sp, color = TextMuted)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Text(wall.title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            if (wall.description.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text(wall.description, fontSize = 13.sp, color = TextSecondary)
            }
            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    val url = wall.imageUrl ?: return@Button
                    scope.launch {
                        applying   = true
                        applyError = false
                        try {
                            applyWallpaper(context, url)
                            onDismiss()
                        } catch (e: Exception) {
                            applyError = true
                        } finally {
                            applying = false
                        }
                    }
                },
                enabled  = wall.imageUrl != null && !applying,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape  = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Purple),
            ) {
                if (applying) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(20.dp),
                        color       = Color.White,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(
                        text       = if (wall.imageUrl == null) "Próximamente" else "Aplicar fondo",
                        fontWeight = FontWeight.SemiBold,
                        color      = Color.White,
                    )
                }
            }

            if (applyError) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text      = "No se pudo aplicar el fondo. Revisa tu conexión.",
                    fontSize  = 12.sp,
                    color     = Color(0xFFFF6B6B),
                    textAlign = TextAlign.Center,
                    modifier  = Modifier.fillMaxWidth(),
                )
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

// ── Period selection ──────────────────────────────────────────────────────────

private data class PeriodOption(val id: String, val label: String, val description: String)

private val PERIOD_OPTIONS = listOf(
    PeriodOption("7day",    "Semanal",    "Lo que sonó esta semana"),
    PeriodOption("1month",  "Mensual",    "Lo que sonó este mes"),
    PeriodOption("1day",    "Diario",     "Las últimas 24 horas"),
    PeriodOption("overall", "Histórico",  "Tus favoritos de siempre"),
    PeriodOption("random",  "Aleatorio",  "Portadas al azar de todo tu archivo histórico"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PeriodSelectionSheet(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    val context = LocalContext.current
    val prefs   = remember { context.getSharedPreferences("music_wall_prefs", Context.MODE_PRIVATE) }
    var selected by remember { mutableStateOf(prefs.getString("period", "7day") ?: "7day") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor   = Color(0xFF1A1A1A),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .navigationBarsPadding(),
        ) {
            Text(
                text       = "¿Qué música quieres usar?",
                fontSize   = 18.sp,
                fontWeight = FontWeight.Bold,
                color      = Color.White,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text     = "Elige el período de tu historial de Last.fm",
                fontSize = 13.sp,
                color    = Color.White.copy(alpha = 0.5f),
            )
            Spacer(Modifier.height(20.dp))

            PERIOD_OPTIONS.forEach { option ->
                val isSelected = option.id == selected
                Surface(
                    onClick        = { selected = option.id },
                    modifier       = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape          = RoundedCornerShape(14.dp),
                    color          = if (isSelected) Color(0xFF2A1A4A) else Color(0xFF242424),
                    border         = if (isSelected) BorderStroke(1.dp, Purple) else null,
                ) {
                    Row(
                        modifier            = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment   = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text       = option.label,
                                fontSize   = 15.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color      = Color.White,
                            )
                            Text(
                                text     = option.description,
                                fontSize = 12.sp,
                                color    = Color.White.copy(alpha = 0.5f),
                            )
                        }
                        if (isSelected) {
                            Box(
                                modifier         = Modifier
                                    .size(22.dp)
                                    .background(Purple, RoundedCornerShape(11.dp)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint     = Color.White,
                                    modifier = Modifier.size(13.dp),
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    prefs.edit().putString("period", selected).apply()
                    onConfirm()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape  = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Purple),
            ) {
                Text(
                    text       = "Generar mural",
                    fontSize   = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = Color.White,
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

private suspend fun applyWallpaper(context: Context, url: String) = withContext(Dispatchers.IO) {
    val imageLoader = ImageLoader(context)
    val request = ImageRequest.Builder(context)
        .data(url)
        .allowHardware(false)
        .build()
    val drawable = (imageLoader.execute(request) as SuccessResult).drawable
    val bitmap: Bitmap = if (drawable is BitmapDrawable) {
        drawable.bitmap
    } else {
        Bitmap.createBitmap(
            drawable.intrinsicWidth.coerceAtLeast(1),
            drawable.intrinsicHeight.coerceAtLeast(1),
            Bitmap.Config.ARGB_8888,
        ).also { bmp ->
            val canvas = Canvas(bmp)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
        }
    }
    WallpaperManager.getInstance(context).setBitmap(bitmap)
}
