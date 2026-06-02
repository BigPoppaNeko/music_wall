package com.jfcardenas.musicwall.api

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

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

internal fun createSpotifyOEmbedService(httpClient: OkHttpClient): SpotifyOEmbedService {
    return Retrofit.Builder()
        .baseUrl("https://open.spotify.com/")
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(SpotifyOEmbedService::class.java)
}
