package com.pps.parapente.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface PpsDao {

    // ---------- Utilisateurs ----------
    @Query("SELECT * FROM utilisateurs WHERE identifiant = :identifiant AND actif = 1 LIMIT 1")
    suspend fun utilisateurParIdentifiant(identifiant: String): UtilisateurEntity?

    @Query("SELECT * FROM utilisateurs ORDER BY identifiant")
    suspend fun tousUtilisateurs(): List<UtilisateurEntity>

    @Insert
    suspend fun insererUtilisateur(u: UtilisateurEntity): Long

    @Update
    suspend fun majUtilisateur(u: UtilisateurEntity)

    @Query("UPDATE utilisateurs SET actif = :actif, updatedAt = :maintenant WHERE id = :id")
    suspend fun definirActifUtilisateur(id: Int, actif: Boolean, maintenant: Long = System.currentTimeMillis())

    @Query("UPDATE utilisateurs SET motDePasse = :motDePasseHache, updatedAt = :maintenant WHERE id = :id")
    suspend fun changerMotDePasse(id: Int, motDePasseHache: String, maintenant: Long = System.currentTimeMillis())

    @Query("DELETE FROM utilisateurs WHERE id = :id")
    suspend fun supprimerUtilisateur(id: Int)

    // ---------- Pilotes ----------
    @Query("SELECT * FROM pilotes ORDER BY nom, prenom")
    suspend fun tousPilotes(): List<PiloteEntity>

    @Insert
    suspend fun insererPilote(p: PiloteEntity): Long

    @Update
    suspend fun majPilote(p: PiloteEntity)

    @Query("DELETE FROM pilotes WHERE id = :id")
    suspend fun supprimerPilote(id: Int)

    // ---------- Épreuves ----------
    @Query("SELECT * FROM epreuves ORDER BY ordre, id")
    suspend fun toutesEpreuves(): List<EpreuveEntity>

    @Insert
    suspend fun insererEpreuve(e: EpreuveEntity): Long

    @Update
    suspend fun majEpreuve(e: EpreuveEntity)

    @Query("DELETE FROM epreuves WHERE id = :id")
    suspend fun supprimerEpreuve(id: Int)

    @Query("SELECT * FROM epreuve_parametres WHERE epreuveId = :epreuveId ORDER BY ordre, id")
    suspend fun parametres(epreuveId: Int): List<ParametreEntity>

    @Insert
    suspend fun insererParametres(liste: List<ParametreEntity>)

    @Query("DELETE FROM epreuve_parametres WHERE epreuveId = :epreuveId")
    suspend fun supprimerParametres(epreuveId: Int)

    @Query("SELECT * FROM bareme_points WHERE epreuveId = :epreuveId ORDER BY rang")
    suspend fun bareme(epreuveId: Int): List<BaremeEntity>

    @Insert
    suspend fun insererBareme(liste: List<BaremeEntity>)

    @Query("DELETE FROM bareme_points WHERE epreuveId = :epreuveId")
    suspend fun supprimerBareme(epreuveId: Int)

    // ---------- Inscriptions ----------
    @Query("INSERT OR IGNORE INTO inscriptions (piloteId, epreuveId) SELECT id, :epreuveId FROM pilotes")
    suspend fun inscrireTousLesPilotes(epreuveId: Int)

    @Query("INSERT OR IGNORE INTO inscriptions (piloteId, epreuveId) SELECT :piloteId, id FROM epreuves WHERE actif = 1")
    suspend fun inscrirePiloteAuxEpreuvesActives(piloteId: Int)

    @Query("SELECT piloteId FROM inscriptions WHERE epreuveId = :epreuveId")
    suspend fun pilotesInscrits(epreuveId: Int): List<Int>

    @Query("SELECT * FROM inscriptions")
    suspend fun toutesInscriptions(): List<InscriptionEntity>

    @Query("SELECT COUNT(*) FROM inscriptions WHERE piloteId = :piloteId AND epreuveId = :epreuveId")
    suspend fun inscriptionExiste(piloteId: Int, epreuveId: Int): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insererInscription(i: InscriptionEntity)

    @Query("DELETE FROM inscriptions WHERE id = :id")
    suspend fun supprimerInscription(id: Int)

    // ---------- Résultats ----------
    @Query("SELECT * FROM resultats WHERE epreuveId = :epreuveId")
    suspend fun resultatsEpreuve(epreuveId: Int): List<ResultatEntity>

    @Query("SELECT * FROM resultats WHERE epreuveId = :epreuveId AND piloteId = :piloteId LIMIT 1")
    suspend fun resultat(epreuveId: Int, piloteId: Int): ResultatEntity?

    @Query("SELECT * FROM resultats")
    suspend fun tousResultats(): List<ResultatEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun enregistrerResultat(r: ResultatEntity)

    @Query("DELETE FROM resultats WHERE id = :id")
    suspend fun supprimerResultat(id: Int)

    @Query("UPDATE pilotes SET supabaseId = :supabaseId WHERE id = :id")
    suspend fun definirSupabaseIdPilote(id: Int, supabaseId: Int)

    @Query("UPDATE epreuves SET supabaseId = :supabaseId WHERE id = :id")
    suspend fun definirSupabaseIdEpreuve(id: Int, supabaseId: Int)
}
