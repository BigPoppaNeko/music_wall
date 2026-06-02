package com.jfcardenas.musicwall.api

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface DeezerService {
    @GET("search/artist")
    suspend fun searchArtist(
        @Query("q") query: String,
        @Query("limit") limit: Int = 1,
    ): DeezerArtistSearchResponse

    @GET("search/album")
    suspend fun searchAlbum(
        @Query("q") query: String,
        @Query("limit") limit: Int = 1,
    ): DeezerAlbumSearchResponse
}

data class DeezerArtistSearchResponse(
    @SerializedName("data") val data: List<DeezerArtist>?
)

data class DeezerArtist(
    val id: Long,
    val name: String,
    @SerializedName("picture_medium") val pictureMedium: String?,
    @SerializedName("picture_xl") val pictureXl: String?,
)

data class DeezerAlbumSearchResponse(
    @SerializedName("data") val data: List<DeezerAlbum>?
)

data class DeezerAlbum(
    val id: Long,
    val title: String,
    @SerializedName("cover_xl") val coverXl: String?,
    @SerializedName("cover_medium") val coverMedium: String?,
)

internal fun createDeezerService(httpClient: OkHttpClient): DeezerService =
    Retrofit.Builder()
        .baseUrl("https://api.deezer.com/")
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(DeezerService::class.java)
