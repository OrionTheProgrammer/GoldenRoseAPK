package com.example.golden_rose_apk.utils

import java.util.Locale

fun Double.formatPrice(): String {
    return String.format(Locale("es", "CL"), "%,.0f", this)
}
