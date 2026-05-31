package com.jfcardenas.musicwall.data

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CoverUpdateBus @Inject constructor() {
    data class CoverUpdate(val artist: String, val album: String, val url: String) {
        val key get() = "${artist}::${album}".lowercase()
    }

    private val _updates = MutableSharedFlow<CoverUpdate>(extraBufferCapacity = 16)
    val updates: SharedFlow<CoverUpdate> = _updates.asSharedFlow()

    suspend fun emit(update: CoverUpdate) = _updates.emit(update)
}
