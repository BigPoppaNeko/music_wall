package com.jfcardenas.musicwall.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.jfcardenas.musicwall.service.CollageWallpaper
import com.jfcardenas.musicwall.ui.theme.*
import com.jfcardenas.musicwall.ui.viewmodel.ExploreViewModel
import kotlin.math.abs

private data class StaticItem(val name: String, val subtitle: String, val bgColor: Color)

private val RECENT_ARTISTS = listOf(
    StaticItem("Radiohead",  "", Color(0xFF2D4A6B)),
    StaticItem("Pink Floyd", "", Color(0xFF5C1A6B)),
    StaticItem("Nujabes",    "", Color(0xFF1A5C3A)),
    StaticItem("Cerati",     "", Color(0xFF6B3A1A)),
)

private val SUGGESTED_ARTISTS = listOf(
    StaticItem("Tool",              "Progressive Metal", Color(0xFF3A3A3A)),
    StaticItem("Gustavo Cerati",    "Rock",              Color(0xFF3A1A1A)),
    StaticItem("King Crimson",      "Art Rock",          Color(0xFF1A1A3A)),
    StaticItem("Radiohead",         "Alternative",       Color(0xFF2D4A6B)),
    StaticItem("Pink Floyd",        "Progressive Rock",  Color(0xFF5C1A6B)),
    StaticItem("Portishead",        "Trip Hop",          Color(0xFF1A3A3A)),
    StaticItem("Massive Attack",    "Trip Hop",          Color(0xFF2A1A3A)),
    StaticItem("The Notorious B.I.G.", "Hip Hop",        Color(0xFF3A2A1A)),
    StaticItem("Yma Sumac",         "Voz exótica",       Color(0xFF6B3A1A)),
    StaticItem("Nick Cave",         "Art Rock",          Color(0xFF3A2A1A)),
    StaticItem("Nujabes",           "Hip Hop Jazz",      Color(0xFF1A5C3A)),
    StaticItem("Boards of Canada",  "Electronic",        Color(0xFF1A3A1A)),
)

private val SUGGESTED_ALBUMS = listOf(
    StaticItem("OK Computer",  "Radiohead · 1997",   Color(0xFF2D4A6B)),
    StaticItem("The Wall",     "Pink Floyd · 1979",  Color(0xFF5C1A6B)),
    StaticItem("In Rainbows",  "Radiohead · 2007",   Color(0xFF3A1A5C)),
    StaticItem("Lateralus",    "Tool · 2001",        Color(0xFF3A3A3A)),
    StaticItem("Dummy",        "Portishead · 1994",  Color(0xFF1A3A3A)),
)

private val SUGGESTED_GENRES = listOf(
    StaticItem("Progressive Rock", "", Color(0xFF2D4A6B)),
    StaticItem("Trip Hop",         "", Color(0xFF3A1A3A)),
    StaticItem("Jazz",             "", Color(0xFF3A2A1A)),
    StaticItem("Electronic",       "", Color(0xFF1A3A1A)),
    StaticItem("Metal",            "", Color(0xFF3A1A1A)),
    StaticItem("Ambient",          "", Color(0xFF1A2A3A)),
)

private val TABS = listOf("Artistas", "Álbumes", "Géneros")

private fun colorFromName(name: String): Color {
    val h = abs(name.hashCode())
    val palettes = listOf(
        Color(0xFF2D4A6B), Color(0xFF5C1A6B), Color(0xFF1A5C3A), Color(0xFF6B3A1A),
        Color(0xFF3A3A3A), Color(0xFF1A1A3A), Color(0xFF1A3A3A), Color(0xFF2A1A3A),
    )
    return palettes[h % palettes.size]
}

