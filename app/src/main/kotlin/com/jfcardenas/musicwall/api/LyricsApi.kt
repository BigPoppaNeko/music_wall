package com.jfcardenas.musicwall.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

interface LyricsService {
    @GET("v1/{artist}/{title}")
    suspend fun getLyrics(
        @Path("artist") artist: String,
        @Path("title") title: String,
    ): LyricsResponse
}

data class LyricsResponse(
    val lyrics: String?,
    val error: String?,
)

internal fun createLyricsService(): LyricsService =
    Retrofit.Builder()
        .baseUrl("https://api.lyrics.ovh/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(LyricsService::class.java)
