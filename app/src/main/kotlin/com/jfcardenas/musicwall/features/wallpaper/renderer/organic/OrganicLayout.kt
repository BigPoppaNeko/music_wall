package com.jfcardenas.musicwall.features.wallpaper.renderer.organic

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

data class OrganicSlot(
    val id:           Int,
    val x:            Float,
    val y:            Float,
    val width:        Float,
    val height:       Float,
    val rotation:     Float = 0f,
    val lightingZone: List<Int> = listOf(128, 128, 128),
)

data class OrganicLightingMap(
    val type:            String = "gradient",
    val mainLightColor:  String = "#FFFFFF",
    val ambientColor:    String = "#333333",
    val shadowDirection: String = "bottom-right",
)

data class OrganicLayout(
    val id:          String,
    val genre:       String = "mixed",
    val totalSlots:  Int    = 0,
    val slots:       List<OrganicSlot>    = emptyList(),
    val lightingMap: OrganicLightingMap   = OrganicLightingMap(),
) {
    companion object {
        private val gson = Gson()

        fun fromJson(json: String): OrganicLayout = gson.fromJson(json, OrganicLayout::class.java)
    }
}
