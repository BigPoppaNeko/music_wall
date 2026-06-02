package com.jfcardenas.musicwall.api

import com.google.gson.annotations.SerializedName

// ── Albums ────────────────────────────────────────────────────────────────────

data class TopAlbumsResponse(
    @SerializedName("topalbums") val topAlbums: TopAlbums
)

data class TagTopAlbumsResponse(
    @SerializedName("albums") val albums: TopAlbums,
)

data class TopAlbums(
    @SerializedName("album") val albums: List<Album>,
    @SerializedName("@attr") val attr: TopAlbumsAttr? = null,
)

data class TopAlbumsAttr(
    val user: String? = null,
    val artist: String? = null,
    val page: String? = null,
    val perPage: String? = null,
    val totalPages: String? = null,
    val total: String? = null,
)

data class Album(
    val name: String,
    val artist: ArtistRef,
    @SerializedName("image") val images: List<LastFmImage>,
    val playcount: String?,
    val mbid: String?,
    val url: String?
)

// ── Artists ───────────────────────────────────────────────────────────────────

data class TopArtistsResponse(
    @SerializedName("topartists") val topArtists: TopArtists
)

data class TopArtists(
    @SerializedName("artist") val artists: List<ArtistItem>
)

data class ArtistRef(
    val name: String,
    val mbid: String? = null,
    val url: String? = null
)

data class ArtistItem(
    val name: String,
    @SerializedName("image") val images: List<LastFmImage>?,
    val playcount: String?,
    val mbid: String?,
    val url: String?
)

// ── Top Tracks ────────────────────────────────────────────────────────────────

data class TopTracksResponse(
    @SerializedName("toptracks") val topTracks: TopTracks
)

data class TopTracks(
    @SerializedName("track") val tracks: List<TopTrackItem>
)

data class TopTrackItem(
    val name: String,
    val mbid: String?,
    val url: String?,
    val playcount: String?,
    val artist: ArtistRef,
    @SerializedName("image") val images: List<LastFmImage>,
    @SerializedName("@attr") val attr: TrackRankAttr?
)

data class TrackRankAttr(val rank: String?)

// ── Loved Tracks ──────────────────────────────────────────────────────────────

data class LovedTracksResponse(
    @SerializedName("lovedtracks") val lovedTracks: LovedTracks
)

data class LovedTracks(
    @SerializedName("track") val tracks: List<LovedTrackItem>
)

data class LovedTrackItem(
    val name: String,
    val mbid: String?,
    val url: String?,
    val artist: ArtistRef,
    @SerializedName("image") val images: List<LastFmImage>,
    val date: TrackDate?
)

data class TrackDate(
    val uts: String?,
    @SerializedName("#text") val text: String?
)

// ── User Info ─────────────────────────────────────────────────────────────────

data class UserInfoResponse(
    @SerializedName("user") val user: LastFmUser
)

data class LastFmUser(
    val name: String,
    val realname: String,
    val playcount: String,
    val country: String?,
    val subscriber: String?,
    @SerializedName("track_count") val trackCount: String?,
    @SerializedName("artist_count") val artistCount: String?,
    @SerializedName("album_count") val albumCount: String?,
    val registered: RegisteredInfo?,
    @SerializedName("image") val images: List<LastFmImage>?
)

data class RegisteredInfo(
    val unixtime: String?,
    @SerializedName("#text") val text: Long?
)

// ── Images ────────────────────────────────────────────────────────────────────

data class LastFmImage(
    @SerializedName("#text") val url: String,
    val size: String
)

// ── Recent Tracks ─────────────────────────────────────────────────────────────

data class RecentTracksResponse(
    @SerializedName("recenttracks") val recentTracks: RecentTracks
)

data class RecentTracks(
    @SerializedName("track") val tracks: List<RecentTrack>,
    @SerializedName("@attr") val attr: RecentTracksAttr?,
)

data class RecentTracksAttr(
    val total: String?,
    val user: String?,
)

data class RecentTrack(
    val name: String,
    val artist: SimpleRef,
    val album: SimpleRef,
    @SerializedName("image") val images: List<LastFmImage>,
    @SerializedName("@attr") val attr: NowPlayingAttr?,
    val date: TrackDate?,
    val url: String?,
)

data class SimpleRef(
    @SerializedName("#text") val name: String,
    val mbid: String? = null,
)

data class NowPlayingAttr(
    val nowplaying: String?,
)

// ── Search ────────────────────────────────────────────────────────────────────

data class ArtistSearchResponse(
    val results: ArtistSearchResults
)

data class ArtistSearchResults(
    @SerializedName("artistmatches") val artistMatches: ArtistMatches
)

