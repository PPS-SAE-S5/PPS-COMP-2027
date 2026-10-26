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
    version = 2,
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
                    // Version 2 (ajout du genre, des champs de synchronisation) : pas de vraie
                    // migration pour une appli encore en phase de test — on recrée la base au
                    // besoin plutôt que d'écrire une migration complexe pour des données de test.
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Compte administrateur par défaut : admin / admin123
                            db.execSQL(
                                "INSERT INTO utilisateurs (identifiant, motDePasse, role, piloteId, actif, updatedAt) " +
                                    "VALUES ('admin', '${Securite.hacher("admin123")}', 'ADMINISTRATEUR', NULL, 1, ${System.currentTimeMillis()})"
                            )
                        }
                    })
                    .build().also { instance = it }
            }
    }
}
