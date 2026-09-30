package com.pps.parapente.data

import androidx.room.withTransaction
import net.objecthunter.exp4j.ExpressionBuilder

/**
 * Toute la logique métier (identique à la version PC) : authentification, pilotes, épreuves
 * paramétrables, calcul des points (formule ou barème) et classements.
 */
class Repository(private val db: AppDatabase) {

    private val dao = db.dao()

    // ------------------------------------------------------------------ Authentification

    suspend fun seConnecter(identifiant: String, motDePasse: String): UtilisateurEntity? {
        val u = dao.utilisateurParIdentifiant(identifiant) ?: return null
        return if (Securite.hacher(motDePasse) == u.motDePasse) u else null
    }

    // ------------------------------------------------------------------ Utilisateurs

    suspend fun utilisateurs(): List<UtilisateurEntity> = dao.tousUtilisateurs()

    suspend fun creerUtilisateur(identifiant: String, motDePasse: String, role: Role?) {
        if (identifiant.isBlank() || motDePasse.isEmpty() || role == null)
            throw ErreurMetier("Identifiant, mot de passe et rôle sont obligatoires.")
        dao.insererUtilisateur(
            UtilisateurEntity(identifiant = identifiant.trim(), motDePasse = Securite.hacher(motDePasse), role = role.name)
        )
    }

    suspend fun supprimerUtilisateur(u: UtilisateurEntity) {
        if (u.identifiant == "admin")
            throw ErreurMetier("Le compte administrateur par défaut ne peut pas être supprimé.")
        dao.supprimerUtilisateur(u.id)
    }

    // ------------------------------------------------------------------ Pilotes

    suspend fun pilotes(): List<PiloteEntity> = dao.tousPilotes()

    private fun validerPilote(p: PiloteEntity) {
        if (p.numeroLicence.isBlank()) throw ErreurMetier("Le numéro de licence est obligatoire.")
        if (p.nom.isBlank()) throw ErreurMetier("Le nom est obligatoire.")
        if (p.prenom.isBlank()) throw ErreurMetier("Le prénom est obligatoire.")
    }

    suspend fun creerPilote(p: PiloteEntity) {
        validerPilote(p)
        db.withTransaction {
            val id = dao.insererPilote(p).toInt()
            // Règle du club : tout pilote inscrit doit passer les épreuves -> inscription automatique
            dao.inscrirePiloteAuxEpreuvesActives(id)
        }
    }

    suspend fun majPilote(p: PiloteEntity) {
        validerPilote(p)
        dao.majPilote(p)
    }

    suspend fun supprimerPilote(id: Int) = dao.supprimerPilote(id)

    suspend fun pilotesInscrits(epreuveId: Int): List<PiloteEntity> {
        val ids = dao.pilotesInscrits(epreuveId).toSet()
        return dao.tousPilotes().filter { it.id in ids }
    }

    // ------------------------------------------------------------------ Épreuves

    suspend fun epreuves(): List<EpreuveComplete> =
        dao.toutesEpreuves().map { EpreuveComplete(it, dao.parametres(it.id), dao.bareme(it.id)) }

    private fun validerEpreuve(ec: EpreuveComplete) {
        val e = ec.epreuve
        if (e.nom.isBlank()) throw ErreurMetier("Le nom de l'épreuve est obligatoire.")
        if (e.modeCalcul == Mode.FORMULE && e.formule.isNullOrBlank())
            throw ErreurMetier("La formule de calcul est obligatoire en mode Formule.")
        if (e.modeCalcul == Mode.BAREME) {
            if (e.valeurCle.isNullOrBlank())
                throw ErreurMetier("La variable de classement est obligatoire en mode Barème.")
            if (ec.bareme.isEmpty())
                throw ErreurMetier("Le barème de points (rang -> points) est obligatoire en mode Barème.")
        }
    }

    suspend fun sauverEpreuve(ec: EpreuveComplete) {
        validerEpreuve(ec)
        db.withTransaction {
            val nouvelle = ec.epreuve.id == 0
            val id = if (nouvelle) {
                dao.insererEpreuve(ec.epreuve).toInt()
            } else {
                dao.majEpreuve(ec.epreuve)
                dao.supprimerParametres(ec.epreuve.id)
                dao.supprimerBareme(ec.epreuve.id)
                ec.epreuve.id
            }
            if (ec.parametres.isNotEmpty())
                dao.insererParametres(ec.parametres.map { it.copy(id = 0, epreuveId = id) })
            if (ec.bareme.isNotEmpty())
                dao.insererBareme(ec.bareme.map { it.copy(id = 0, epreuveId = id) })
            if (nouvelle) dao.inscrireTousLesPilotes(id) // tous les pilotes existants passent la nouvelle épreuve
        }
    }

    suspend fun supprimerEpreuve(id: Int) = dao.supprimerEpreuve(id)

    // ------------------------------------------------------------------ Calcul des points

    private fun evaluerFormule(formule: String, variables: Map<String, Double>): Double {
        try {
            val builder = ExpressionBuilder(formule)
            variables.keys.forEach { builder.variable(it) }
            val expression = builder.build()
            variables.forEach { (nom, valeur) -> expression.setVariable(nom, valeur) }
            val resultat = expression.evaluate()
            if (resultat.isNaN() || resultat.isInfinite())
                throw ErreurMetier("Le calcul donne un résultat impossible (division par zéro ?).")
            return resultat
        } catch (e: ErreurMetier) {
            throw e
        } catch (e: ArithmeticException) {
            throw ErreurMetier("Division par zéro dans la formule.")
        } catch (e: Exception) {
            throw ErreurMetier(
                "La formule est invalide, ou utilise une variable non saisie ou mal orthographiée. " +
                    "Vérifiez la configuration de l'épreuve."
            )
        }
    }

