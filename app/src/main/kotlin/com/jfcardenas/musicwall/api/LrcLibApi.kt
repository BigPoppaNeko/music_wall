package com.jfcardenas.musicwall.api

import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface LrcLibService {
    @GET("api/search")
    suspend fun search(
        @Query("track_name") trackName: String,
        @Query("artist_name") artistName: String,
    ): List<LrcLibResult>

    @GET("api/search")
    suspend fun searchQuery(
        @Query("q") query: String,
    ): List<LrcLibResult>
}

data class LrcLibResult(
    @SerializedName("plainLyrics") val plainLyrics: String?,
    @SerializedName("syncedLyrics") val syncedLyrics: String?,
)

internal fun createLrcLibService(): LrcLibService =
    Retrofit.Builder()
        .baseUrl("https://lrclib.net/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(LrcLibService::class.java)
