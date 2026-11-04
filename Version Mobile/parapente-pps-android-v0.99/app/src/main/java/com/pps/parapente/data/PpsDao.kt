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

    // ---------- Suivi d'un pilote ----------
    @Query("SELECT epreuveId FROM inscriptions WHERE piloteId = :piloteId")
    suspend fun epreuvesDuPilote(piloteId: Int): List<Int>

    // ---------- Planning : journées ----------
    @Query("SELECT * FROM journees ORDER BY ordre, id")
    suspend fun toutesJournees(): List<JourneeEntity>

    @Query("SELECT * FROM journees WHERE uuid = :uuid LIMIT 1")
    suspend fun journeeParUuid(uuid: String): JourneeEntity?

    @Insert
    suspend fun insererJournee(j: JourneeEntity): Long

    @Update
    suspend fun majJournee(j: JourneeEntity)

    @Query("DELETE FROM journees WHERE id = :id")
    suspend fun supprimerJournee(id: Int)

    @Query("SELECT COALESCE(MAX(ordre), -1) + 1 FROM journees")
    suspend fun prochainOrdreJournee(): Int

    // ---------- Planning : ateliers ----------
    @Query("SELECT * FROM ateliers WHERE journeeId = :journeeId ORDER BY heureDebut, ordre, id")
    suspend fun ateliersDeJournee(journeeId: Int): List<AtelierEntity>

    @Query("SELECT * FROM ateliers")
    suspend fun tousAteliers(): List<AtelierEntity>

    @Query("SELECT * FROM ateliers WHERE uuid = :uuid LIMIT 1")
    suspend fun atelierParUuid(uuid: String): AtelierEntity?

    @Query("SELECT COUNT(*) FROM ateliers WHERE journeeId = :journeeId")
    suspend fun compterAteliers(journeeId: Int): Int

    @Insert
    suspend fun insererAtelier(a: AtelierEntity): Long

    @Update
    suspend fun majAtelier(a: AtelierEntity)

    @Query("DELETE FROM ateliers WHERE id = :id")
    suspend fun supprimerAtelier(id: Int)

    @Query("SELECT COALESCE(MAX(ordre), -1) + 1 FROM ateliers WHERE journeeId = :journeeId")
    suspend fun prochainOrdreAtelier(journeeId: Int): Int

    // ---------- Planning : épreuves des ateliers ----------
    @Query("SELECT * FROM atelier_epreuves")
    suspend fun tousLiensAtelier(): List<AtelierEpreuveEntity>

    @Insert
    suspend fun insererLiensAtelier(liens: List<AtelierEpreuveEntity>)

    @Query("DELETE FROM atelier_epreuves WHERE atelierId = :atelierId")
    suspend fun supprimerLiensAtelier(atelierId: Int)

    @Query("DELETE FROM atelier_epreuves WHERE epreuveId IN (:epreuveIds)")
    suspend fun retirerEpreuvesDesAteliers(epreuveIds: List<Int>)

    @Query("SELECT e.* FROM epreuves e JOIN atelier_epreuves ae ON ae.epreuveId = e.id WHERE ae.atelierId = :atelierId ORDER BY ae.ordre, ae.id")
    suspend fun epreuvesDeAtelier(atelierId: Int): List<EpreuveEntity>

    @Query("SELECT * FROM epreuves WHERE actif = 1 AND id NOT IN (SELECT epreuveId FROM atelier_epreuves) ORDER BY ordre, id")
    suspend fun epreuvesNonAffectees(): List<EpreuveEntity>

    @Query("SELECT * FROM epreuves WHERE actif = 1 AND (id NOT IN (SELECT epreuveId FROM atelier_epreuves) OR id IN (SELECT epreuveId FROM atelier_epreuves WHERE atelierId = :atelierId)) ORDER BY ordre, id")
    suspend fun epreuvesDisponibles(atelierId: Int): List<EpreuveEntity>

    @Query("SELECT COUNT(*) FROM epreuves WHERE actif = 1")
    suspend fun compterEpreuvesActives(): Int

    @Query("SELECT * FROM epreuves WHERE uuid = :uuid LIMIT 1")
    suspend fun epreuveParUuid(uuid: String): EpreuveEntity?
}
