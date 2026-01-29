package com.example.golden_rose_apk.ViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.golden_rose_apk.model.PlayerCard
import com.example.golden_rose_apk.model.PlayerTitle
import com.example.golden_rose_apk.repository.PlayerContentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PlayerContentViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = PlayerContentRepository(application)

    private val _playerCards = MutableStateFlow<List<PlayerCard>>(emptyList())
    val playerCards: StateFlow<List<PlayerCard>> = _playerCards

    private val _playerTitles = MutableStateFlow<List<PlayerTitle>>(emptyList())
    val playerTitles: StateFlow<List<PlayerTitle>> = _playerTitles

    private val _purchasedCardIds = MutableStateFlow<Set<String>>(emptySet())
    val purchasedCardIds: StateFlow<Set<String>> = _purchasedCardIds

    private val _purchasedTitleIds = MutableStateFlow<Set<String>>(emptySet())
    val purchasedTitleIds: StateFlow<Set<String>> = _purchasedTitleIds

    private val _equippedTitleId = MutableStateFlow<String?>(null)
    val equippedTitleId: StateFlow<String?> = _equippedTitleId

    init {
        refreshContent()
        refreshLocalState()
    }

    fun refreshContent() {
        viewModelScope.launch {
            _playerCards.value = repository.loadPlayerCards()
            _playerTitles.value = repository.loadPlayerTitles()
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
        return repository.downloadPlayerCard(card)
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
