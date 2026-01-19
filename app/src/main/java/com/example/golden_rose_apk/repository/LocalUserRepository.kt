package com.example.golden_rose_apk.repository

import android.content.Context
import com.example.golden_rose_apk.data.AppDatabase
import com.example.golden_rose_apk.data.UserEntity
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class LocalUser(
    val id: String,
    val username: String,
    val email: String,
    val password: String
)

class LocalUserRepository(private val context: Context) {
    private val userDao = AppDatabase.getInstance(context).userDao()
    private val sessionPrefs = context.getSharedPreferences("session_prefs", Context.MODE_PRIVATE)

    suspend fun registerUser(username: String, email: String, password: String): Result<LocalUser> {
        return withContext(Dispatchers.IO) {
            val existing = userDao.getUserByEmail(email)
            if (existing != null) {
                return@withContext Result.failure(IllegalStateException("El correo ya está registrado."))
            }

            val newUser = LocalUser(
                id = UUID.randomUUID().toString(),
                username = username,
                email = email,
                password = password
            )
            val entity = UserEntity(
                id = newUser.id,
                username = newUser.username,
                email = newUser.email,
                password = newUser.password,
                createdAt = System.currentTimeMillis()
            )
            userDao.insertUser(entity)
            Result.success(newUser)
        }
    }

    suspend fun login(email: String, password: String): Result<LocalUser> {
        return withContext(Dispatchers.IO) {
            val user = userDao.getUserByEmail(email)
            if (user != null && user.password == password) {
                Result.success(
                    LocalUser(
                        id = user.id,
                        username = user.username,
                        email = user.email,
                        password = user.password
                    )
                )
            } else {
                Result.failure(IllegalArgumentException("Correo o contraseña incorrectos."))
            }
        }
    }

    fun setCurrentUser(user: LocalUser) {
        sessionPrefs.edit()
            .putString("CURRENT_USER_ID", user.id)
            .putString("CURRENT_USER_NAME", user.username)
            .putString("CURRENT_USER_EMAIL", user.email)
            .apply()
    }

    fun clearSession() {
        sessionPrefs.edit()
            .remove("CURRENT_USER_ID")
            .remove("CURRENT_USER_NAME")
            .remove("CURRENT_USER_EMAIL")
            .apply()
    }

    suspend fun getCurrentUser(): LocalUser? {
        val id = sessionPrefs.getString("CURRENT_USER_ID", null) ?: return null
        return withContext(Dispatchers.IO) {
            userDao.getUserById(id)?.let {
                LocalUser(
                    id = it.id,
                    username = it.username,
                    email = it.email,
                    password = it.password
                )
            }
        }
    }

    fun getCurrentUserId(): String? = sessionPrefs.getString("CURRENT_USER_ID", null)

    fun getCurrentUsername(): String? = sessionPrefs.getString("CURRENT_USER_NAME", null)

    fun getCurrentEmail(): String? = sessionPrefs.getString("CURRENT_USER_EMAIL", null)

    suspend fun getUserById(userId: String): LocalUser? {
        return withContext(Dispatchers.IO) {
            userDao.getUserById(userId)?.let {
                LocalUser(
                    id = it.id,
                    username = it.username,
                    email = it.email,
                    password = it.password
                )
            }
        }
    }
}
