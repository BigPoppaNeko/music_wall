package com.jfcardenas.musicwall.ui.navigation

import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.jfcardenas.musicwall.ui.components.SampleBackground
import com.jfcardenas.musicwall.ui.screens.*
import com.jfcardenas.musicwall.ui.theme.*

private data class TabItem(val route: String, val label: String, val icon: ImageVector)

private val TABS = listOf(
    TabItem("inicio",   "Inicio",   Icons.Filled.Home),
    TabItem("murales",  "Murales",  Icons.Filled.GridView),
    TabItem("fuente",   "Fuente",   Icons.Filled.MusicNote),
    TabItem("ajustes",  "Ajustes",  Icons.Filled.Settings),
)

private val TAB_ROUTES = TABS.map { it.route }.toSet()

@Composable
fun MainNav() {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    androidx.compose.foundation.layout.Box(Modifier.fillMaxSize()) {
        SampleBackground(route = currentRoute)
    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            // Strip both path "/" and query "?" separators to get base route
            val baseRoute = currentRoute?.substringBefore("/")?.substringBefore("?")
            if (baseRoute in TAB_ROUTES) {
                NavigationBar(containerColor = Surface) {
                    TABS.forEach { tab ->
                        NavigationBarItem(
                            selected = baseRoute == tab.route,
                            onClick  = {
                                nav.navigate(tab.route) {
                                    popUpTo("inicio") { saveState = true }
                                    launchSingleTop = true
                                    restoreState    = true
                                }
                            },
                            icon  = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label, fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor   = Purple,
                                selectedTextColor   = Purple,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary,
                                indicatorColor      = Color.Transparent,
                            ),
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController    = nav,
            startDestination = "inicio",
            modifier         = Modifier.padding(innerPadding),
            enterTransition  = { slideInHorizontally(tween(260)) { it / 6 } + fadeIn(tween(260)) },
            exitTransition   = { slideOutHorizontally(tween(260)) { -it / 6 } + fadeOut(tween(180)) },
            popEnterTransition  = { slideInHorizontally(tween(260)) { -it / 6 } + fadeIn(tween(260)) },
            popExitTransition   = { slideOutHorizontally(tween(260)) { it / 6 } + fadeOut(tween(180)) },
        ) {
            // ── Tabs ──────────────────────────────────────────────────────────
            composable("inicio") {
                HomeScreen(
                    onGoToEstilos   = {
                        nav.navigate("murales?tab=1") {
                            popUpTo("inicio") { saveState = true }
                            launchSingleTop = false
                            restoreState    = false
                        }
                    },
                    onGoToHistorial   = { nav.navigate("historial") },
                    onGoToArtistas    = { nav.navigate("artistas") },
                    onGoToFavoritas   = { nav.navigate("favoritas") },
                    onNowPlayingClick = { artist, track ->
                        nav.navigate("nowplaying/${Uri.encode(artist)}/${Uri.encode(track)}")
                    },
                )
            }

            composable(
                route = "murales?tab={tab}",
                arguments = listOf(
                    navArgument("tab") { type = NavType.IntType; defaultValue = 0 }
                ),
            ) { back ->
                val tab = back.arguments?.getInt("tab") ?: 0
                MuralesScreen(
                    initialTab       = tab,
                    onBack           = null,
                    onSelectRenderer = { styleId -> nav.navigate("generating/$styleId") },
                )
            }

            composable("fuente") {
                SourceSelectionScreen(
                    onBack           = null,
                    onSourceSelected = { source ->
                        when (source) {
                            MusicSource.LASTFM  -> nav.navigate("source/lastfm")
                            MusicSource.SPOTIFY -> nav.navigate("source/spotify")
                            MusicSource.EXPLORE -> nav.navigate("source/explore")
                        }
                    },
                )
            }

            composable("ajustes") { SettingsScreen() }

            // ── Source detail (from Fuente tab) ───────────────────────────────
            composable("source/lastfm") {
                LastFmSourceScreen(
                    onBack      = { nav.popBackStack() },
                    onConnected = { nav.navigate("fuente") { popUpTo("fuente") { inclusive = true } } },
                )
            }

            composable("source/spotify") {
                SpotifySourceScreen(
                    onBack             = { nav.popBackStack() },
                    onPlaylistSelected = { nav.navigate("fuente") { popUpTo("fuente") { inclusive = true } } },
                )
            }

            composable("source/explore") {
                ExploreSourceScreen(
                    onBack     = { nav.popBackStack() },
                    onContinue = { nav.navigate("fuente") { popUpTo("fuente") { inclusive = true } } },
                )
            }

            // ── Historial ─────────────────────────────────────────────────────
            composable("historial") {
                MuralHistoryScreen(
                    onBack       = { nav.popBackStack() },
                    onApplyMural = { styleId -> nav.navigate("generating/$styleId") },
                )
            }

            // ── Artistas ──────────────────────────────────────────────────────
            composable("artistas") {
                ArtistasScreen(onBack = { nav.popBackStack() })
            }

            // ── Mis portadas favoritas ─────────────────────────────────────────
            composable("favoritas") {
                FavoritesScreen(onBack = { nav.popBackStack() })
            }

            // ── Now Playing Detail ─────────────────────────────────────────────
            composable(
                route = "nowplaying/{artist}/{track}",
                arguments = listOf(
                    navArgument("artist") { type = NavType.StringType },
                    navArgument("track")  { type = NavType.StringType },
                ),
            ) { back ->
                val artist    = back.arguments?.getString("artist") ?: ""
                val trackName = back.arguments?.getString("track")  ?: ""
                NowPlayingDetailScreen(
                    artist    = artist,
                    trackName = trackName,
                    onBack    = { nav.popBackStack() },
                )
            }

            // ── Generating flow ────────────────────────────────────────────────
            composable(
                route     = "generating/{styleId}",
                arguments = listOf(navArgument("styleId") { type = NavType.StringType }),
            ) { back ->
                val styleId = back.arguments?.getString("styleId") ?: "ecosystem"
                GeneratingScreen(
                    styleId    = styleId,
                    onComplete = {
                        nav.navigate("preview/$styleId") {
                            popUpTo("generating/$styleId") { inclusive = true }
                        }
                    },
                    onCancel   = { nav.popBackStack() },
                )
            }

            composable(
                route     = "preview/{styleId}",
                arguments = listOf(navArgument("styleId") { type = NavType.StringType }),
            ) { back ->
                val styleId = back.arguments?.getString("styleId") ?: "ecosystem"
                PreviewScreen(
                    styleId      = styleId,
                    onBack       = { nav.popBackStack() },
                    onRegenerate = {
                        nav.navigate("generating/$styleId") {
                            popUpTo("preview/$styleId") { inclusive = true }
                        }
                    },
                    onApply  = {},
                    onShare  = {},
                )
            }
        }
    }
    } // Box
}
