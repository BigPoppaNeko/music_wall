package com.jfcardenas.musicwall.api

import com.google.gson.annotations.SerializedName

data class TopAlbumsResponse(
    @SerializedName("topalbums") val topAlbums: TopAlbums
)

data class TopAlbums(
    @SerializedName("album") val albums: List<Album>
)

data class Album(
    val name: String,
    val artist: ArtistRef,
    @SerializedName("image") val images: List<LastFmImage>
)

data class TopArtistsResponse(
    @SerializedName("topartists") val topArtists: TopArtists
)

data class TopArtists(
    @SerializedName("artist") val artists: List<ArtistItem>
)

data class ArtistRef(val name: String)

data class ArtistItem(
    val name: String,
    @SerializedName("image") val images: List<LastFmImage>?
)

data class LastFmImage(
    @SerializedName("#text") val url: String,
    val size: String
)

fun List<LastFmImage>.getExtraLargeUrl(): String? =
    firstOrNull { it.size == "extralarge" }?.url?.takeIf { it.isNotEmpty() }
        ?: firstOrNull { it.size == "large" }?.url?.takeIf { it.isNotEmpty() }
