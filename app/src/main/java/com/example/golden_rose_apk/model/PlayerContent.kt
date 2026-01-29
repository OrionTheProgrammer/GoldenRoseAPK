package com.example.golden_rose_apk.model

enum class PlayerCardFormat {
    SMALL,
    WIDE,
    LARGE
}

data class PlayerCard(
    val uuid: String,
    val displayName: String,
    val smallArt: String?,
    val wideArt: String?,
    val largeArt: String?,
    val themeUuid: String?,
    val categoryLabel: String
)

data class PlayerTitle(
    val uuid: String,
    val displayName: String
)
