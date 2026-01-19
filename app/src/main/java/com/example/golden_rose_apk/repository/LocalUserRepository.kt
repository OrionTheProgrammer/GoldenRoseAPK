package com.example.golden_rose_apk.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.util.UUID

data class LocalUser(
    val id: String,
    val username: String,
    val email: String,
    val password: String
)

class LocalUserRepository(private val context: Context) {
    private val gson = Gson()
    private val usersFile = File(context.filesDir, "users.json")
    private val sessionPrefs = context.getSharedPreferences("session_prefs", Context.MODE_PRIVATE)

    fun registerUser(username: String, email: String, password: String): Result<LocalUser> {
        val users = loadUsers()
        if (users.any { it.email.equals(email, ignoreCase = true) }) {
            return Result.failure(IllegalStateException("El correo ya está registrado."))
        }

        val newUser = LocalUser(
            id = UUID.randomUUID().toString(),
            username = username,
            email = email,
            password = password
        )
        users.add(newUser)
        saveUsers(users)
        return Result.success(newUser)
    }

    fun login(email: String, password: String): Result<LocalUser> {
        val user = loadUsers()
            .firstOrNull { it.email.equals(email, ignoreCase = true) && it.password == password }
        return if (user != null) {
            Result.success(user)
        } else {
            Result.failure(IllegalArgumentException("Correo o contraseña incorrectos."))
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

    fun getCurrentUser(): LocalUser? {
        val id = sessionPrefs.getString("CURRENT_USER_ID", null) ?: return null
        return loadUsers().firstOrNull { it.id == id }
    }

    fun getCurrentUserId(): String? = sessionPrefs.getString("CURRENT_USER_ID", null)

    fun getCurrentUsername(): String? = sessionPrefs.getString("CURRENT_USER_NAME", null)

    fun getCurrentEmail(): String? = sessionPrefs.getString("CURRENT_USER_EMAIL", null)

    private fun loadUsers(): MutableList<LocalUser> {
        if (!usersFile.exists()) return mutableListOf()
        val json = usersFile.readText()
        if (json.isBlank()) return mutableListOf()
        val type = object : TypeToken<List<LocalUser>>() {}.type
        return gson.fromJson<List<LocalUser>>(json, type)?.toMutableList() ?: mutableListOf()
    }

    private fun saveUsers(users: List<LocalUser>) {
        usersFile.writeText(gson.toJson(users))
    }
}
