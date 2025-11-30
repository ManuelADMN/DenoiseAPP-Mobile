package com.denoise.denoiseapp.core.util

import android.content.Context
import android.content.SharedPreferences
import com.denoise.denoiseapp.domain.model.Rol
import com.denoise.denoiseapp.domain.model.Usuario
import com.google.gson.Gson

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("denoise_session", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveUser(usuario: Usuario) {
        val json = gson.toJson(usuario)
        prefs.edit().putString("current_user", json).apply()
    }

    fun getUser(): Usuario? {
        val json = prefs.getString("current_user", null)
        return if (json != null) {
            gson.fromJson(json, Usuario::class.java)
        } else {
            null
        }
    }

    fun logout() {
        prefs.edit().clear().apply()
    }

    fun isLoggedIn(): Boolean = getUser() != null

    fun isAdmin(): Boolean = getUser()?.rol == Rol.ADMIN
}