    private fun construireVariables(pilote: PiloteEntity, saisies: Map<String, Double>): Map<String, Double> =
        saisies + mapOf("age" to pilote.age.toDouble(), "poidsPilote" to (pilote.poids ?: 0.0))

    private suspend fun sauverResultat(
        epreuveId: Int, piloteId: Int, valeurs: Map<String, Double>,
        points: Double, disqualifie: Boolean, saisiPar: String
    ) {
        val existant = dao.resultat(epreuveId, piloteId)
        dao.enregistrerResultat(
            ResultatEntity(
                id = existant?.id ?: 0, epreuveId = epreuveId, piloteId = piloteId,
                valeursJson = mapVersJson(valeurs), points = points,
                disqualifie = disqualifie, saisiPar = saisiPar
            )
        )
    }

    /** Mode FORMULE : calcule et enregistre les points d'UN pilote. */
    suspend fun calculerFormule(
        ep: EpreuveComplete, pilote: PiloteEntity, valeurs: Map<String, Double>,
        disqualifie: Boolean, saisiPar: String
    ) {
        if (ep.epreuve.modeCalcul != Mode.FORMULE) throw ErreurMetier("Cette épreuve n'est pas en mode Formule.")
        val points = if (disqualifie) 0.0
        else evaluerFormule(ep.epreuve.formule ?: "", construireVariables(pilote, valeurs))
        sauverResultat(ep.epreuve.id, pilote.id, valeurs, points, disqualifie, saisiPar)
    }

    /** Mode BAREME : enregistre la valeur brute puis recalcule les points de TOUS les pilotes. */
    suspend fun enregistrerBareme(
        ep: EpreuveComplete, pilote: PiloteEntity, valeurs: Map<String, Double>,
        disqualifie: Boolean, saisiPar: String
    ) {
        if (ep.epreuve.modeCalcul != Mode.BAREME) throw ErreurMetier("Cette épreuve n'est pas en mode Barème.")
        db.withTransaction {
            sauverResultat(ep.epreuve.id, pilote.id, valeurs, 0.0, disqualifie, saisiPar)
            recalculerBareme(ep)
        }
    }

    /** Classe tous les résultats sur la variable clé puis attribue les points du barème par rang. */
    suspend fun recalculerBareme(ep: EpreuveComplete) {
        val e = ep.epreuve
        val cle = e.valeurCle?.takeIf { it.isNotBlank() }
            ?: throw ErreurMetier("Aucune variable de classement n'est définie pour cette épreuve.")
        val resultats = dao.resultatsEpreuve(e.id)
        val classables = resultats.filter { !it.disqualifie && it.valeurs().containsKey(cle) }
        val tries = if (e.sensClassement == "DESC")
            classables.sortedByDescending { it.valeurs().getValue(cle) }
        else
            classables.sortedBy { it.valeurs().getValue(cle) }

        val pointsParRang = ep.bareme.associate { it.rang to it.points }
        val dernierPalier = ep.bareme.maxByOrNull { it.rang }?.points ?: 0.0

        tries.forEachIndexed { index, r ->
            dao.enregistrerResultat(r.copy(points = pointsParRang[index + 1] ?: dernierPalier))
        }
        val idsClasses = classables.map { it.id }.toSet()
        resultats.filter { it.id !in idsClasses }.forEach { dao.enregistrerResultat(it.copy(points = 0.0)) }
    }

    // ------------------------------------------------------------------ Résultats & classements

    suspend fun resultat(epreuveId: Int, piloteId: Int): ResultatEntity? = dao.resultat(epreuveId, piloteId)

    /** Classement d'UNE épreuve (points décroissants). */
    suspend fun classementEpreuve(epreuveId: Int): List<ResultatAffiche> {
        val pilotes = dao.tousPilotes().associateBy { it.id }
        return dao.resultatsEpreuve(epreuveId)
            .sortedByDescending { it.points }
            .map { ResultatAffiche(it, pilotes[it.piloteId], it.valeurs()) }
    }

    /** Classement général : un pilote inscrit sans résultat sur une épreuve obtient 0 point. */
    suspend fun classementGeneral(): List<LigneClassement> {
        val pilotes = dao.tousPilotes()
        val epreuves = dao.toutesEpreuves().filter { it.actif && it.compteDansGeneral }

        val totaux = pilotes.associate { it.id to 0.0 }.toMutableMap()
        val details = pilotes.associate { it.id to mutableMapOf<Int, Double>() }

        for (ep in epreuves) {
            val pointsParPilote = dao.resultatsEpreuve(ep.id).associate { it.piloteId to it.points }
            for (piloteId in dao.pilotesInscrits(ep.id)) {
                if (piloteId !in totaux) continue
                val points = pointsParPilote[piloteId] ?: 0.0
                details.getValue(piloteId)[ep.id] = points
                totaux[piloteId] = totaux.getValue(piloteId) + points
            }
        }

        return pilotes
            .sortedByDescending { totaux.getValue(it.id) }
            .mapIndexed { index, p ->
                LigneClassement(p, totaux.getValue(p.id), index + 1, details.getValue(p.id))
            }
    }
}
