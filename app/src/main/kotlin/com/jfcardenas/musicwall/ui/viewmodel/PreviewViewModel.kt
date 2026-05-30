package com.jfcardenas.musicwall.ui.viewmodel

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jfcardenas.musicwall.data.local.db.dao.MuralDao
import com.jfcardenas.musicwall.data.local.db.entity.MuralRecord
import com.jfcardenas.musicwall.service.CollageWallpaper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class PreviewViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val muralDao: MuralDao,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val styleId: String = savedStateHandle.get<String>("styleId") ?: "ecosystem"

    val collagePath: String?
        get() = context
            .getSharedPreferences(CollageWallpaper.PREFS_NAME, Context.MODE_PRIVATE)
            .getString(CollageWallpaper.PREF_COLLAGE_PATH, null)
            ?.let { if (File(it).exists()) it else null }

    fun applyWallpaper() {
        viewModelScope.launch {
            val path = collagePath
            if (path != null) {
                withContext(Dispatchers.IO) {
                    val permanent = copyToPermanent(path)
                    muralDao.insert(MuralRecord(styleId = styleId, filePath = permanent))
                }
            }
            val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
                putExtra(
                    WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                    ComponentName(context, CollageWallpaper::class.java),
                )
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    fun shareWallpaper() {
        val path = collagePath
        val intent = if (path != null) {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", File(path))
            Intent(Intent.ACTION_SEND).apply {
                type = "image/jpeg"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, "Mi mural musical hecho con MusicWall 🎵")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        } else {
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "Mi mural musical hecho con MusicWall 🎵")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
        context.startActivity(Intent.createChooser(intent, "Compartir mural").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    private fun copyToPermanent(sourcePath: String): String {
        val dir  = File(context.filesDir, "murals").also { it.mkdirs() }
        val dest = File(dir, "mural_${System.currentTimeMillis()}.png")
        File(sourcePath).copyTo(dest, overwrite = true)
        return dest.absolutePath
    }
}
