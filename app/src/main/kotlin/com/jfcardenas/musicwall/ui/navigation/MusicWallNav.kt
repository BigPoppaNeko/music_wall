package com.jfcardenas.musicwall.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.jfcardenas.musicwall.ui.screens.*

private object Route {
    const val SPLASH  = "splash"
    const val SOURCE  = "source"
    const val SOURCE_LASTFM  = "source/lastfm"
    const val SOURCE_SPOTIFY = "source/spotify"
    const val SOURCE_EXPLORE = "source/explore"
    const val STYLE   = "style"
    const val GENERATING = "generating/{styleId}"
    const val PREVIEW    = "preview/{styleId}"

    fun generating(styleId: String) = "generating/$styleId"
    fun preview(styleId: String)    = "preview/$styleId"
}

@Composable
fun MusicWallNav(onOnboardingComplete: () -> Unit = {}) {
    val nav = rememberNavController()

    NavHost(
        navController = nav,
        startDestination = Route.SPLASH,
        enterTransition  = { slideInHorizontally(tween(280)) { it / 5 } + fadeIn(tween(280)) },
        exitTransition   = { slideOutHorizontally(tween(280)) { -it / 5 } + fadeOut(tween(180)) },
        popEnterTransition  = { slideInHorizontally(tween(280)) { -it / 5 } + fadeIn(tween(280)) },
        popExitTransition   = { slideOutHorizontally(tween(280)) { it / 5 } + fadeOut(tween(180)) },
    ) {
        composable(Route.SPLASH) {
            SplashScreen(
                onComenzar      = { nav.navigate(Route.SOURCE) },
                onIniciarSesion = { nav.navigate(Route.SOURCE) },
            )
        }

        composable(Route.SOURCE) {
            SourceSelectionScreen(
                onBack = { nav.popBackStack() },
                onSourceSelected = { source ->
                    when (source) {
                        MusicSource.LASTFM  -> nav.navigate(Route.SOURCE_LASTFM)
                        MusicSource.SPOTIFY -> nav.navigate(Route.SOURCE_SPOTIFY)
                        MusicSource.EXPLORE -> nav.navigate(Route.SOURCE_EXPLORE)
                    }
                },
            )
        }

        composable(Route.SOURCE_LASTFM) {
            LastFmSourceScreen(
                onBack      = { nav.popBackStack() },
                onConnected = { nav.navigate(Route.STYLE) { popUpTo(Route.SOURCE) } },
            )
        }

        composable(Route.SOURCE_SPOTIFY) {
            SpotifySourceScreen(
                onBack              = { nav.popBackStack() },
                onPlaylistSelected  = { nav.navigate(Route.STYLE) { popUpTo(Route.SOURCE) } },
            )
        }

        composable(Route.SOURCE_EXPLORE) {
            ExploreSourceScreen(
                onBack     = { nav.popBackStack() },
                onContinue = { nav.navigate(Route.STYLE) { popUpTo(Route.SOURCE) } },
            )
        }

        composable(Route.STYLE) {
            StyleSelectionScreen(
                onBack      = { nav.popBackStack() },
                onContinuar = { styleId ->
                    nav.navigate(Route.generating(styleId))
                },
            )
        }

        composable(
            route = Route.GENERATING,
            arguments = listOf(navArgument("styleId") { type = NavType.StringType }),
        ) { back ->
            val styleId = back.arguments?.getString("styleId") ?: "street"
            GeneratingScreen(
                styleId    = styleId,
                onComplete = {
                    nav.navigate(Route.preview(styleId)) {
                        popUpTo(Route.GENERATING) { inclusive = true }
                    }
                },
                onCancel   = { nav.popBackStack() },
            )
        }

        composable(
            route = Route.PREVIEW,
            arguments = listOf(navArgument("styleId") { type = NavType.StringType }),
        ) { back ->
            val styleId = back.arguments?.getString("styleId") ?: "street"
            PreviewScreen(
                styleId      = styleId,
                onBack       = { nav.popBackStack() },
                onRegenerate = {
                    nav.navigate(Route.generating(styleId)) {
                        popUpTo(Route.PREVIEW) { inclusive = true }
                    }
                },
                onApply  = { onOnboardingComplete() },
                onShare  = {},
            )
        }
    }
}
