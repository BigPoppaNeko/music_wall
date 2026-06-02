package com.jfcardenas.musicwall.ui.navigation

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.jfcardenas.musicwall.ui.components.AlbumTracksOverlay
import com.jfcardenas.musicwall.ui.components.MuralesGridIcon
import com.jfcardenas.musicwall.ui.components.SampleBackground
import com.jfcardenas.musicwall.ui.screens.*
import com.jfcardenas.musicwall.ui.theme.*
import com.jfcardenas.musicwall.ui.viewmodel.CoverInteractionViewModel
import com.jfcardenas.musicwall.ui.viewmodel.HomeViewModel
import com.jfcardenas.musicwall.ui.viewmodel.SettingsViewModel

private data class TabItem(val route: String, val label: String, val icon: ImageVector)

private val TABS = listOf(
    TabItem("inicio",     "Inicio",     Icons.Filled.Home),
    TabItem("biblioteca", "ADN",       Icons.Filled.Person),
    TabItem("murales",    "Murales",    MuralesGridIcon),
    TabItem("cuenta",     "Perfil",     Icons.Filled.Settings),
)

private val TAB_ROUTES = TABS.map { it.route }.toSet()

@Composable
fun MainNav(initialRoute: String? = null) {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val activity = LocalContext.current as ComponentActivity
    val coverVm: CoverInteractionViewModel = hiltViewModel(viewModelStoreOwner = activity)
    val homeVm: HomeViewModel = hiltViewModel(viewModelStoreOwner = activity)
    val settingsVm: SettingsViewModel = hiltViewModel(viewModelStoreOwner = activity)
    val snackbarHostState = remember { SnackbarHostState() }

    val baseRoute = currentRoute?.substringBefore("/")?.substringBefore("?")

    BackHandler(enabled = coverVm.tracksOverlay != null) {
        coverVm.closeTracksOverlay()
    }

    BackHandler(
        enabled = coverVm.tracksOverlay == null &&
            baseRoute != null &&
            baseRoute !in TAB_ROUTES,
    ) {
        nav.popBackStack()
    }

    LaunchedEffect(initialRoute) {
        initialRoute?.let { route ->
            nav.navigate(route) {
                launchSingleTop = true
            }
        }
    }

    LaunchedEffect(coverVm.snackbarMessage) {
        coverVm.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            coverVm.clearSnackbar()
        }
    }

    LaunchedEffect(currentRoute) {
        coverVm.closeTracksOverlay()
    }

    Box(Modifier.fillMaxSize()) {
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
                    vm = homeVm,
                    coverVm = coverVm,
                    onGoToEstilos   = {
                        nav.navigate("murales?tab=1") {
                            popUpTo("inicio") { saveState = true }
                            launchSingleTop = false
                            restoreState    = false
                        }
                    },
                    onGoToFavoritas = { nav.navigate("favoritas") },
                    onNowPlayingClick = { artist, track, album ->
                        nav.navigate("nowplaying/${Uri.encode(artist)}/${Uri.encode(track)}?album=${Uri.encode(album)}")
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
                    coverVm          = coverVm,
                    onBack           = null,
                    onSelectRenderer = { styleId -> nav.navigate("generating/$styleId") },
                )
            }

            composable("biblioteca") {
                MusicalDnaScreen()
            }

            composable("cuenta") {
                SettingsScreen(
                    homeViewModel = homeVm,
                    settingsViewModel = settingsVm,
                    onConnectLastFm = { nav.navigate("source/lastfm") },
                    onConnectSpotify = { nav.navigate("source/spotify") },
                    onExplore = { nav.navigate("source/explore") },
                )
            }

            // Alias legacy
            composable("perfil") {
                LaunchedEffect(Unit) {
                    nav.navigate("biblioteca") {
                        popUpTo("inicio") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }

            composable("ajustes") {
                LaunchedEffect(Unit) {
                    nav.navigate("cuenta") {
                        popUpTo("inicio") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }

            // ── Source detail (from Fuente tab) ───────────────────────────────
            composable("source/lastfm") {
                LastFmSourceScreen(
                    onBack      = { nav.popBackStack() },
                    onConnected = {
                        homeVm.refresh()
                        settingsVm.reload()
                        nav.navigate("biblioteca") {
                            popUpTo("inicio") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }

            composable("source/spotify") {
                SpotifySourceScreen(
                    onBack             = { nav.popBackStack() },
                    onPlaylistSelected = {
                        nav.navigate("biblioteca") {
                            popUpTo("inicio") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }

            composable("source/explore") {
                ExploreSourceScreen(
                    onBack     = { nav.popBackStack() },
                    onContinue = {
                        homeVm.refresh()
                        settingsVm.reload()
                        nav.navigate("biblioteca") {
                            popUpTo("inicio") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }

            // ── Mis portadas favoritas ─────────────────────────────────────────
            composable("favoritas") {
                FavoritesScreen(onBack = { nav.popBackStack() }, coverVm = coverVm)
            }

            // ── Now Playing Detail ─────────────────────────────────────────────
            composable(
                route = "nowplaying/{artist}/{track}?album={album}",
                arguments = listOf(
                    navArgument("artist") { type = NavType.StringType },
                    navArgument("track")  { type = NavType.StringType },
                    navArgument("album")  { type = NavType.StringType; defaultValue = "" },
                ),
            ) { back ->
                val artist    = back.arguments?.getString("artist") ?: ""
                val trackName = back.arguments?.getString("track")  ?: ""
                val albumName = back.arguments?.getString("album") ?: ""
                NowPlayingDetailScreen(
                    artist    = artist,
                    trackName = trackName,
                    albumHint = albumName,
                    onBack    = { nav.popBackStack() },
                    coverVm   = coverVm,
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
                    onApply  = { nav.popBackStack() },
                    onShare  = {},
                )
            }
        }
    }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp),
        )

        coverVm.tracksOverlay?.let { detail ->
            AlbumTracksOverlay(
                detail = detail,
                isFavorite = coverVm.isFavorite(detail.cover.key),
                onDismiss = { coverVm.closeTracksOverlay() },
                onToggleFavorite = { coverVm.toggleFavoriteFromOverlay() },
            )
        }
    }
}
