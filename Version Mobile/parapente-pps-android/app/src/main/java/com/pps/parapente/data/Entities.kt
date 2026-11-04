package com.pps.parapente.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "utilisateurs", indices = [Index(value = ["identifiant"], unique = true)])
data class UtilisateurEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val identifiant: String,
    val motDePasse: String,
    val role: String,
    val piloteId: Int? = null,
    val actif: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis() // synchronisation : date de dernière modification
)

@Entity(tableName = "pilotes", indices = [Index(value = ["numeroLicence"], unique = true)])
data class PiloteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val numeroLicence: String,
    val nom: String,
    val prenom: String,
    val caserne: String? = null,
    val poids: Double? = null,
    val email: String? = null,
    val anneeNaissance: Int? = null,
    val genre: String? = null, // "Homme" ou "Femme"
    val updatedAt: Long = System.currentTimeMillis(), // synchronisation : date de dernière modification
    val supabaseId: Int? = null // synchronisation : id attribué par Supabase (cache, pour les clés étrangères)
)

@Entity(tableName = "epreuves")
data class EpreuveEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nom: String,
    val description: String? = null,
    val modeCalcul: String = Mode.FORMULE,
    val formule: String? = null,
    val valeurCle: String? = null,
    val sensClassement: String = "ASC",
    val afficherClassement: Boolean = true,
    val compteDansGeneral: Boolean = true,
    val actif: Boolean = true,
    val ordre: Int = 0,
    val uuid: String = java.util.UUID.randomUUID().toString(), // synchronisation : identifiant stable entre appareils
    val updatedAt: Long = System.currentTimeMillis(),
    val supabaseId: Int? = null
)

@Entity(
    tableName = "epreuve_parametres",
    foreignKeys = [ForeignKey(entity = EpreuveEntity::class, parentColumns = ["id"], childColumns = ["epreuveId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("epreuveId")]
)
data class ParametreEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val epreuveId: Int,
    val nomVariable: String,
    val label: String,
    val unite: String? = null,
    val obligatoire: Boolean = true,
    val ordre: Int = 0
)

@Entity(
    tableName = "bareme_points",
    foreignKeys = [ForeignKey(entity = EpreuveEntity::class, parentColumns = ["id"], childColumns = ["epreuveId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index(value = ["epreuveId", "rang"], unique = true)]
)
data class BaremeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val epreuveId: Int,
    val rang: Int,
    val points: Double
)

@Entity(
    tableName = "inscriptions",
    foreignKeys = [
        ForeignKey(entity = PiloteEntity::class, parentColumns = ["id"], childColumns = ["piloteId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = EpreuveEntity::class, parentColumns = ["id"], childColumns = ["epreuveId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index(value = ["piloteId", "epreuveId"], unique = true), Index("epreuveId")]
)
data class InscriptionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val piloteId: Int,
    val epreuveId: Int
)

@Entity(
    tableName = "resultats",
    foreignKeys = [
        ForeignKey(entity = PiloteEntity::class, parentColumns = ["id"], childColumns = ["piloteId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = EpreuveEntity::class, parentColumns = ["id"], childColumns = ["epreuveId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index(value = ["epreuveId", "piloteId"], unique = true), Index("piloteId")]
)
data class ResultatEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val epreuveId: Int,
    val piloteId: Int,
    val valeursJson: String = "{}",
    val points: Double = 0.0,
    val disqualifie: Boolean = false,
    val saisiPar: String? = null,
    val dateSaisie: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis() // synchronisation : date de dernière modification
)

// ====================================================================== Planning (version PC 0.5)
// Journée > ateliers (matin / après-midi) > épreuves. Une épreuve n'appartient qu'à UN seul atelier.

@Entity(tableName = "journees")
data class JourneeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val uuid: String = java.util.UUID.randomUUID().toString(), // synchronisation : identifiant stable entre appareils
    val nom: String,
    val dateJournee: String? = null, // format ISO "yyyy-MM-dd" (comme Supabase)
    val ordre: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "ateliers",
    foreignKeys = [ForeignKey(entity = JourneeEntity::class, parentColumns = ["id"], childColumns = ["journeeId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("journeeId")]
)
data class AtelierEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val uuid: String = java.util.UUID.randomUUID().toString(),
    val journeeId: Int,
    val nom: String,
    val creneau: String = Creneau.MATIN, // MATIN ou APRES_MIDI
    val heureDebut: String? = null,      // HH:mm
    val heureFin: String? = null,        // HH:mm
    val lieu: String? = null,
    val responsable: String? = null,
    val ordre: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "atelier_epreuves",
    foreignKeys = [
        ForeignKey(entity = AtelierEntity::class, parentColumns = ["id"], childColumns = ["atelierId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = EpreuveEntity::class, parentColumns = ["id"], childColumns = ["epreuveId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("atelierId"), Index(value = ["epreuveId"], unique = true)]
)
data class AtelierEpreuveEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val atelierId: Int,
    val epreuveId: Int,
    val ordre: Int = 0 // ordre de passage dans l'atelier
)
