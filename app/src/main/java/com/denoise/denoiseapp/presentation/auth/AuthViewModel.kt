package com.denoise.denoiseapp.presentation.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.denoise.denoiseapp.core.util.SessionManager
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

    // Inicializamos el repositorio con el contexto de la aplicación
    private val repo = UserRepository(app)
    private val session = SessionManager(app)

    // Estados observables para la UI (Campos de texto y Carga)
    var email = androidx.compose.runtime.mutableStateOf("")
    var password = androidx.compose.runtime.mutableStateOf("")
    var name = androidx.compose.runtime.mutableStateOf("")
    var isLoading = androidx.compose.runtime.mutableStateOf(false)

    // Canal para comunicar eventos únicos a la UI
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
                    session.saveUser(user)
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
                    session.saveUser(user)
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
        viewModelScope.launch {
            _events.send(AuthEvent.ShowError(msg))
        }
    }
}