data class ArtistMatches(
    @SerializedName("artist") val artists: List<SearchArtist>
)

data class SearchArtist(
    val name: String,
    val listeners: String?,
    @SerializedName("image") val images: List<LastFmImage>?
)

data class AlbumSearchResponse(
    val results: AlbumSearchResults
)

data class AlbumSearchResults(
    @SerializedName("albummatches") val albumMatches: AlbumMatches
)

data class AlbumMatches(
    @SerializedName("album") val albums: List<SearchAlbum>
)

data class SearchAlbum(
    val name: String,
    val artist: String,
    @SerializedName("image") val images: List<LastFmImage>?
)

data class TrackSearchResponse(
    val results: TrackSearchResults,
)

data class TrackSearchResults(
    @SerializedName("trackmatches") val trackMatches: TrackMatches,
)

data class TrackMatches(
    @SerializedName("track") val tracks: List<SearchTrack>,
)

data class SearchTrack(
    val name: String,
    val artist: String,
    val mbid: String?,
    val url: String?,
)

// ── Images ────────────────────────────────────────────────────────────────────

fun List<LastFmImage>.getExtraLargeUrl(): String? {
    fun String.isRealImage() = isNotEmpty() && !contains("2a96cbd8b46e442fc41c2b86b821562f")
    return firstOrNull { it.size == "mega" }?.url?.takeIf { it.isRealImage() }
        ?: firstOrNull { it.size == "extralarge" }?.url?.takeIf { it.isRealImage() }
        ?: firstOrNull { it.size == "large" }?.url?.takeIf { it.isRealImage() }
}

// ── Track Info ────────────────────────────────────────────────────────────────

data class TrackInfoResponse(
    @SerializedName("track") val track: TrackInfoDetail?
)

data class TrackInfoDetail(
    val name: String,
    val artist: ArtistRef,
    val album: TrackAlbumRef?,
    val wiki: WikiSection?,
    val duration: String? = null,
)

data class TrackAlbumRef(
    val title: String?,
    val mbid: String?,
    @SerializedName("image") val images: List<LastFmImage>?
)

data class WikiSection(
    val published: String?,
    val summary: String?,
    val content: String?
)

// ── Artist Info ───────────────────────────────────────────────────────────────

data class ArtistInfoResponse(
    @SerializedName("artist") val artist: ArtistInfoDetail?
)

data class ArtistInfoDetail(
    val name: String,
    val bio: BioSection?
)

data class BioSection(
    val published: String?,
    val summary: String?,
    val content: String?
)

// ── User Top Tags ─────────────────────────────────────────────────────────────

data class UserTopTagsResponse(
    @SerializedName("toptags") val topTags: UserTopTags
)

data class UserTopTags(
    @SerializedName("tag") val tags: List<UserTag>
)

data class UserTag(
    val name: String,
    val count: String,
    val url: String?
)

// ── Album Info ────────────────────────────────────────────────────────────────

data class AlbumInfoResponse(
    @SerializedName("album") val album: AlbumInfoDetail?,
)

data class AlbumInfoDetail(
    val name: String,
    val artist: String = "",
    val mbid: String? = null,
    @SerializedName("image") val images: List<LastFmImage>? = null,
    val wiki: WikiSection? = null,
    val tags: AlbumTagsWrapper? = null,
    val tracks: AlbumTracksContainer? = null,
    val listeners: String? = null,
    val playcount: String? = null,
)

data class AlbumTagsWrapper(
    @SerializedName("tag") val tags: List<UserTag>? = null,
)

data class AlbumTracksContainer(
    @SerializedName("track") val tracks: List<AlbumTrackItem> = emptyList(),
)

data class AlbumTrackItem(
    val name: String,
    val duration: String? = null,
    @SerializedName("@attr") val attr: TrackRankAttr? = null,
)

fun AlbumInfoDetail.releaseYear(): String? {
    val text = wiki?.summary.orEmpty() + " " + wiki?.content.orEmpty()
    return Regex("""\b(19|20)\d{2}\b""").find(text)?.value
}

fun formatTrackDuration(raw: String?): String {
    val seconds = raw?.toLongOrNull() ?: return "—"
    if (seconds <= 0L) return "—"
    val mins = seconds / 60
    val secs = seconds % 60
    return "%d:%02d".format(mins, secs)
}

fun formatTotalDuration(totalSeconds: Long): String {
    if (totalSeconds <= 0L) return "—"
    val hours = totalSeconds / 3600
    val mins = (totalSeconds % 3600) / 60
    val secs = totalSeconds % 60
    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, mins, secs)
    } else {
        "%d:%02d".format(mins, secs)
    }
}
