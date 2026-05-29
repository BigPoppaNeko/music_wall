package com.jfcardenas.musicwall.domain.repository

import com.jfcardenas.musicwall.data.NetworkResult
import com.jfcardenas.musicwall.domain.model.MusicImage

interface MusicRepository {
    suspend fun getImages(
        username: String,
        imageKind: String,
        period: String,
        limit: Int,
        forceRefresh: Boolean = false
    ): NetworkResult<List<MusicImage>>
}
