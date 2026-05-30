package com.jfcardenas.musicwall.api

import com.google.gson.annotations.SerializedName
import com.jfcardenas.musicwall.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

data class SpotifyOEmbedResponse(
    val title: String,
    @SerializedName("author_name") val authorName: String,
    @SerializedName("thumbnail_url") val thumbnailUrl: String?,
    val type: String,
)

interface SpotifyOEmbedService {
    @GET("oembed")
    suspend fun getPlaylistInfo(@Query("url") url: String): SpotifyOEmbedResponse
}

internal fun createSpotifyOEmbedService(): SpotifyOEmbedService {
    val logging = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC
                else HttpLoggingInterceptor.Level.NONE
    }
    val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    return Retrofit.Builder()
        .baseUrl("https://open.spotify.com/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(SpotifyOEmbedService::class.java)
}