@Composable
fun ExploreSourceScreen(
    onBack: () -> Unit,
    onContinue: () -> Unit,
    viewModel: ExploreViewModel = hiltViewModel(),
) {
    var selectedTab by remember { mutableStateOf(0) }
    var search by remember { mutableStateOf("") }
    val selected = remember { mutableStateListOf<String>() }
    val context = LocalContext.current

    LaunchedEffect(search, selectedTab) {
        viewModel.search(search, selectedTab)
    }

    val staticSuggestions = when (selectedTab) {
        0 -> SUGGESTED_ARTISTS
        1 -> SUGGESTED_ALBUMS
        else -> SUGGESTED_GENRES
    }.filter { search.isBlank() || it.name.contains(search, ignoreCase = true) }

    val searchState = if (selectedTab == 0) viewModel.artistState else viewModel.albumState

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
            Column {
                Text("Explorar", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Text("Elige lo que más te representa.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
        }

        TabRow(selectedTabIndex = selectedTab, containerColor = Background, contentColor = Purple) {
            TABS.forEachIndexed { i, label ->
                Tab(
                    selected = selectedTab == i,
                    onClick  = { selectedTab = i; search = ""; selected.clear() },
                    text = {
                        Text(
                            text       = label,
                            color      = if (selectedTab == i) Purple else TextSecondary,
                            fontWeight = if (selectedTab == i) FontWeight.SemiBold else FontWeight.Normal,
                        )
                    },
                )
            }
        }

        OutlinedTextField(
            value         = search,
            onValueChange = { search = it },
            modifier      = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            placeholder   = { Text("Buscar ${TABS[selectedTab].lowercase()}...", fontSize = 14.sp) },
            leadingIcon   = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
            singleLine    = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = Purple,
                unfocusedBorderColor = CardBorder,
                focusedTextColor     = TextPrimary,
                unfocusedTextColor   = TextPrimary,
                cursorColor          = Purple,
            ),
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            // Favoritos recientes — solo en Artistas cuando no hay búsqueda
            if (selectedTab == 0 && search.isBlank()) {
                item {
                    Text(
                        text = "Tus favoritos recientes",
                        fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary,
                        modifier = Modifier.padding(bottom = 12.dp),
                    )
                }
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(bottom = 20.dp),
                    ) {
                        items(RECENT_ARTISTS) { artist ->
                            RecentAvatar(name = artist.name, bgColor = artist.bgColor)
                        }
                    }
                }
            }

            // Resultados de búsqueda real (artistas y álbumes)
            if (search.isNotBlank() && selectedTab < 2) {
                when (searchState) {
                    is ExploreViewModel.SearchState.Loading -> {
                        item {
                            Box(Modifier.fillMaxWidth().padding(vertical = 32.dp), Alignment.Center) {
                                CircularProgressIndicator(color = Purple, modifier = Modifier.size(24.dp))
                            }
                        }
                    }
                    is ExploreViewModel.SearchState.Results -> {
                        item {
                            Text(
                                "Resultados para \"$search\"",
                                fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary,
                                modifier = Modifier.padding(bottom = 4.dp),
                            )
                        }
                        items(searchState.items) { result ->
                            ApiResultRow(
                                result     = result,
                                isSelected = result.name in selected,
                                onToggle   = {
                                    if (result.name in selected) selected.remove(result.name)
                                    else selected.add(result.name)
                                },
                            )
                        }
                    }
                    is ExploreViewModel.SearchState.Empty -> {
                        item {
                            Box(Modifier.fillMaxWidth().padding(vertical = 32.dp), Alignment.Center) {
                                Text("Sin resultados para \"$search\"", fontSize = 14.sp, color = TextSecondary)
                            }
                        }
                    }
                    is ExploreViewModel.SearchState.Error -> {
                        item {
                            Box(Modifier.fillMaxWidth().padding(vertical = 32.dp), Alignment.Center) {
                                Text("No se pudo buscar. Revisa tu conexión.", fontSize = 14.sp, color = TextSecondary)
                            }
                        }
                    }
                    else -> {}
                }
            } else {
                // Sugerencias estáticas
                item {
                    Text(
                        text = "Sugerencias para ti",
                        fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary,
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                }
                items(staticSuggestions) { item ->
                    StaticSuggestionRow(
                        item       = item,
                        isSelected = item.name in selected,
                        onToggle   = {
                            if (item.name in selected) selected.remove(item.name)
                            else selected.add(item.name)
                        },
                    )
                }
            }

            item { Spacer(Modifier.height(8.dp)) }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .navigationBarsPadding()
                .padding(bottom = 24.dp),
        ) {
            Button(
                onClick  = {
                    saveExploreArtists(context, selected)
                    onContinue()
                },
                enabled  = selectedTab == 0 && selected.isNotEmpty(),
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape    = RoundedCornerShape(28.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = Purple),
            ) {
                Text(
                    text       = when {
                        selectedTab != 0 -> "Elige artistas para continuar"
                        selected.isEmpty() -> "Continuar"
                        else -> "Continuar (${selected.size} seleccionados)"
                    },
                    fontSize   = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = Color.White,
                )
            }
        }
    }
}

private fun saveExploreArtists(context: Context, artists: Collection<String>) {
    context.getSharedPreferences(CollageWallpaper.PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putString(CollageWallpaper.PREF_SOURCE, CollageWallpaper.PREF_SOURCE_EXPLORE_ARTISTS)
        .putString(CollageWallpaper.PREF_IMAGE_KIND, "ALBUMS")
        .putString(CollageWallpaper.PREF_PERIOD, "random")
        .putStringSet(CollageWallpaper.PREF_EXPLORE_ARTISTS, artists.map { it.trim() }.filter { it.isNotEmpty() }.toSet())
        .apply()
}

@Composable
private fun RecentAvatar(name: String, bgColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(64.dp)) {
        Box(
            modifier = Modifier.size(56.dp).background(bgColor, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(name.take(1), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
        Spacer(Modifier.height(4.dp))
        Text(name, fontSize = 11.sp, color = TextSecondary, maxLines = 1)
    }
}

@Composable
private fun ApiResultRow(
    result: ExploreViewModel.SearchResult,
    isSelected: Boolean,
    onToggle: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape).background(colorFromName(result.name)),
            contentAlignment = Alignment.Center,
        ) {
            if (result.imageUrl != null) {
                AsyncImage(
                    model = result.imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                )
            } else {
                Text(result.name.take(1), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(result.name, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
            if (result.subtitle.isNotEmpty()) {
                Text(result.subtitle, fontSize = 12.sp, color = TextSecondary)
            }
        }

        Box(
            modifier = Modifier
                .size(32.dp).clip(CircleShape)
                .background(if (isSelected) Purple else CardBorder)
                .clickable { onToggle() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (isSelected) Icons.Default.Check else Icons.Default.Add,
                contentDescription = null,
                tint = if (isSelected) Color.White else TextSecondary,
                modifier = Modifier.size(16.dp),
            )
        }
    }
    HorizontalDivider(color = CardBorder, thickness = 0.5.dp)
}

@Composable
private fun StaticSuggestionRow(item: StaticItem, isSelected: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(40.dp).background(item.bgColor, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(item.name.take(1), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(item.name, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
            if (item.subtitle.isNotEmpty()) Text(item.subtitle, fontSize = 12.sp, color = TextSecondary)
        }
        Box(
            modifier = Modifier
                .size(32.dp).clip(CircleShape)
                .background(if (isSelected) Purple else CardBorder)
                .clickable { onToggle() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (isSelected) Icons.Default.Check else Icons.Default.Add,
                contentDescription = null,
                tint = if (isSelected) Color.White else TextSecondary,
                modifier = Modifier.size(16.dp),
            )
        }
    }
    HorizontalDivider(color = CardBorder, thickness = 0.5.dp)
}
