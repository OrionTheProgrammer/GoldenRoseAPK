package com.example.golden_rose_apk.config

object ValorantApi {
    const val BASE_API = "https://valorant-api.com/v1/"
    const val BASE_MEDIA = "https://media.valorant-api.com/"

    fun weaponImageUrl(weaponId: String): String {
        return "${BASE_MEDIA}weapons/$weaponId/displayicon.png"
    }

    fun weaponVideoUrl(weaponId: String): String {
        return "${BASE_MEDIA}weapons/$weaponId/streamedvideo.mp4"
    }

    fun weaponSkinImageUrl(skinId: String): String {
        return "${BASE_MEDIA}weaponskins/$skinId/displayicon.png"
    }
}
