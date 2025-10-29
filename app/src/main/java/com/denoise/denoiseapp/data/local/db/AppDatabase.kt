package com.denoise.denoiseapp.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.denoise.denoiseapp.data.local.dao.ReportDao
import com.denoise.denoiseapp.data.local.entity.ReportEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

@Database(
    entities = [ReportEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun reportDao(): ReportDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "denoise_db"
                )
                    .addCallback(seedCallback(context.applicationContext))
                    .build()
                    .also { INSTANCE = it }
            }

        private fun seedCallback(appContext: Context) = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Semilla en un hilo IO
                CoroutineScope(Dispatchers.IO).launch {
                    val instance = getInstance(appContext)
                    val dao = instance.reportDao()
                    if (dao.count() == 0) {
                        val now = System.currentTimeMillis()
                        val demo = (1..12).map { idx ->
                            ReportEntity(
                                id = UUID.randomUUID().toString(),
                                titulo = "Inspección Lote ${1000 + idx}",
                                plantaId = "PL-${(idx % 3) + 1}",
                                plantaNombre = listOf("Puerto Montt", "Quellón", "Castro")[(idx - 1) % 3],
                                linea = "Línea ${(idx % 4) + 1}",
                                lote = "L${200 + idx}",
                                estado = listOf("PENDIENTE","EN_PROCESO","QA","FINALIZADO")[idx % 4],
                                fechaCreacionMillis = now - idx * 86_400_000L,
                                fechaObjetivoMillis = if (idx % 3 == 0) now + idx * 86_400_000L else null,
                                notas = if (idx % 2 == 0) "Revisión visual y captura de evidencias." else null,
                                evidenciasCount = idx % 5,
                                creadoPor = "operario${idx}@denoise.com",
                                asignadoA = if (idx % 2 == 1) "tecnico${idx}@denoise.com" else null,
                                ultimaActualizacionMillis = now - idx * 43_200_000L
                            )
                        }
                        demo.forEach { dao.upsert(it) }
                    }
                }
            }
        }
    }
}
