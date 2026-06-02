package com.jfcardenas.musicwall.auth

import android.content.Context
import com.jfcardenas.musicwall.service.CollageWallpaper
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserSessionRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val prefs get() =
        context.getSharedPreferences(CollageWallpaper.PREFS_NAME, Context.MODE_PRIVATE)

    fun getUserId(): String = prefs.getString(CollageWallpaper.PREF_USER_ID, "") ?: ""

    fun getDisplayName(): String = prefs.getString(CollageWallpaper.PREF_USER_NAME, "") ?: ""

    fun getEmail(): String = prefs.getString(CollageWallpaper.PREF_USER_EMAIL, "") ?: ""

    fun getLastFmUsername(): String = prefs.getString(CollageWallpaper.PREF_USERNAME, "") ?: ""

    fun getAuthProvider(): String = prefs.getString(CollageWallpaper.PREF_AUTH_PROVIDER, "") ?: ""

    fun isOnboardingComplete(): Boolean =
        prefs.getBoolean(CollageWallpaper.PREF_ONBOARDING_COMPLETE, false)

    fun hasActiveSession(): Boolean =
        isOnboardingComplete() && getUserId().isNotBlank()

    fun usesExploreSource(): Boolean =
        prefs.getString(CollageWallpaper.PREF_SOURCE, "") == CollageWallpaper.PREF_SOURCE_EXPLORE_ARTISTS

    fun getExploreArtists(): List<String> =
        prefs.getStringSet(CollageWallpaper.PREF_EXPLORE_ARTISTS, emptySet())
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?.distinct()
            .orEmpty()

    fun usesLocalScrobbler(): Boolean {
        val source = prefs.getString(CollageWallpaper.PREF_SOURCE, "") ?: ""
        return source == CollageWallpaper.PREF_SOURCE_LOCAL ||
            source == CollageWallpaper.PREF_SOURCE_GOOGLE
    }

    fun ensureExploreSession() {
        val artists = getExploreArtists()
        if (artists.isEmpty()) return
        val id = prefs.getString(CollageWallpaper.PREF_USER_ID, "")?.takeIf { it.isNotBlank() }
            ?: "explore_${UUID.randomUUID()}"
        prefs.edit()
            .putString(CollageWallpaper.PREF_USER_ID, id)
            .putString(CollageWallpaper.PREF_USER_NAME, "Explorador")
            .putString(CollageWallpaper.PREF_AUTH_PROVIDER, CollageWallpaper.PREF_SOURCE_EXPLORE_ARTISTS)
            .putString(CollageWallpaper.PREF_SOURCE, CollageWallpaper.PREF_SOURCE_EXPLORE_ARTISTS)
            .apply()
    }

    fun saveGoogleUser(id: String, displayName: String, email: String) {
        prefs.edit()
            .putString(CollageWallpaper.PREF_USER_ID, id)
            .putString(CollageWallpaper.PREF_USER_NAME, displayName)
            .putString(CollageWallpaper.PREF_USER_EMAIL, email)
            .putString(CollageWallpaper.PREF_AUTH_PROVIDER, CollageWallpaper.PREF_SOURCE_GOOGLE)
            .putString(CollageWallpaper.PREF_SOURCE, CollageWallpaper.PREF_SOURCE_GOOGLE)
            .apply()
    }

    fun saveLocalUser(displayName: String = "Oyente") {
        val id = prefs.getString(CollageWallpaper.PREF_USER_ID, "")?.takeIf { it.isNotBlank() }
            ?: "local_${UUID.randomUUID()}"
        prefs.edit()
            .putString(CollageWallpaper.PREF_USER_ID, id)
            .putString(CollageWallpaper.PREF_USER_NAME, displayName)
            .putString(CollageWallpaper.PREF_AUTH_PROVIDER, CollageWallpaper.PREF_SOURCE_LOCAL)
            .putString(CollageWallpaper.PREF_SOURCE, CollageWallpaper.PREF_SOURCE_LOCAL)
            .apply()
    }

    fun markOnboardingComplete() {
        prefs.edit()
            .putBoolean(CollageWallpaper.PREF_ONBOARDING_COMPLETE, true)
            .apply()
    }

    fun clearSession() {
        prefs.edit()
            .remove(CollageWallpaper.PREF_USER_ID)
            .remove(CollageWallpaper.PREF_USER_NAME)
            .remove(CollageWallpaper.PREF_USER_EMAIL)
            .remove(CollageWallpaper.PREF_AUTH_PROVIDER)
            .remove(CollageWallpaper.PREF_USERNAME)
            .remove(CollageWallpaper.PREF_SOURCE)
            .putBoolean(CollageWallpaper.PREF_ONBOARDING_COMPLETE, false)
            .apply()
    }
}
