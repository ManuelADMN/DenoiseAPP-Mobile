package com.denoise.denoiseapp.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.denoise.denoiseapp.data.local.dao.MarkerDao
import com.denoise.denoiseapp.data.local.dao.ReportDao
import com.denoise.denoiseapp.data.local.dao.UsuarioDao
import com.denoise.denoiseapp.data.local.entity.MarkerEntity
import com.denoise.denoiseapp.data.local.entity.ReportEntity
import com.denoise.denoiseapp.data.local.entity.UsuarioEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

@Database(
    entities = [
        ReportEntity::class,
        MarkerEntity::class,
        UsuarioEntity::class
    ],
    version = 6, // Versión actual: incrementada para incluir cambios de esquema
    exportSchema = false
)
@TypeConverters(Converters::class) // Habilita los conversores de tipos (List<String> -> JSON)
abstract class AppDatabase : RoomDatabase() {

    abstract fun reportDao(): ReportDao
    abstract fun markerDao(): MarkerDao
    abstract fun usuarioDao(): UsuarioDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "denoise_db"
                )
                    // IMPORTANTE: Permite destruir y recrear la base de datos si hay cambios de versión.
                    // Esto borra los datos existentes al actualizar, lo cual es aceptable en desarrollo.
                    // Para producción, se deberían usar migraciones.
                    .fallbackToDestructiveMigration()
                    .addCallback(seedCallback(context.applicationContext))
                    .build()
                    .also { INSTANCE = it }
            }

        // Callback para poblar la base de datos con datos iniciales
        private fun seedCallback(appContext: Context) = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    val database = getInstance(appContext)
                    val usuarioDao = database.usuarioDao()

                    // Insertar usuarios por defecto si la tabla está vacía
                    if (usuarioDao.count() == 0) {
                        usuarioDao.insert(
                            UsuarioEntity(
                                id = UUID.randomUUID().toString(),
                                email = "admin@denoise.com",
                                nombre = "Administrador",
                                passwordHash = "admin123",
                                rolName = "ADMIN"
                            )
                        )
                        usuarioDao.insert(
                            UsuarioEntity(
                                id = UUID.randomUUID().toString(),
                                email = "user@denoise.com",
                                nombre = "Operario",
                                passwordHash = "user123",
                                rolName = "USUARIO"
                            )
                        )
                    }
                }
            }
        }
    }
}