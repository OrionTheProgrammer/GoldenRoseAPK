package com.example.golden_rose_apk.repository

import android.content.Context
import android.util.Log
import com.example.golden_rose_apk.config.ValorantApi
import com.example.golden_rose_apk.model.ProductFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Repository encargado de cargar el catálogo de skins desde la API oficial de Valorant.
 *
 * Esta capa se responsabiliza de:
 * - Consultar los endpoints remotos.
 * - Seleccionar una imagen representativa por skin.
 * - Normalizar tier y precio para el modelo de UI.
 */
class LocalProductRepository(private val context: Context) {
    private val TAG = "LocalProductRepository"

    private val skinsApi: ValorantSkinsApi by lazy {
        Retrofit.Builder()
            .baseUrl(ValorantApi.BASE_API)
            .client(OkHttpClient.Builder().build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ValorantSkinsApi::class.java)
    }

    /**
     * Obtiene el listado de skins y las transforma a [ProductFirestore] para consumo en la UI.
     */
    suspend fun loadProducts(): List<ProductFirestore> = withContext(Dispatchers.IO) {
        runCatching {
            val skins = skinsApi.getWeaponSkins().data
            Log.d(TAG, "Skins cargadas desde API: ${skins.size}")
            skins.mapNotNull { skin ->
                val name = skin.displayName?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                val image = resolveSkinImage(skin) ?: return@mapNotNull null
                val tierLabel = resolveTierLabel(skin.contentTierUuid)
                ProductFirestore(
                    id = skin.uuid,
                    name = name,
                    price = resolvePriceForTier(tierLabel),
                    type = "Skin de arma",
                    category = tierLabel,
                    image = image,
                    desc = "Skin $name de tier $tierLabel."
                )
            }
        }.getOrElse { error ->
            Log.e(TAG, "Error cargando skins desde Valorant API", error)
            emptyList()
        }
    }

    /**
     * Selecciona la mejor imagen disponible para una skin.
     * Prioridad: displayIcon > levels.displayIcon > chromas.displayIcon.
     */
    private fun resolveSkinImage(skin: ValorantSkin): String? {
        val directIcon = skin.displayIcon?.takeIf { it.isNotBlank() }
        if (directIcon != null) return directIcon

        val levelIcon = skin.levels
            ?.firstNotNullOfOrNull { it.displayIcon?.takeIf { icon -> icon.isNotBlank() } }
        if (levelIcon != null) return levelIcon

        return skin.chromas
            ?.firstNotNullOfOrNull { it.displayIcon?.takeIf { icon -> icon.isNotBlank() } }
    }

    /**
     * Traduce el UUID del tier a una etiqueta legible para UI.
     */
    private fun resolveTierLabel(contentTierUuid: String?): String {
        val tierId = contentTierUuid?.lowercase() ?: return "Desconocido"
        return TIER_LABELS_BY_UUID[tierId] ?: "Desconocido"
    }

    /**
     * Estima un precio de referencia según el tier (usado solo para catálogo visual).
     */
    private fun resolvePriceForTier(tierLabel: String): Double {
        return when (tierLabel.lowercase()) {
            "select" -> 875.0
            "deluxe" -> 1275.0
            "premium" -> 1775.0
            "exclusive" -> 2475.0
            "ultra" -> 2975.0
            else -> 0.0
        }
    }
}

private interface ValorantSkinsApi {
    @GET("weapons/skins")
    suspend fun getWeaponSkins(): ValorantSkinsResponse

    @GET("weapons/skins/{weaponSkinUuid}")
    suspend fun getWeaponSkin(@Path("weaponSkinUuid") weaponSkinUuid: String): ValorantSkinResponse
}

private data class ValorantSkinsResponse(
    val data: List<ValorantSkin>
)

private data class ValorantSkinResponse(
    val data: ValorantSkin
)

private data class ValorantSkin(
    val uuid: String,
    val displayName: String?,
    val displayIcon: String?,
    val contentTierUuid: String?,
    val levels: List<ValorantSkinLevel>?,
    val chromas: List<ValorantSkinChroma>?
)

private data class ValorantSkinLevel(
    val displayIcon: String?
)

private data class ValorantSkinChroma(
    val displayIcon: String?
)

private val TIER_LABELS_BY_UUID = mapOf(
    "5a629df4-4765-0214-bd40-fbb96542941f" to "Select",
    "0cebb8be-46d7-c12a-d306-e9907bfc5a25" to "Deluxe",
    "60bca009-4182-7998-dee7-b8a2558dc369" to "Premium",
    "e046854e-406c-37f4-6607-19a9ba8426fc" to "Exclusive",
    "12683d76-48d7-84a3-4e09-6985794f0445" to "Ultra"
)
