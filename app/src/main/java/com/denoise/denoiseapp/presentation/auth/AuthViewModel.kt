package com.denoise.denoiseapp.presentation.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.denoise.denoiseapp.core.util.SessionManager
import com.denoise.denoiseapp.data.remote.firestore.FirestoreUsuariosRepository
import com.denoise.denoiseapp.data.repository.UserRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

// Eventos que la UI debe manejar (Navegación o Errores)
sealed class AuthEvent {
    object NavigateToHome : AuthEvent()
    data class ShowError(val msg: String) : AuthEvent()
}

class AuthViewModel(app: Application) : AndroidViewModel(app) {

    // Repositorio y sesión
    private val repo = UserRepository(app)
    private val session = SessionManager(app)

    // Estados UI
    var email = androidx.compose.runtime.mutableStateOf("")
    var password = androidx.compose.runtime.mutableStateOf("")
    var name = androidx.compose.runtime.mutableStateOf("")
    var isLoading = androidx.compose.runtime.mutableStateOf(false)

    // Eventos one-shot
    private val _events = Channel<AuthEvent>()
    val events = _events.receiveAsFlow()

    fun onLogin() {
        if (email.value.isBlank() || password.value.isBlank()) {
            sendError("Por favor, completa todos los campos")
            return
        }

        viewModelScope.launch {
            isLoading.value = true
            try {
                val user = repo.login(email.value, password.value)
                isLoading.value = false

                if (user != null) {
                    // Guardar en sesión
                    session.saveUser(user)

                    // --- Firestore: upsert usuario (best-effort) ---
                    runCatching {
                        val uid = user.id.ifEmpty { user.email }
                        val rolStr: String? = user.rol?.toString() ?: null
                        FirestoreUsuariosRepository.upsertFromFields(
                            id = uid,
                            nombre = user.nombre,
                            email = user.email,
                            rol = rolStr,
                            extras = mapOf(
                                "lastLoginMillis" to System.currentTimeMillis(),
                                "event" to "login"
                            )
                        )
                    }

                    _events.send(AuthEvent.NavigateToHome)
                } else {
                    sendError("Credenciales incorrectas")
                }
            } catch (e: Exception) {
                isLoading.value = false
                sendError("Error al iniciar sesión: ${e.message}")
            }
        }
    }

    fun onRegister() {
        if (email.value.isBlank() || password.value.isBlank() || name.value.isBlank()) {
            sendError("Por favor, completa todos los campos")
            return
        }

        viewModelScope.launch {
            isLoading.value = true
            try {
                val user = repo.register(name.value, email.value, password.value)
                isLoading.value = false

                if (user != null) {
                    // Guardar en sesión
                    session.saveUser(user)

                    // --- Firestore: creación/actualización usuario (best-effort) ---
                    runCatching {
                        val uid = user.id.ifEmpty { user.email }
                        val rolStr: String? = user.rol?.toString() ?: null
                        FirestoreUsuariosRepository.upsertFromFields(
                            id = uid,
                            nombre = user.nombre,
                            email = user.email,
                            rol = rolStr,
                            extras = mapOf(
                                "createdAtMillis" to System.currentTimeMillis(),
                                "event" to "register"
                            )
                        )
                    }

                    _events.send(AuthEvent.NavigateToHome)
                } else {
                    sendError("El email ya está registrado")
                }
            } catch (e: Exception) {
                isLoading.value = false
                sendError("Error al registrarse: ${e.message}")
            }
        }
    }

    private fun sendError(msg: String) {
        viewModelScope.launch { _events.send(AuthEvent.ShowError(msg)) }
    }
}
