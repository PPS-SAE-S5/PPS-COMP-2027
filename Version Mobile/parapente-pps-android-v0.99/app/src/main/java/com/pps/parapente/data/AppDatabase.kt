package com.pps.parapente.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        UtilisateurEntity::class, PiloteEntity::class, EpreuveEntity::class,
        ParametreEntity::class, BaremeEntity::class, InscriptionEntity::class, ResultatEntity::class,
        JourneeEntity::class, AtelierEntity::class, AtelierEpreuveEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun dao(): PpsDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        /**
         * Version 3 : ajout du planning (journées, ateliers, liens atelier-épreuves).
         * Vraie migration : les données déjà présentes sur le téléphone (pilotes, épreuves, résultats...)
         * sont CONSERVÉES. Le SQL reproduit exactement ce que Room génère pour les trois nouvelles entités.
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `journees` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `uuid` TEXT NOT NULL, `nom` TEXT NOT NULL, `dateJournee` TEXT, `ordre` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `ateliers` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `uuid` TEXT NOT NULL, `journeeId` INTEGER NOT NULL, `nom` TEXT NOT NULL, `creneau` TEXT NOT NULL, `heureDebut` TEXT, `heureFin` TEXT, `lieu` TEXT, `responsable` TEXT, `ordre` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, FOREIGN KEY(`journeeId`) REFERENCES `journees`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_ateliers_journeeId` ON `ateliers` (`journeeId`)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `atelier_epreuves` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `atelierId` INTEGER NOT NULL, `epreuveId` INTEGER NOT NULL, `ordre` INTEGER NOT NULL, FOREIGN KEY(`atelierId`) REFERENCES `ateliers`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`epreuveId`) REFERENCES `epreuves`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_atelier_epreuves_atelierId` ON `atelier_epreuves` (`atelierId`)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_atelier_epreuves_epreuveId` ON `atelier_epreuves` (`epreuveId`)")
            }
        }

        fun get(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext, AppDatabase::class.java, "pps_championnat.db"
                )
                    .addMigrations(MIGRATION_2_3)
                    // Reste de secours pour une très vieille base (version 1) : on la recrée.
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
