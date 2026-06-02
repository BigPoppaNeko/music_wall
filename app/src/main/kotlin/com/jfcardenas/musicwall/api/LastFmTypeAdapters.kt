package com.jfcardenas.musicwall.api

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

internal class AlbumTracksContainerDeserializer : JsonDeserializer<AlbumTracksContainer> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext,
    ): AlbumTracksContainer {
        val trackElement = json.asJsonObject.get("track") ?: return AlbumTracksContainer()
        val listType = object : TypeToken<List<AlbumTrackItem>>() {}.type
        val tracks = when {
            trackElement.isJsonArray -> context.deserialize<List<AlbumTrackItem>>(trackElement, listType)
            trackElement.isJsonObject -> listOf(context.deserialize(trackElement, AlbumTrackItem::class.java))
            else -> emptyList()
        }
        return AlbumTracksContainer(tracks = tracks)
    }
}

internal class AlbumInfoResponseDeserializer : JsonDeserializer<AlbumInfoResponse> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext,
    ): AlbumInfoResponse {
        val albumObj = json.asJsonObject.getAsJsonObject("album") ?: return AlbumInfoResponse(null)
        val base = context.deserialize<AlbumInfoDetail>(
            albumObj,
            object : TypeToken<AlbumInfoDetailBase>() {}.type,
        ) as AlbumInfoDetailBase
        val tracks = albumObj.get("tracks")?.let { tracksJson ->
            context.deserialize<AlbumTracksContainer>(tracksJson, AlbumTracksContainer::class.java)
        }
        return AlbumInfoResponse(
            album = AlbumInfoDetail(
                name = base.name,
                artist = base.artist,
                mbid = base.mbid,
                images = base.images,
                wiki = base.wiki,
                tags = base.tags,
                tracks = tracks,
                listeners = base.listeners,
                playcount = base.playcount,
            ),
        )
    }

    private data class AlbumInfoDetailBase(
        val name: String,
        val artist: String = "",
        val mbid: String? = null,
        @com.google.gson.annotations.SerializedName("image") val images: List<LastFmImage>? = null,
        val wiki: WikiSection? = null,
        val tags: AlbumTagsWrapper? = null,
        val listeners: String? = null,
        val playcount: String? = null,
    )
}
