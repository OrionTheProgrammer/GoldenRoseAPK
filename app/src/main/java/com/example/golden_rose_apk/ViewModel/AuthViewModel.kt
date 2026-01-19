package com.example.golden_rose_apk.ViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.golden_rose_apk.repository.LocalUser
import com.example.golden_rose_apk.repository.LocalUserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepository = LocalUserRepository(application)

    private val _isLoggedIn = MutableStateFlow<Boolean>(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    private val _userId = MutableStateFlow<String?>(null)
    val userId: StateFlow<String?> = _userId

    private val _username = MutableStateFlow<String?>(null)
    val username: StateFlow<String?> = _username

    private val _email = MutableStateFlow<String?>(null)
    val email: StateFlow<String?> = _email


    init {
        refreshSession()
    }

    private fun refreshSession() {
        viewModelScope.launch {
            val currentUser = userRepository.getCurrentUser()
            _isLoggedIn.value = currentUser != null
            _userId.value = currentUser?.id
            _username.value = currentUser?.username
            _email.value = currentUser?.email
        }
    }

    fun login(user: LocalUser) {
        viewModelScope.launch {
            userRepository.setCurrentUser(user)
            refreshSession()
        }
    }

    fun logout() {
        viewModelScope.launch {
            userRepository.clearSession()
            refreshSession()
        }
    }
}

class AuthViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
