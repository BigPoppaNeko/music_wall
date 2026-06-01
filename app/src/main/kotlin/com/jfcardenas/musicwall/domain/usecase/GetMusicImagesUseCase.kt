package com.jfcardenas.musicwall.domain.usecase

import com.jfcardenas.musicwall.data.NetworkResult
import com.jfcardenas.musicwall.domain.model.MusicImage
import com.jfcardenas.musicwall.domain.repository.MusicRepository
import javax.inject.Inject

class GetMusicImagesUseCase @Inject constructor(
    private val repository: MusicRepository
) {
    suspend operator fun invoke(
        username: String,
        imageKind: String,
        period: String,
        limit: Int,
        forceRefresh: Boolean = false
    ): NetworkResult<List<MusicImage>> {
        if (username.isBlank()) {
            return NetworkResult.Error(
                com.jfcardenas.musicwall.data.ErrorType.UNKNOWN,
                "El usuario de Last.fm no está configurado"
            )
        }
        return repository.getImages(username, imageKind, period, limit, forceRefresh)
    }

    suspend fun artistCatalogAlbums(
        artists: List<String>,
        limit: Int,
        forceRefresh: Boolean = false
    ): NetworkResult<List<MusicImage>> {
        val cleaned = artists.map { it.trim() }.filter { it.isNotEmpty() }.distinct()
        if (cleaned.isEmpty()) {
            return NetworkResult.Error(
                com.jfcardenas.musicwall.data.ErrorType.UNKNOWN,
                "Elige al menos un artista para explorar"
            )
        }
        return repository.getArtistCatalogAlbums(cleaned, limit, forceRefresh)
    }
}
