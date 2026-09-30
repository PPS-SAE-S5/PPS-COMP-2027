package com.pps.parapente.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        UtilisateurEntity::class, PiloteEntity::class, EpreuveEntity::class,
        ParametreEntity::class, BaremeEntity::class, InscriptionEntity::class, ResultatEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun dao(): PpsDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext, AppDatabase::class.java, "pps_championnat.db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Compte administrateur par défaut : admin / admin123
                            db.execSQL(
                                "INSERT INTO utilisateurs (identifiant, motDePasse, role, piloteId, actif) " +
                                    "VALUES ('admin', '${Securite.hacher("admin123")}', 'ADMINISTRATEUR', NULL, 1)"
                            )
                        }
                    })
                    .build().also { instance = it }
            }
    }
}
