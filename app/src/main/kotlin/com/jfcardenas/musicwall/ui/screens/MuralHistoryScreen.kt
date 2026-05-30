package com.jfcardenas.musicwall.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.jfcardenas.musicwall.data.local.db.entity.MuralRecord
import com.jfcardenas.musicwall.ui.theme.*
import com.jfcardenas.musicwall.ui.viewmodel.MuralHistoryViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MuralHistoryScreen(
    onBack: () -> Unit,
    onApplyMural: (styleId: String) -> Unit,
    vm: MuralHistoryViewModel = hiltViewModel(),
) {
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
            Column {
                Text(
                    text = "Historial de murales",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "${vm.murals.size} murales creados",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }
        }

        when {
            vm.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Purple)
                }
            }
            vm.murals.isEmpty() -> {
                EmptyHistoryState()
            }
            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(vm.murals, key = { it.id }) { mural ->
                        MuralCell(
                            mural = mural,
                            onDelete = { vm.delete(mural) },
                            onApply = { onApplyMural(mural.styleId) },
                        )
                    }
                    item { Spacer(Modifier.height(8.dp)) }
                    item { Spacer(Modifier.height(8.dp)) }
                }
            }
        }
    }
}

@Composable
private fun MuralCell(
    mural: MuralRecord,
    onDelete: () -> Unit,
    onApply: () -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }
    val dateStr = remember(mural.createdAt) {
        SimpleDateFormat("d MMM yyyy", Locale("es")).format(Date(mural.createdAt))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.6f)
            .clip(RoundedCornerShape(16.dp))
            .background(Card)
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onApply() },
                    onLongPress = { showMenu = true },
                )
            },
    ) {
        val file = File(mural.filePath)
        if (file.exists()) {
            AsyncImage(
                model = file,
                contentDescription = "Mural",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize().background(
                    Brush.verticalGradient(listOf(Color(0xFF2D1B69), Color(0xFF1A0A2E)))
                ),
                contentAlignment = Alignment.Center,
            ) {
                Text("MW", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Purple)
            }
        }

        // Bottom overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.4f)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)))
                ),
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(10.dp),
        ) {
            Text(
                text = styleLabelFor(mural.styleId),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
            )
            Text(text = dateStr, fontSize = 10.sp, color = Color.White.copy(alpha = 0.65f))
        }

        // Context menu
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
        ) {
            DropdownMenuItem(
                text = { Text("Aplicar de nuevo") },
                onClick = { showMenu = false; onApply() },
            )
            DropdownMenuItem(
                text = { Text("Eliminar", color = MaterialTheme.colorScheme.error) },
                leadingIcon = {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                },
                onClick = { showMenu = false; onDelete() },
            )
        }
    }
}

@Composable
private fun EmptyHistoryState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🖼", fontSize = 48.sp)
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Aún no has creado murales",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Ve a Estilos y genera tu primer mural.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center,
            )
        }
    }
}

private fun styleLabelFor(styleId: String) = when (styleId) {
    "street"        -> "Street Poster"
    "constellation" -> "Constellation"
    "museum"        -> "Museum"
    else            -> "Dreamscape"
}
