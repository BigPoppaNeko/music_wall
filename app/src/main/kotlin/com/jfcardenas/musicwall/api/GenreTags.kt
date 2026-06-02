package com.jfcardenas.musicwall.api

private val BLOCKED_GENRE_TAGS = setOf(
    "seen live",
    "favorite",
    "favorites",
    "owned",
    "wishlist",
    "my tags",
    "all",
    "other",
    "library",
    "uploaded",
)

fun browseableGenreTags(tags: List<UserTag>): List<String> =
    tags.map { it.name }
        .filter { tag ->
            val key = tag.trim().lowercase()
            key.isNotBlank() && key != "mixed" && key !in BLOCKED_GENRE_TAGS
        }
        .distinct()

fun isBrowseableGenreTag(tag: String): Boolean {
    val key = tag.trim().lowercase()
    return key.isNotBlank() && key != "mixed" && key !in BLOCKED_GENRE_TAGS
}
