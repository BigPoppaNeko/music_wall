package com.jfcardenas.musicwall.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface LastFmService {
    @GET(".")
    suspend fun getTopAlbums(
        @Query("method") method: String = "user.gettopalbums",
        @Query("user") user: String,
        @Query("period") period: String,
        @Query("limit") limit: Int,
        @Query("api_key") apiKey: String,
        @Query("format") format: String = "json"
    ): TopAlbumsResponse

    @GET(".")
    suspend fun getTopArtists(
        @Query("method") method: String = "user.gettopartists",
        @Query("user") user: String,
        @Query("period") period: String,
        @Query("limit") limit: Int,
        @Query("api_key") apiKey: String,
        @Query("format") format: String = "json"
    ): TopArtistsResponse
}

object LastFmApi {
    private const val BASE_URL = "https://ws.audioscrobbler.com/2.0/"

    val service: LastFmService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LastFmService::class.java)
    }
}
