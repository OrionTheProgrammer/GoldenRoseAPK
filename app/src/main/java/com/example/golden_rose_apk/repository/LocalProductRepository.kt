package com.example.golden_rose_apk.repository

import android.content.Context
import android.util.Log
import com.example.golden_rose_apk.config.ValorantApi
import com.example.golden_rose_apk.model.ProductFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

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
    private val TAG = "LocalProductRepository"

    private val weaponsApi: ValorantWeaponsApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://valorant-api.com/v1/")
            .client(OkHttpClient.Builder().build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ValorantWeaponsApi::class.java)
    }

    suspend fun loadProducts(): List<ProductFirestore> = withContext(Dispatchers.IO) {
        try {
            val assetManager = context.assets
            Log.d(TAG, "Listado assets: ${assetManager.list("")?.joinToString(", ")}")
            val json = assetManager.open("products.json").bufferedReader().use { it.readText() }
            val type = object : TypeToken<List<LocalProductSeed>>() {}.type
            val seeds: List<LocalProductSeed> = try {
                val reader = com.google.gson.stream.JsonReader(java.io.StringReader(json)).apply { isLenient = true }
                gson.fromJson<List<LocalProductSeed>>(reader, type) ?: emptyList()
            } catch (e: Exception) {
                Log.w(TAG, "Gson lenient parse falló, intentando parseo estricto", e)
                gson.fromJson(json, type) ?: emptyList()
            }
            Log.d(TAG, "products.json encontrado. Seeds parseadas: ${seeds.size}")

            val weaponsById = fetchWeaponsById()

            return@withContext seeds.map { seed ->
                val resolvedImage = resolveImage(seed, weaponsById)
                Log.d(TAG, "Seed: ${seed.id}, imageRes: ${seed.imageRes}, resolvedImage: $resolvedImage")
                ProductFirestore(
                    id = seed.id,
                    name = seed.name,
                    price = seed.price,
                    type = seed.type,
                    category = seed.category,
                    image = resolvedImage,
                    desc = seed.desc
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error leyendo assets/products.json", e)
            return@withContext emptyList<ProductFirestore>()
        }
    }

    private suspend fun fetchWeaponsById(): Map<String, String> {
        return runCatching {
            weaponsApi.getWeapons().data
                .mapNotNull { weapon ->
                    val icon = weapon.displayIcon?.takeIf { it.isNotBlank() }
                    if (icon != null) weapon.uuid to icon else null
                }
                .toMap()
        }.getOrElse { emptyMap() }
    }

    private fun resolveImage(seed: LocalProductSeed, weaponsById: Map<String, String>): String {
        val remoteUrl = seed.imageUrl?.takeIf { it.isNotBlank() }
        if (remoteUrl != null) {
            return remoteUrl
        }

        val valorantId = seed.valorantWeaponId?.takeIf { it.isNotBlank() }
        if (valorantId != null) {
            return weaponsById[valorantId] ?: ValorantApi.weaponImageUrl(valorantId)
        }

        return buildResourceUri(seed.imageRes)
    }

    private fun buildResourceUri(resourceName: String): String {
        val resourceId = context.resources.getIdentifier(resourceName, "drawable", context.packageName)
        return if (resourceId != 0) {
            "android.resource://${context.packageName}/drawable/$resourceName"
        } else {
            // Fallback: look for the image inside assets/valorant_skins/<resourceName>.png
            // Coil and other image loaders can load URIs like file:///android_asset/...
            val assetPath = "file:///android_asset/valorant_skins/$resourceName.png"
            Log.d(TAG, "Drawable no encontrado para '$resourceName', fallback a: $assetPath")
            assetPath
        }
    }
}

private interface ValorantWeaponsApi {
    @GET("weapons")
    suspend fun getWeapons(): ValorantWeaponsResponse
}

private data class ValorantWeaponsResponse(
    val data: List<ValorantWeapon>
)

private data class ValorantWeapon(
    val uuid: String,
    val displayIcon: String?
)
