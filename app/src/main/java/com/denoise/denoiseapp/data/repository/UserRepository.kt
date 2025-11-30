package com.denoise.denoiseapp.data.repository

import android.content.Context
import com.denoise.denoiseapp.data.local.db.AppDatabase
import com.denoise.denoiseapp.data.local.entity.toDomain
import com.denoise.denoiseapp.data.local.entity.toEntity
import com.denoise.denoiseapp.domain.model.Rol
import com.denoise.denoiseapp.domain.model.Usuario
import java.util.UUID

class UserRepository(context: Context) {

    private val dao = AppDatabase.getInstance(context).usuarioDao()

    suspend fun login(email: String, pass: String): Usuario? {
        val userEntity = dao.getByEmail(email) ?: return null
        // Verificación simple de contraseña (en producción usar hashing)
        return if (userEntity.passwordHash == pass) {
            userEntity.toDomain()
        } else {
            null
        }
    }

    suspend fun register(nombre: String, email: String, pass: String): Usuario? {
        if (dao.getByEmail(email) != null) return null // Ya existe

        val newUser = Usuario(
            id = UUID.randomUUID().toString(),
            email = email,
            nombre = nombre,
            passwordHash = pass,
            rol = Rol.USUARIO
        )
        dao.insert(newUser.toEntity())
        return newUser
    }

    // --- FUNCIONES DE ADMIN (CRUD REAL) ---

    suspend fun getAllUsers(): List<Usuario> {
        return dao.getAll().map { it.toDomain() }
    }

    suspend fun toggleAdminRole(userId: String) {
        // Buscamos, modificamos y actualizamos
        val users = dao.getAll()
        val target = users.find { it.id == userId } ?: return

        val currentRol = try { Rol.valueOf(target.rolName) } catch (e: Exception) { Rol.USUARIO }
        val newRol = if (currentRol == Rol.ADMIN) Rol.USUARIO else Rol.ADMIN

        dao.update(target.copy(rolName = newRol.name))
    }

    // Nueva función para actualizar un usuario existente
    suspend fun updateUser(usuario: Usuario) {
        dao.update(usuario.toEntity())
    }

    // Nueva función para eliminar un usuario
    suspend fun deleteUser(userId: String) {
        // Room requiere el objeto para borrar, así que lo buscamos primero
        val users = dao.getAll()
        val target = users.find { it.id == userId }
        if (target != null) {
            dao.delete(target)
        }
    }
}