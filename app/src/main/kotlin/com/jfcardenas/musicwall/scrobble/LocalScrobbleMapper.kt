package com.jfcardenas.musicwall.scrobble

import com.jfcardenas.musicwall.api.LastFmImage
import com.jfcardenas.musicwall.api.NowPlayingAttr
import com.jfcardenas.musicwall.api.RecentTrack
import com.jfcardenas.musicwall.api.SimpleRef
import com.jfcardenas.musicwall.api.TrackDate
import com.jfcardenas.musicwall.data.local.db.entity.LocalScrobbleEntity

object LocalScrobbleMapper {

    fun toRecentTrack(entity: LocalScrobbleEntity): RecentTrack {
        val images = entity.imageUrl?.takeIf { it.isNotBlank() }?.let { url ->
            listOf(LastFmImage(url = url, size = "extralarge"))
        }.orEmpty()
        return RecentTrack(
            name = entity.trackName,
            artist = SimpleRef(name = entity.artistName, mbid = entity.artistMbid),
            album = SimpleRef(name = entity.albumName, mbid = entity.albumMbid),
            images = images,
            attr = if (entity.isNowPlaying) NowPlayingAttr(nowplaying = "true") else null,
            date = if (entity.isNowPlaying) null else TrackDate(uts = (entity.playedAt / 1000L).toString(), text = null),
            url = null,
        )
    }
}
