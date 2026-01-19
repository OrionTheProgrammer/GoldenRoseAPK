package com.example.golden_rose_apk.repository

import android.content.Context
import com.example.golden_rose_apk.config.ValorantApi
import com.example.golden_rose_apk.model.ProductFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

private data class LocalProductSeed(
    val id: String,
    val name: String,
    val price: Double,
    val type: String,
    val category: String,
    val imageRes: String,
    val desc: String,
    val imageUrl: String? = null,
    val valorantWeaponId: String? = null
)

class LocalProductRepository(private val context: Context) {
    private val gson = Gson()

    fun loadProducts(): List<ProductFirestore> {
        val json = context.assets.open("products.json").bufferedReader().use { it.readText() }
        val type = object : TypeToken<List<LocalProductSeed>>() {}.type
        val seeds: List<LocalProductSeed> = gson.fromJson(json, type) ?: emptyList()

        return seeds.map { seed ->
            ProductFirestore(
                id = seed.id,
                name = seed.name,
                price = seed.price,
                type = seed.type,
                category = seed.category,
                image = resolveImage(seed),
                desc = seed.desc
            )
        }
    }

    private fun resolveImage(seed: LocalProductSeed): String {
        val remoteUrl = seed.imageUrl?.takeIf { it.isNotBlank() }
        if (remoteUrl != null) {
            return remoteUrl
        }

        val valorantId = seed.valorantWeaponId?.takeIf { it.isNotBlank() }
        if (valorantId != null) {
            return ValorantApi.weaponImageUrl(valorantId)
        }

        return buildResourceUri(seed.imageRes)
    }

    private fun buildResourceUri(resourceName: String): String {
        val resourceId = context.resources.getIdentifier(resourceName, "drawable", context.packageName)
        return if (resourceId != 0) {
            "android.resource://${context.packageName}/drawable/$resourceName"
        } else {
            ""
        }
    }
}
