package com.jfcardenas.musicwall.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jfcardenas.musicwall.data.local.db.dao.FavoriteAlbumDao
import com.jfcardenas.musicwall.data.local.db.entity.FavoriteAlbum
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val dao: FavoriteAlbumDao,
) : ViewModel() {

    var albums by mutableStateOf<List<FavoriteAlbum>>(emptyList())
        private set

    init { load() }

    fun load() {
        viewModelScope.launch {
            albums = dao.getAll()
        }
    }

    fun remove(id: String) {
        viewModelScope.launch {
            dao.delete(id)
            albums = albums.filter { it.id != id }
        }
    }
}
