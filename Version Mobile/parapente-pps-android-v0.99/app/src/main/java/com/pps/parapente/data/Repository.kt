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

    /**
     * Bouton « Non, je veux uniquement suivre le classement » (comme sur PC) : connexion avec le compte
     * invité (rôle Pilote = classement seulement). Si ce compte n'existe pas encore sur cet appareil,
     * il est créé automatiquement. Renvoie null s'il existe mais a été désactivé ou si son mot de passe a changé.
     */
    suspend fun connexionInvite(): UtilisateurEntity? {
        seConnecter(Invite.IDENTIFIANT, Invite.MOT_DE_PASSE)?.let { return it }
        if (dao.tousUtilisateurs().any { it.identifiant == Invite.IDENTIFIANT }) return null
        dao.insererUtilisateur(
            UtilisateurEntity(
                identifiant = Invite.IDENTIFIANT,
                motDePasse = Securite.hacher(Invite.MOT_DE_PASSE),
                role = Role.PILOTE.name
            )
        )
        return seConnecter(Invite.IDENTIFIANT, Invite.MOT_DE_PASSE)
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

    /** Active ou désactive un compte (un compte désactivé ne peut plus se connecter). */
    suspend fun definirActifUtilisateur(u: UtilisateurEntity, actif: Boolean) {
        if (u.identifiant == "admin" && !actif)
            throw ErreurMetier("Le compte administrateur par défaut ne peut pas être désactivé.")
        dao.definirActifUtilisateur(u.id, actif)
    }

    suspend fun changerMotDePasse(u: UtilisateurEntity, nouveauMotDePasse: String) {
        if (nouveauMotDePasse.isBlank()) throw ErreurMetier("Le nouveau mot de passe ne peut pas être vide.")
        dao.changerMotDePasse(u.id, Securite.hacher(nouveauMotDePasse))
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
            dao.enregistrerResultat(r.copy(points = pointsParRang[index + 1] ?: dernierPalier, updatedAt = System.currentTimeMillis()))
        }
        val idsClasses = classables.map { it.id }.toSet()
        resultats.filter { it.id !in idsClasses }.forEach {
            dao.enregistrerResultat(it.copy(points = 0.0, updatedAt = System.currentTimeMillis()))
        }
    }

    // ------------------------------------------------------------------ Résultats & classements

    suspend fun resultat(epreuveId: Int, piloteId: Int): ResultatEntity? = dao.resultat(epreuveId, piloteId)

    /** Classement d'UNE épreuve (points décroissants). */
    suspend fun classementEpreuve(epreuveId: Int, filtre: FiltreGenre = FiltreGenre.MIXTE): List<ResultatAffiche> {
        val pilotes = dao.tousPilotes().associateBy { it.id }
        return dao.resultatsEpreuve(epreuveId)
            .sortedByDescending { it.points }
            .map { ResultatAffiche(it, pilotes[it.piloteId], it.valeurs()) }
            .filter { r -> filtre == FiltreGenre.MIXTE || r.pilote?.correspond(filtre) == true }
    }

    /** Classement général : un pilote inscrit sans résultat sur une épreuve obtient 0 point. */
    suspend fun classementGeneral(filtre: FiltreGenre = FiltreGenre.MIXTE): List<LigneClassement> {
        val pilotes = dao.tousPilotes().filter { it.correspond(filtre) }
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

    // ------------------------------------------------------------------ Duplication d'épreuve

    /** Duplique une épreuve (variables et barème inclus, SANS les résultats des pilotes). */
    suspend fun dupliquerEpreuve(idOriginal: Int): EpreuveEntity {
        val original = epreuves().firstOrNull { it.epreuve.id == idOriginal }
            ?: throw ErreurMetier("L'épreuve à dupliquer n'existe plus.")
        val existantes = dao.toutesEpreuves()
        val copie = original.epreuve.copy(
            id = 0, uuid = java.util.UUID.randomUUID().toString(), supabaseId = null,
            nom = nomDeCopie(original.epreuve.nom, existantes.map { it.nom }),
            ordre = (existantes.maxOfOrNull { it.ordre } ?: 0) + 1,
            updatedAt = System.currentTimeMillis()
        )
        // Même traitement qu'une nouvelle épreuve : validation + inscription de tous les pilotes existants
        sauverEpreuve(
            EpreuveComplete(
                copie,
                original.parametres.map { it.copy(id = 0, epreuveId = 0) },
                original.bareme.map { it.copy(id = 0, epreuveId = 0) }
            )
        )
        return dao.toutesEpreuves().first { it.uuid == copie.uuid }
    }

    /** Exemple : « X (copie) », puis « X (copie 2) », « X (copie 3) »... si ce nom est déjà pris. */
    private fun nomDeCopie(nomOriginal: String, existants: List<String>): String {
        val base = "$nomOriginal (copie"
        var candidat = "$base)"
        var n = 2
        while (existants.any { it.equals(candidat, ignoreCase = true) }) candidat = "$base ${n++})"
        return candidat
    }

    // ------------------------------------------------------------------ Planning (journées > ateliers > épreuves)

    private val heureValide = Regex("^([01]\\d|2[0-3]):[0-5]\\d$")

    suspend fun journees(): List<JourneeEntity> = dao.toutesJournees()

    suspend fun ateliersComplets(journeeId: Int): List<AtelierComplet> =
        dao.ateliersDeJournee(journeeId).map { AtelierComplet(it, dao.epreuvesDeAtelier(it.id)) }

    suspend fun epreuvesDeAtelier(atelierId: Int): List<EpreuveEntity> = dao.epreuvesDeAtelier(atelierId)
    suspend fun epreuvesNonAffectees(): List<EpreuveEntity> = dao.epreuvesNonAffectees()
    suspend fun epreuvesDisponibles(atelierId: Int): List<EpreuveEntity> = dao.epreuvesDisponibles(atelierId)
    suspend fun compterEpreuvesActives(): Int = dao.compterEpreuvesActives()
    suspend fun compterAteliers(journeeId: Int): Int = dao.compterAteliers(journeeId)

    /** "25/07/2027" -> "2027-07-25" ; vide -> null ; sinon erreur lisible. */
    private fun dateVersIso(saisie: String): String? {
        val texte = saisie.trim()
        if (texte.isEmpty()) return null
        return try {
            java.time.LocalDate.parse(
                texte,
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(java.time.format.ResolverStyle.STRICT)
            ).toString()
        } catch (e: java.time.format.DateTimeParseException) {
            throw ErreurMetier("La date doit être au format jj/mm/aaaa (ex : 25/07/2027), ou laissée vide.")
        }
    }

    suspend fun creerJournee(nom: String, dateSaisie: String): JourneeEntity {
        val n = nom.trim()
        if (n.isEmpty()) throw ErreurMetier("Le nom de la journée est obligatoire.")
        val j = JourneeEntity(nom = n, dateJournee = dateVersIso(dateSaisie), ordre = dao.prochainOrdreJournee())
        return j.copy(id = dao.insererJournee(j).toInt())
    }

    suspend fun modifierJournee(j: JourneeEntity, nom: String, dateSaisie: String) {
        val n = nom.trim()
        if (n.isEmpty()) throw ErreurMetier("Le nom de la journée est obligatoire.")
        dao.majJournee(j.copy(nom = n, dateJournee = dateVersIso(dateSaisie), updatedAt = System.currentTimeMillis()))
    }

    /** Supprime la journée et ses ateliers ; les épreuves ne sont pas supprimées (elles redeviennent « à placer »). */
    suspend fun supprimerJournee(id: Int) = dao.supprimerJournee(id)

    suspend fun supprimerAtelier(id: Int) = dao.supprimerAtelier(id)

    /** Crée ou met à jour un atelier et fixe la liste ordonnée de ses épreuves (une épreuve = un seul atelier). */
    suspend fun sauverAtelier(a: AtelierEntity, epreuves: List<EpreuveEntity>) {
        val nom = a.nom.trim()
        val debut = a.heureDebut?.trim().orEmpty()
        val fin = a.heureFin?.trim().orEmpty()
        if (nom.isEmpty()) throw ErreurMetier("Le nom de l'atelier est obligatoire.")
        if (a.journeeId <= 0) throw ErreurMetier("Choisissez la journée de cet atelier.")
        if (debut.isNotEmpty() && !heureValide.matches(debut)) throw ErreurMetier("L'heure de début doit être au format HH:mm (ex : 08:30).")
        if (fin.isNotEmpty() && !heureValide.matches(fin)) throw ErreurMetier("L'heure de fin doit être au format HH:mm (ex : 10:30).")
        if (debut.isNotEmpty() && fin.isNotEmpty() && fin <= debut) throw ErreurMetier("L'heure de fin doit être après l'heure de début.")

        val propre = a.copy(
            nom = nom, heureDebut = debut.ifEmpty { null }, heureFin = fin.ifEmpty { null },
            lieu = a.lieu?.trim()?.ifEmpty { null }, responsable = a.responsable?.trim()?.ifEmpty { null },
            updatedAt = System.currentTimeMillis()
        )
        db.withTransaction {
            val id = if (propre.id == 0) {
                dao.insererAtelier(propre.copy(ordre = dao.prochainOrdreAtelier(propre.journeeId))).toInt()
            } else {
                dao.majAtelier(propre)
                propre.id
            }
            dao.supprimerLiensAtelier(id)
            if (epreuves.isNotEmpty()) {
                dao.retirerEpreuvesDesAteliers(epreuves.map { it.id }) // une épreuve ne peut être que dans un atelier
                dao.insererLiensAtelier(epreuves.mapIndexed { i, e -> AtelierEpreuveEntity(atelierId = id, epreuveId = e.id, ordre = i) })
            }
        }
    }

    // ------------------------------------------------------------------ Suivi des participants

    suspend fun pilotesTries(): List<PiloteEntity> =
        dao.tousPilotes().sortedWith(compareBy({ it.nom.lowercase() }, { it.prenom.lowercase() }))

    /** Scores d'un pilote épreuve par épreuve + total et rang au classement général mixte. */
    suspend fun suivi(pilote: PiloteEntity): SuiviParticipant {
        val epreuves = dao.toutesEpreuves()
        val inscrites = dao.epreuvesDuPilote(pilote.id).toSet()
        val resultats = dao.tousResultats().filter { it.piloteId == pilote.id }.associateBy { it.epreuveId }

        val lignes = mutableListOf<LigneSuivi>()
        var saisies = 0
        var attendues = 0
        for (e in epreuves) {
            val r = resultats[e.id]
            if (!e.actif && r == null) continue
            if (e.id !in inscrites && r == null) continue
            attendues++
            if (r == null) {
                lignes.add(LigneSuivi(e.nom, "", null, StatutSuivi.PAS_ENCORE_SAISI, e.compteDansGeneral))
            } else {
                saisies++
                lignes.add(
                    LigneSuivi(
                        e.nom, formaterValeurs(e, r), r.points,
                        if (r.disqualifie) StatutSuivi.DISQUALIFIE else StatutSuivi.SAISI, e.compteDansGeneral
                    )
                )
            }
        }
        val classement = classementGeneral(FiltreGenre.MIXTE)
        val ligne = classement.firstOrNull { it.pilote.id == pilote.id }
        return SuiviParticipant(pilote, lignes, ligne?.total ?: 0.0, ligne?.rang ?: 0, classement.size, saisies, attendues)
    }

    /** Exemple : « Temps de marche : 12 min ; Balises : 5 ». */
    private suspend fun formaterValeurs(e: EpreuveEntity, r: ResultatEntity): String {
        val parNom = dao.parametres(e.id).associateBy { it.nomVariable }
        return r.valeurs().entries.joinToString(" ; ") { (cle, valeur) ->
            val p = parNom[cle]
            val libelle = p?.label ?: cle
            val unite = if (p?.unite.isNullOrBlank() || "(" in libelle) "" else " ${p?.unite}"
            "$libelle : ${fmt(valeur)}$unite"
        }
    }
}
