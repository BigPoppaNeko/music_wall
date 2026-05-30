package com.jfcardenas.musicwall.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jfcardenas.musicwall.data.local.db.dao.MuralDao
import com.jfcardenas.musicwall.data.local.db.entity.MuralRecord
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class MuralHistoryViewModel @Inject constructor(
    private val muralDao: MuralDao,
) : ViewModel() {

    var murals by mutableStateOf<List<MuralRecord>>(emptyList())
        private set

    var isLoading by mutableStateOf(true)
        private set

    init { load() }

    private fun load() {
        viewModelScope.launch {
            murals = muralDao.getAll()
            isLoading = false
        }
    }

    fun delete(mural: MuralRecord) {
        viewModelScope.launch(Dispatchers.IO) {
            muralDao.delete(mural)
            File(mural.filePath).delete()
            murals = murals.filter { it.id != mural.id }
        }
    }
}
