package com.jfcardenas.musicwall.api

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.jfcardenas.musicwall.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.io.IOException
import java.util.concurrent.TimeUnit

interface LastFmService {
    @GET(".")
    suspend fun getTopAlbums(
        @Query("method") method: String = "user.gettopalbums",
        @Query("user") user: String,
        @Query("period") period: String,
        @Query("limit") limit: Int
    ): TopAlbumsResponse

    @GET(".")
    suspend fun getTopArtists(
        @Query("method") method: String = "user.gettopartists",
        @Query("user") user: String,
        @Query("period") period: String,
        @Query("limit") limit: Int
    ): TopArtistsResponse

    @GET(".")
    suspend fun getRecentTracks(
        @Query("method") method: String = "user.getrecenttracks",
        @Query("user") user: String,
        @Query("limit") limit: Int
    ): RecentTracksResponse

    @GET(".")
    suspend fun getUserInfo(
        @Query("method") method: String = "user.getinfo",
        @Query("user") user: String
    ): UserInfoResponse

    @GET(".")
    suspend fun getTopTracks(
        @Query("method") method: String = "user.gettoptracks",
        @Query("user") user: String,
        @Query("period") period: String,
        @Query("limit") limit: Int
    ): TopTracksResponse

    @GET(".")
    suspend fun getLovedTracks(
        @Query("method") method: String = "user.getlovedtracks",
        @Query("user") user: String,
        @Query("limit") limit: Int
    ): LovedTracksResponse

    @GET(".")
    suspend fun getWeeklyAlbumChart(
        @Query("method") method: String = "user.getweeklyalbumchart",
        @Query("user") user: String,
        @Query("from") from: Long? = null,
        @Query("to") to: Long? = null
    ): WeeklyAlbumChartResponse

    @GET(".")
    suspend fun getWeeklyArtistChart(
        @Query("method") method: String = "user.getweeklyartistchart",
        @Query("user") user: String,
        @Query("from") from: Long? = null,
        @Query("to") to: Long? = null
    ): WeeklyArtistChartResponse
}

class LastFmApiException(val code: Int, message: String) : IOException(message)

private class ApiKeyInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val apiKey = BuildConfig.LASTFM_API_KEY
        if (apiKey.isEmpty()) throw LastFmApiException(10, "LASTFM_API_KEY no configurada en local.properties")
        val url = chain.request().url.newBuilder()
            .addQueryParameter("api_key", apiKey)
            .addQueryParameter("format", "json")
            .build()
        return chain.proceed(chain.request().newBuilder().url(url).build())
    }
}

private class LastFmErrorInterceptor : Interceptor {
    private val gson = Gson()
    private data class ErrorBody(val error: Int?, val message: String?)

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        if (!response.isSuccessful) return response
        val body = try {
            response.peekBody(Long.MAX_VALUE).string()
        } catch (e: Exception) {
            return response
        }
        if (body.contains("\"error\"")) {
            try {
                val err = gson.fromJson(body, ErrorBody::class.java)
                if (err?.error != null && err.error > 0) {
                    throw LastFmApiException(err.error, err.message ?: "Last.fm error ${err.error}")
                }
            } catch (e: JsonSyntaxException) {
                // valid response that happens to contain the word "error"
            }
        }
        return response
    }
}

object LastFmApi {
    private const val BASE_URL = "https://ws.audioscrobbler.com/2.0/"

    val service: LastFmService by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC
                    else HttpLoggingInterceptor.Level.NONE
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(ApiKeyInterceptor())
            .addInterceptor(LastFmErrorInterceptor())
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LastFmService::class.java)
    }
}
