package com.example.golden_rose_apk.model

data class PlayerCard(
    val uuid: String,
    val displayName: String,
    val smallArt: String?,
    val wideArt: String?,
    val largeArt: String?
)

data class PlayerTitle(
    val uuid: String,
    val displayName: String
)
