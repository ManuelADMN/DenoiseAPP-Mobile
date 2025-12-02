package com.denoise.denoiseapp.data.remote.firestore

import com.denoise.denoiseapp.core.util.awaitResult
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

/**
 * Guardado de USUARIOS en Firestore.
 * Colección: "usuarios"
 *
 * API sin dependencia del modelo de dominio:
 *  - upsertFromFields(id, nombre, email, rol?, extras?)
 *  - upsertFromMap(id, map)         // por si quieres pasar un mapa arbitrario
 *  - deleteById(id)                 // opcional
 */
object FirestoreUsuariosRepository {

    private const val COLLECTION = "usuarios"
    private val db by lazy { FirebaseFirestore.getInstance() }

    /** Crea/actualiza un usuario con algunos campos comunes. */
    suspend fun upsertFromFields(
        id: String,
        nombre: String? = null,
        email: String? = null,
        rol: String? = null,
        extras: Map<String, Any?> = emptyMap()
    ) {
        val data = mutableMapOf<String, Any?>(
            "id" to id,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (!nombre.isNullOrBlank()) data["nombre"] = nombre
        if (!email.isNullOrBlank())  data["email"] = email
        if (!rol.isNullOrBlank())    data["rol"] = rol
        data.putAll(extras)

        db.collection(COLLECTION)
            .document(id)
            .set(data, SetOptions.merge())   // merge para no pisar campos previos
            .awaitResult()
    }

    /** Versión genérica: pasa cualquier mapa de campos ya armado. */
    suspend fun upsertFromMap(
        id: String,
        campos: Map<String, Any?>
    ) {
        val base = campos.toMutableMap().apply {
            put("id", id)
            put("updatedAt", FieldValue.serverTimestamp())
        }

        db.collection(COLLECTION)
            .document(id)
            .set(base, SetOptions.merge())
            .awaitResult()
    }

    /** (Opcional) Borrar usuario remoto por ID. */
    suspend fun deleteById(id: String) {
        db.collection(COLLECTION).document(id).delete().awaitResult()
    }
}
