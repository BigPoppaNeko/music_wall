package com.jfcardenas.musicwall.data

import android.content.Context
import com.jfcardenas.musicwall.service.CollageWallpaper
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Preferencias de fuente musical, wallpaper y onboarding de contenido.
 * Sesión de usuario (Google/local) sigue en [com.jfcardenas.musicwall.auth.UserSessionRepository].
 */
@Singleton
class UserSettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val prefs get() =
        context.getSharedPreferences(CollageWallpaper.PREFS_NAME, Context.MODE_PRIVATE)

    fun getMusicSource(): String =
        prefs.getString(CollageWallpaper.PREF_SOURCE, CollageWallpaper.PREF_SOURCE_LASTFM)
            ?: CollageWallpaper.PREF_SOURCE_LASTFM

    fun getLastFmUsername(): String =
        prefs.getString(CollageWallpaper.PREF_USERNAME, "") ?: ""

    fun getImageKind(): String =
        prefs.getString(CollageWallpaper.PREF_IMAGE_KIND, "ALBUMS") ?: "ALBUMS"

    fun getPeriod(): String =
        prefs.getString(CollageWallpaper.PREF_PERIOD, "7day") ?: "7day"

    fun getLimit(): Int =
        prefs.getString(CollageWallpaper.PREF_LIMIT, "25")?.toIntOrNull() ?: 25

    fun getCollagePath(): String? =
        prefs.getString(CollageWallpaper.PREF_COLLAGE_PATH, null)

    fun isAutoWallpaperRefreshEnabled(): Boolean =
        prefs.getBoolean(CollageWallpaper.PREF_AUTO_WALLPAPER_REFRESH, true)

    fun setAutoWallpaperRefreshEnabled(enabled: Boolean) {
        prefs.edit()
            .putBoolean(CollageWallpaper.PREF_AUTO_WALLPAPER_REFRESH, enabled)
            .apply()
    }

    fun saveLastFmConnection(username: String) {
        val trimmed = username.trim()
        prefs.edit()
            .putString(CollageWallpaper.PREF_USERNAME, trimmed)
            .putString(CollageWallpaper.PREF_SOURCE, CollageWallpaper.PREF_SOURCE_LASTFM)
            .apply()
        if (prefs.getString(CollageWallpaper.PREF_USER_ID, "").isNullOrBlank()) {
            prefs.edit()
                .putString(CollageWallpaper.PREF_USER_ID, "lastfm_$trimmed")
                .putString(CollageWallpaper.PREF_USER_NAME, trimmed)
                .putString(CollageWallpaper.PREF_AUTH_PROVIDER, CollageWallpaper.PREF_SOURCE_LASTFM)
                .apply()
        }
    }

    fun saveExploreArtists(artists: Collection<String>) {
        val cleaned = artists.map { it.trim() }.filter { it.isNotEmpty() }.toSet()
        val userId = prefs.getString(CollageWallpaper.PREF_USER_ID, "")?.takeIf { it.isNotBlank() }
            ?: "explore_${UUID.randomUUID()}"
        prefs.edit()
            .putString(CollageWallpaper.PREF_USER_ID, userId)
            .putString(CollageWallpaper.PREF_USER_NAME, "Explorador")
            .putString(CollageWallpaper.PREF_AUTH_PROVIDER, CollageWallpaper.PREF_SOURCE_EXPLORE_ARTISTS)
            .putString(CollageWallpaper.PREF_SOURCE, CollageWallpaper.PREF_SOURCE_EXPLORE_ARTISTS)
            .putString(CollageWallpaper.PREF_IMAGE_KIND, "ALBUMS")
            .putString(CollageWallpaper.PREF_PERIOD, "random")
            .putStringSet(CollageWallpaper.PREF_EXPLORE_ARTISTS, cleaned)
            .apply()
    }

    fun saveWallpaperPrefs(imageKind: String, period: String, limit: Int) {
        prefs.edit()
            .putString(CollageWallpaper.PREF_IMAGE_KIND, imageKind)
            .putString(CollageWallpaper.PREF_PERIOD, period)
            .putString(CollageWallpaper.PREF_LIMIT, limit.toString())
            .apply()
    }

    fun saveCollagePath(path: String) {
        prefs.edit().putString(CollageWallpaper.PREF_COLLAGE_PATH, path).apply()
    }

    fun clearLastFmConnection() {
        prefs.edit().remove(CollageWallpaper.PREF_USERNAME).apply()
    }
}
