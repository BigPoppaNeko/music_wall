package com.jfcardenas.musicwall.data

object FavoriteKeys {
    fun id(artist: String, album: String): String =
        "${artist.trim().lowercase()}::${album.trim().lowercase()}"
}
