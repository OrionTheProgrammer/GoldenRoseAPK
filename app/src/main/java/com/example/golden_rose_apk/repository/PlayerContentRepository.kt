package com.example.golden_rose_apk.repository

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import com.example.golden_rose_apk.config.ValorantApi
import com.example.golden_rose_apk.model.PlayerCard
import com.example.golden_rose_apk.model.PlayerCardFormat
import com.example.golden_rose_apk.model.PlayerTitle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

class PlayerContentRepository(context: Context) {
    private val appContext = context.applicationContext
    private val storage = PlayerContentStorage(appContext)

    private val contentApi: ValorantPlayerContentApi by lazy {
        Retrofit.Builder()
            .baseUrl(ValorantApi.BASE_API)
            .client(OkHttpClient.Builder().build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ValorantPlayerContentApi::class.java)
    }

    suspend fun loadPlayerCards(): List<PlayerCard> = withContext(Dispatchers.IO) {
        runCatching {
            contentApi.getPlayerCards().data
                .mapNotNull { card ->
                    val name = card.displayName?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                    PlayerCard(
                        uuid = card.uuid,
                        displayName = name,
                        smallArt = card.smallArt,
                        wideArt = card.wideArt,
                        largeArt = card.largeArt,
                        themeUuid = card.themeUuid,
                        categoryLabel = resolveCardCategory(card.themeUuid)
                    )
                }
        }.getOrElse { error ->
            Log.e("PlayerContentRepo", "Error cargando player cards", error)
            emptyList()
        }
    }

    suspend fun loadPlayerTitles(): List<PlayerTitle> = withContext(Dispatchers.IO) {
        runCatching {
            contentApi.getPlayerTitles().data
                .mapNotNull { title ->
                    val name = title.displayName?.takeIf { it.isNotBlank() }
                        ?: title.titleText?.takeIf { it.isNotBlank() }
                        ?: return@mapNotNull null
                    PlayerTitle(uuid = title.uuid, displayName = name)
                }
        }.getOrElse { error ->
            Log.e("PlayerContentRepo", "Error cargando player titles", error)
            emptyList()
        }
    }

    fun getPurchasedCardIds(): Set<String> = storage.getPurchasedCardIds()

    fun getPurchasedTitleIds(): Set<String> = storage.getPurchasedTitleIds()

    fun purchaseCard(card: PlayerCard) {
        storage.purchaseCard(card.uuid)
    }

    fun purchaseTitle(title: PlayerTitle) {
        storage.purchaseTitle(title.uuid)
    }

    fun getEquippedTitleId(): String? = storage.getEquippedTitleId()

    fun setEquippedTitleId(titleId: String?) {
        storage.setEquippedTitleId(titleId)
    }

    fun downloadPlayerCard(card: PlayerCard, format: PlayerCardFormat): Boolean {
        val imageUrl = resolveCardImageUrl(card, format) ?: return false
        return runCatching {
            val request = DownloadManager.Request(Uri.parse(imageUrl))
                .setTitle(card.displayName)
                .setDescription("Descargando tarjeta de jugador")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    "valorant_card_${card.uuid}.png"
                )
            val downloadManager = appContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request)
            true
        }.getOrDefault(false)
    }

    private fun resolveCardImageUrl(card: PlayerCard, format: PlayerCardFormat): String? {
        return when (format) {
            PlayerCardFormat.SMALL -> card.smallArt ?: card.wideArt ?: card.largeArt
            PlayerCardFormat.WIDE -> card.wideArt ?: card.largeArt ?: card.smallArt
            PlayerCardFormat.LARGE -> card.largeArt ?: card.wideArt ?: card.smallArt
        }
    }

    private fun resolveCardCategory(themeUuid: String?): String {
        return themeUuid?.takeIf { it.isNotBlank() }?.let { uuid ->
            "Colección ${uuid.take(4).uppercase()}"
        } ?: "General"
    }
}

private class PlayerContentStorage(context: Context) {
    private val prefs = context.getSharedPreferences("player_content_prefs", Context.MODE_PRIVATE)

    fun getPurchasedCardIds(): Set<String> =
        prefs.getStringSet("purchased_cards", emptySet()) ?: emptySet()

    fun getPurchasedTitleIds(): Set<String> =
        prefs.getStringSet("purchased_titles", emptySet()) ?: emptySet()

    fun purchaseCard(cardId: String) {
        val updated = getPurchasedCardIds().toMutableSet().apply { add(cardId) }
        prefs.edit().putStringSet("purchased_cards", updated).apply()
    }

    fun purchaseTitle(titleId: String) {
        val updated = getPurchasedTitleIds().toMutableSet().apply { add(titleId) }
        prefs.edit().putStringSet("purchased_titles", updated).apply()
    }

    fun getEquippedTitleId(): String? = prefs.getString("equipped_title", null)

    fun setEquippedTitleId(titleId: String?) {
        prefs.edit().putString("equipped_title", titleId).apply()
    }
}

private interface ValorantPlayerContentApi {
    @GET("playercards")
    suspend fun getPlayerCards(): PlayerCardsResponse

    @GET("playertitles")
    suspend fun getPlayerTitles(): PlayerTitlesResponse
}

private data class PlayerCardsResponse(
    val data: List<PlayerCardResponse>
)

private data class PlayerTitlesResponse(
    val data: List<PlayerTitleResponse>
)

private data class PlayerCardResponse(
    val uuid: String,
    val displayName: String?,
    val smallArt: String?,
    val wideArt: String?,
    val largeArt: String?,
    val themeUuid: String?
)

private data class PlayerTitleResponse(
    val uuid: String,
    val displayName: String?,
    val titleText: String?
)
