package com.jfcardenas.musicwall.scrobble

object MetadataNormalizer {

    private val junkTitleSuffixes = Regex(
        """(?i)\s*[\(\[]\s*(live|remaster(ed)?(\s+\d{4})?|radio edit|single version|deluxe|bonus track|acoustic|demo|instrumental)[^\)\]]*[\)\]]""",
    )

    fun normalize(raw: RawNowPlaying): RawNowPlaying? {
        val artist = cleanArtist(raw.artist)
        val track = cleanTrack(raw.track)
        if (!isValid(artist, track)) return null
        return raw.copy(
            artist = artist,
            track = track,
            album = cleanAlbum(raw.album),
        )
    }

    fun isValid(artist: String, track: String): Boolean {
        if (artist.isBlank() || track.isBlank()) return false
        val lowerArtist = artist.lowercase()
        val lowerTrack = track.lowercase()
        if (lowerArtist in IGNORE_ARTISTS || lowerTrack in IGNORE_TRACKS) return false
        if (lowerTrack.contains("advertisement") || lowerTrack.contains("anuncio")) return false
        return true
    }

    fun cacheKey(artist: String, track: String, album: String): String =
        "${artist.trim().lowercase()}::${track.trim().lowercase()}::${album.trim().lowercase()}"

    fun cleanTrack(title: String): String =
        title
            .replace(junkTitleSuffixes, "")
            .replace(Regex("""(?i)\s*-?\s*feat\.?\s+.*$"""), "")
            .replace(Regex("""(?i)\s*-?\s*ft\.?\s+.*$"""), "")
            .replace(Regex("""(?i)\s*-?\s*with\s+.*$"""), "")
            .replace(Regex("""\s+"""), " ")
            .trim()

    fun cleanArtist(artist: String): String =
        artist
            .substringBefore(" feat.", artist)
            .substringBefore(" ft.", artist)
            .substringBefore(",", artist)
            .replace(Regex("""\s+"""), " ")
            .trim()

    fun cleanAlbum(album: String): String =
        album
            .replace(junkTitleSuffixes, "")
            .replace(Regex("""\s+"""), " ")
            .trim()

    private val IGNORE_ARTISTS = setOf(
        "unknown artist",
        "desconocido",
        "<unknown>",
    )

    private val IGNORE_TRACKS = setOf(
        "",
        "unknown",
    )
}
