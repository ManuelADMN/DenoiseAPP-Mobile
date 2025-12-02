package com.denoise.denoiseapp.data.remote.firestore

import com.denoise.denoiseapp.core.util.awaitResult
import com.denoise.denoiseapp.domain.model.Evidencia
import com.denoise.denoiseapp.domain.model.Reporte
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

/**
 * Repositorio mínimo para escribir en Firestore.
 * - Colección: "reportes"
 * - upsertFromDomain(reporte): crea/actualiza un doc con el mismo id del dominio.
 * - deleteById(id): (opcional) borrar remoto.
 *
 * No introduce dependencias *-ktx. Usa Task.awaitResult() (extensión propia).
 */
object FirestoreReportesRepository {

    private const val COLLECTION = "reportes"
    private val db by lazy { FirebaseFirestore.getInstance() }

    /** Crea o actualiza el documento con el ID del dominio. */
    suspend fun upsertFromDomain(reporte: Reporte) {
        val data = reporte.toFirestoreMap()
        db.collection(COLLECTION)
            .document(reporte.id)
            // merge() para no perder campos si más adelante agregas otros
            .set(data, SetOptions.merge())
            .awaitResult()
    }

    /** (Opcional) Borra un reporte en Firestore por ID. */
    suspend fun deleteById(id: String) {
        db.collection(COLLECTION).document(id).delete().awaitResult()
    }

    /** Mapeo tipado Reporte -> Map<String, Any?> apto para Firestore */
    private fun Reporte.toFirestoreMap(): Map<String, Any?> {
        return mapOf(
            // Identificadores y metadatos
            "id" to id,
            "titulo" to titulo,
            "plantaId" to planta.id,
            "plantaNombre" to planta.nombre,
            "linea" to linea,
            "lote" to lote,
            "estado" to estado.name,
            "notas" to notas,

            // Métricas
            "porcentajeInfectados" to porcentajeInfectados,
            "melanosis" to melanosis,
            "cracking" to cracking,
            "gaping" to gaping,

            // Evidencias como lista de mapas
            "evidencias" to evidencias.map { it.toFirestoreMap() },

            // Autores / asignación
            "creadoPor" to creadoPor,
            "asignadoA" to asignadoA,

            // Tiempos (guardamos milisegundos del cliente y un serverTimestamp)
            "fechaCreacionMillis" to fechaCreacionMillis,
            "fechaObjetivoMillis" to fechaObjetivoMillis,
            "ultimaActualizacionMillis" to ultimaActualizacionMillis,

            // Marca de tiempo del servidor para ordenar en consola
            "updatedAt" to FieldValue.serverTimestamp()
        )
    }

    private fun Evidencia.toFirestoreMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "uriLocal" to uriLocal,
        "uriRemota" to uriRemota,
        "tipo" to tipo.name,
        "timestampMillis" to timestampMillis,
        "descripcion" to descripcion
    )
}
