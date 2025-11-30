package com.denoise.denoiseapp.ui.admin

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.denoise.denoiseapp.data.repository.UserRepository
import com.denoise.denoiseapp.domain.model.Rol
import com.denoise.denoiseapp.domain.model.Usuario
import com.denoise.denoiseapp.ui.components.MinimalTopBar
import kotlinx.coroutines.launch

// --- VIEWMODEL LOCAL PARA ADMIN (CRUD) ---
class AdminViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = UserRepository(application)

    var users = mutableStateOf<List<Usuario>>(emptyList())
        private set

    init {
        loadUsers()
    }

    fun loadUsers() {
        viewModelScope.launch {
            users.value = repo.getAllUsers()
        }
    }

    fun toggleRole(userId: String) {
        viewModelScope.launch {
            repo.toggleAdminRole(userId)
            loadUsers()
        }
    }

    fun deleteUser(userId: String) {
        viewModelScope.launch {
            repo.deleteUser(userId)
            loadUsers()
        }
    }

    fun saveUser(id: String?, nombre: String, email: String, pass: String) {
        viewModelScope.launch {
            if (id == null) {
                repo.register(nombre, email, pass)
            } else {
                val original = repo.getAllUsers().find { it.id == id }
                if (original != null) {
                    val finalPass = if (pass.isBlank()) original.passwordHash else pass
                    val updatedUser = original.copy(
                        nombre = nombre,
                        email = email,
                        passwordHash = finalPass
                    )
                    repo.updateUser(updatedUser)
                }
            }
            loadUsers()
        }
    }
}

// --- PANTALLA (UI) ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUsersScreen() {
    val vm: AdminViewModel = viewModel()
    val users by vm.users

    var showDialog by remember { mutableStateOf(false) }
    var editingUser by remember { mutableStateOf<Usuario?>(null) }

    Scaffold(
        // CORRECCIÓN: Fondo adaptable al tema
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { MinimalTopBar("Gestión de Usuarios") },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingUser = null
                    showDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Usuario")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(users) { user ->
                UserCard(
                    user = user,
                    onToggleRole = { vm.toggleRole(user.id) },
                    onEdit = {
                        editingUser = user
                        showDialog = true
                    },
                    onDelete = { vm.deleteUser(user.id) }
                )
            }

            item { Spacer(Modifier.height(80.dp)) }
        }

        if (showDialog) {
            UserDialog(
                user = editingUser,
                onDismiss = { showDialog = false },
                onSave = { name, email, pass ->
                    vm.saveUser(editingUser?.id, name, email, pass)
                    showDialog = false
                }
            )
        }
    }
}

@Composable
fun UserCard(
    user: Usuario,
    onToggleRole: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        // CORRECCIÓN: Usamos surfaceContainer para que sea legible en ambos modos
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp) // Diseño plano moderno
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // CORRECCIÓN: Colores de texto semánticos (onSurface)
                Text(
                    user.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    user.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))

                AssistChip(
                    onClick = onToggleRole,
                    label = { Text(user.rol.name) },
                    leadingIcon = {
                        Icon(
                            if (user.rol == Rol.ADMIN) Icons.Default.AdminPanelSettings else Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (user.rol == Rol.ADMIN) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (user.rol == Rol.ADMIN) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                        labelColor = if (user.rol == Rol.ADMIN) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                    )
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun UserDialog(
    user: Usuario?,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(user?.nombre ?: "") }
    var email by remember { mutableStateOf(user?.email ?: "") }
    var password by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        // CORRECCIÓN: Colores semánticos para el diálogo
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        title = { Text(if (user == null) "Nuevo Usuario" else "Editar Usuario") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(if (user == null) "Contraseña" else "Nueva Contraseña (Opcional)") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    placeholder = { if (user != null) Text("Dejar vacía para mantener actual") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, email, password) },
                enabled = name.isNotBlank() && email.isNotBlank() && (user != null || password.isNotBlank())
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}