package com.jfcardenas.musicwall.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "ArtistImageResolver"

// Last.fm removed artist images in 2022; this resolves them via the iTunes Search API.
@Singleton
class ArtistImageResolver @Inject constructor() {

    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    private val cache = mutableMapOf<String, String?>()

    suspend fun getImageUrl(artistName: String): String? {
        if (cache.containsKey(artistName)) return cache[artistName]
        return withContext(Dispatchers.IO) {
            fetchFromItunes(artistName).also { cache[artistName] = it }
        }
    }

    private fun fetchFromItunes(artistName: String): String? {
        return try {
            val query = URLEncoder.encode(artistName, "UTF-8")
            val request = Request.Builder()
                .url("https://itunes.apple.com/search?term=$query&media=music&entity=musicArtist&limit=1")
                .build()
            val body = client.newCall(request).execute().use { it.body?.string() }
                ?: return null
            val results = JSONObject(body).getJSONArray("results")
            if (results.length() == 0) return null
            results.getJSONObject(0)
                .optString("artworkUrl100", "")
                .replace("100x100bb", "600x600bb")
                .takeIf { it.isNotEmpty() && it.startsWith("http") }
        } catch (e: Exception) {
            Log.w(TAG, "iTunes lookup failed for '$artistName': ${e.message}")
            null
        }
    }
}
