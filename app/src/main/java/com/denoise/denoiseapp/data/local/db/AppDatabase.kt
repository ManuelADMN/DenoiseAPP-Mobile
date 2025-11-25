package com.denoise.denoiseapp.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters // Importante
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.denoise.denoiseapp.data.local.dao.ReportDao
import com.denoise.denoiseapp.data.local.entity.ReportEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

@Database(
    entities = [ReportEntity::class],
    version = 3, // Subimos versión para forzar actualización (importante para cambios de esquema)
    exportSchema = false
)
@TypeConverters(Converters::class) // Registramos el converter para manejar List<String>
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
                    // En desarrollo usamos fallbackToDestructiveMigration para simplificar cambios de esquema.
                    // Esto borra la base de datos antigua y crea una nueva si cambia la versión.
                    .fallbackToDestructiveMigration()
                    .addCallback(seedCallback(context.applicationContext))
                    .build()
                    .also { INSTANCE = it }
            }

        private fun seedCallback(appContext: Context) = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Datos de semilla opcionales...
                CoroutineScope(Dispatchers.IO).launch {
                    // Aquí podrías insertar datos iniciales si lo necesitas
                }
            }
        }
    }
}