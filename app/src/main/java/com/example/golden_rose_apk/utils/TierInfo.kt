package com.example.golden_rose_apk.utils

import androidx.compose.ui.graphics.Color

// Funcion que mapea la etiqueta local a la informacion del Tier
data class TierInfo(val name: String, val color: Color)

fun getTierInfoFromLabel(label: String): TierInfo {
    return when (label.trim().lowercase()) {
        "select" -> TierInfo("Select", Color(0xFF4CAF50))
        "deluxe" -> TierInfo("Deluxe", Color(0xFF2196F3))
        "premium" -> TierInfo("Premium", Color(0xFF9C27B0))
        "exclusive" -> TierInfo("Exclusive", Color(0xFFFF9800))
        "ultra" -> TierInfo("Ultra", Color(0xFFFFEB3B))
        else -> TierInfo("Desconocido", Color.Gray)
    }
}
