package com.example.golden_rose_apk.ViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.golden_rose_apk.model.PlayerCard
import com.example.golden_rose_apk.model.PlayerCardFormat
import com.example.golden_rose_apk.model.PlayerTitle
import com.example.golden_rose_apk.repository.PlayerContentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class PlayerContentViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = PlayerContentRepository(application)

    private val _playerCards = MutableStateFlow<List<PlayerCard>>(emptyList())
    val playerCards: StateFlow<List<PlayerCard>> = _playerCards

    private val _playerTitles = MutableStateFlow<List<PlayerTitle>>(emptyList())
    val playerTitles: StateFlow<List<PlayerTitle>> = _playerTitles

    private val _availableTitles = MutableStateFlow<List<PlayerTitle>>(emptyList())
    val availableTitles: StateFlow<List<PlayerTitle>> = _availableTitles

    private val _purchasedCardIds = MutableStateFlow<Set<String>>(emptySet())
    val purchasedCardIds: StateFlow<Set<String>> = _purchasedCardIds

    private val _purchasedTitleIds = MutableStateFlow<Set<String>>(emptySet())
    val purchasedTitleIds: StateFlow<Set<String>> = _purchasedTitleIds

    private val _equippedTitleId = MutableStateFlow<String?>(null)
    val equippedTitleId: StateFlow<String?> = _equippedTitleId

    private val _selectedCardFormat = MutableStateFlow(PlayerCardFormat.WIDE)
    val selectedCardFormat: StateFlow<PlayerCardFormat> = _selectedCardFormat

    private val _selectedCardCategory = MutableStateFlow<String>("Todas")
    val selectedCardCategory: StateFlow<String> = _selectedCardCategory

    private val _titleStoreUpdates = MutableSharedFlow<Unit>()
    val titleStoreUpdates: SharedFlow<Unit> = _titleStoreUpdates

    private var titleRotationIndex = 0

    init {
        refreshContent()
        refreshLocalState()
        startTitleRotation()
    }

    fun refreshContent() {
        viewModelScope.launch {
            _playerCards.value = repository.loadPlayerCards()
            _playerTitles.value = repository.loadPlayerTitles()
            updateAvailableTitles(reset = true)
        }
    }

    fun refreshLocalState() {
        _purchasedCardIds.value = repository.getPurchasedCardIds()
        _purchasedTitleIds.value = repository.getPurchasedTitleIds()
        _equippedTitleId.value = repository.getEquippedTitleId()
    }

    fun purchaseCard(card: PlayerCard) {
        repository.purchaseCard(card)
        refreshLocalState()
    }

    fun purchaseTitle(title: PlayerTitle) {
        repository.purchaseTitle(title)
        refreshLocalState()
    }

    fun equipTitle(titleId: String?) {
        repository.setEquippedTitleId(titleId)
        refreshLocalState()
    }

    fun downloadPlayerCard(card: PlayerCard): Boolean {
        return repository.downloadPlayerCard(card, _selectedCardFormat.value)
    }

    fun setCardFormat(format: PlayerCardFormat) {
        _selectedCardFormat.value = format
    }

    fun setCardCategory(category: String) {
        _selectedCardCategory.value = category
    }

    private fun startTitleRotation() {
        viewModelScope.launch {
            while (true) {
                delay(TITLE_ROTATION_INTERVAL_MS)
                updateAvailableTitles(reset = false)
                _titleStoreUpdates.emit(Unit)
            }
        }
    }

    private fun updateAvailableTitles(reset: Boolean) {
        val titles = _playerTitles.value
        if (titles.isEmpty()) {
            _availableTitles.value = emptyList()
            return
        }
        val windowSize = TITLE_WINDOW_SIZE.coerceAtMost(titles.size)
        if (reset) {
            titleRotationIndex = 0
        } else {
            titleRotationIndex = (titleRotationIndex + windowSize) % titles.size
        }
        val rotated = (titles + titles).drop(titleRotationIndex).take(windowSize)
        _availableTitles.value = rotated
    }
}

class PlayerContentViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlayerContentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PlayerContentViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

private const val TITLE_ROTATION_INTERVAL_MS = 5 * 60 * 1000L
private const val TITLE_WINDOW_SIZE = 10
