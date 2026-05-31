package com.jfcardenas.musicwall.api

import com.google.gson.annotations.SerializedName
import com.jfcardenas.musicwall.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface DiscogsService {
    @GET("database/search")
    suspend fun searchMaster(
        @Query("release_title") releaseTitle: String,
        @Query("artist") artist: String,
        @Query("type") type: String = "master",
        @Query("per_page") perPage: Int = 3,
    ): DiscogsSearchResponse

    @GET("masters/{id}/versions")
    suspend fun getMasterVersions(
        @Path("id") masterId: Int,
        @Query("per_page") perPage: Int = 50,
    ): MasterVersionsResponse
}

data class DiscogsSearchResponse(
    val results: List<DiscogsSearchResult>?,
)

data class DiscogsSearchResult(
    val id: Int,
    val title: String?,
    val type: String?,
    val year: String?,
)

data class MasterVersionsResponse(
    val versions: List<MasterVersion>?,
    val pagination: DiscogsPagination?,
)

data class MasterVersion(
    val id: Int,
    val format: String?,
    val country: String?,
    val released: String?,
    val label: String?,
    val thumb: String?,
)

data class DiscogsPagination(
    val items: Int?,
)

internal fun createDiscogsService(): DiscogsService {
    val logging = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC
                else HttpLoggingInterceptor.Level.NONE
    }
    val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            chain.proceed(
                chain.request().newBuilder()
                    .header("User-Agent", "MusicWall/1.0 +https://musicwall.app")
                    .build()
            )
        }
        .addInterceptor(logging)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    return Retrofit.Builder()
        .baseUrl("https://api.discogs.com/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(DiscogsService::class.java)
}
