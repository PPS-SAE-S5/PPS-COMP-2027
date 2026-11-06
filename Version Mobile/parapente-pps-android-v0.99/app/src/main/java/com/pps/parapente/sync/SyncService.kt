package com.pps.parapente.sync

import androidx.room.withTransaction
import com.pps.parapente.data.*
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.OffsetDateTime

/**
 * Synchronise la base locale (Room/SQLite) avec la base Supabase (PostgreSQL), dans les deux sens.
 * Même principe que la version PC (voir sync/SyncService.java côté JavaFX) :
 *  - l'id local recommence à 1 sur chaque appareil, donc on fait correspondre les lignes par
 *    une clé "naturelle" stable : identifiant (comptes), numéro de licence (pilotes), uuid
 *    (épreuves, sans clé naturelle), couple pilote+épreuve (inscriptions, résultats).
 *  - "supabaseId" (sur pilotes et épreuves) mémorise l'id numérique attribué par Supabase,
 *    nécessaire pour les clés étrangères des inscriptions/résultats/variables/barèmes.
 *  - Les variables et barèmes d'une épreuve sont remplacés en bloc à chaque synchronisation
 *    (même logique que la modification d'une épreuve dans l'application).
 *
 * Choix assumé (comme sur PC) : les comptes utilisateurs SONT synchronisés, mot de passe
 * haché inclus — acceptable pour un outil interne au club.
 */
class SyncService(private val db: AppDatabase, config: SyncConfig) {

    private val dao = db.dao()
    private val client: SupabaseClient

    /** Vrai pendant une réception "miroir" : les données distantes écrasent les locales, sans comparer les dates. */
    private var ecraser = false

    init {
        if (!config.estConfigure()) throw IllegalStateException("La synchronisation Supabase n'est pas configurée (URL et clé nécessaires).")
        client = SupabaseClient(config.url, config.cleAnon)
    }

    suspend fun testerConnexion() {
        client.ping()
    }

    /** Fusion dans les deux sens, sans aucune suppression. */
    suspend fun synchroniser(): RapportSync {
        val rapport = RapportSync()
        // Test rapide AVANT d'ouvrir la transaction : sans réseau on échoue en quelques secondes,
        // sans jamais immobiliser la base locale pendant que l'on attend le réseau.
        client.ping()
        db.withTransaction {
            tirerUtilisateurs(rapport)
            tirerPilotes(rapport)
            tirerEpreuves(rapport)
            tirerPlanningSecurise(rapport)
            tirerInscriptions(rapport)
            tirerResultats(rapport)

            pousserUtilisateurs(rapport)
            pousserPilotes(rapport)
            pousserEpreuves(rapport)
            pousserPlanningSecurise(rapport)
            pousserInscriptions(rapport)
            pousserResultats(rapport)
        }
        return rapport
    }

    /**
     * Réception « douce » : récupère les nouveautés de la base en ligne (fusion par date de modification)
     * SANS rien envoyer et SANS rien supprimer. C'est ce qu'utilise la synchronisation automatique au démarrage :
     *  - un appareil resté en retard ne peut plus RENVOYER en ligne des données supprimées entre-temps ;
     *  - une coupure réseau au démarrage ne peut plus interrompre un ENVOI (suppression puis réécriture des
     *    variables/barèmes d'une épreuve), qui laissait des épreuves vides en ligne.
     */
    suspend fun recevoirNouveautes(): RapportSync {
        val rapport = RapportSync()
        client.ping()
        db.withTransaction {
            ecraser = false
            tirerUtilisateurs(rapport)
            tirerPilotes(rapport)
            tirerEpreuves(rapport)
            tirerPlanningSecurise(rapport)
            tirerInscriptions(rapport)
            tirerResultats(rapport)
        }
        return rapport
    }

    // ============================================================ RECEVOIR / ENVOYER (mode "miroir")

    /**
     * Base en ligne -> application (équivalent d'un "git pull").
     * Après l'opération, pilotes, épreuves, inscriptions et résultats de l'application sont
     * IDENTIQUES à ceux de la base en ligne : ce qui n'existe pas en ligne est supprimé en local.
     * Les comptes utilisateurs sont aussi alignés (un compte supprimé en ligne est supprimé ici),
     * sauf le compte « admin » qui n'est jamais supprimé.
     */
    suspend fun recevoirDepuisSupabase(): RapportSync {
        val rapport = RapportSync()
        client.ping()
        // Lectures réseau faites AVANT d'ouvrir la transaction locale.
        val distPilotes = client.recuperer("pilotes", "numero_licence")
        val distEpreuves = client.recuperer("epreuves", "uuid")

        // Garde-fou : base en ligne vide + base locale remplie = probablement une erreur
        // (mauvaise URL, tables vidées...). On refuse plutôt que d'effacer tout l'appareil.
        if (distPilotes.length() == 0 && distEpreuves.length() == 0 &&
            (dao.tousPilotes().isNotEmpty() || dao.toutesEpreuves().isNotEmpty())
        ) {
            throw ErreurMetier("La base en ligne est vide : réception annulée pour ne pas effacer les données de cet appareil.")
        }

        val licences = (0 until distPilotes.length()).map { cle(distPilotes.getJSONObject(it).texteOuNull("numero_licence")) }.toSet()
        val uuids = (0 until distEpreuves.length()).map { cle(distEpreuves.getJSONObject(it).texteOuNull("uuid")) }.toSet()
        val inscriptionsDistantes = clesLiaisonsDistantes("inscriptions_epreuve")
        val resultatsDistants = clesLiaisonsDistantes("resultats")
        val distUtilisateurs = client.recuperer("utilisateurs", "identifiant")
        val identifiantsDistants = (0 until distUtilisateurs.length())
            .map { cle(distUtilisateurs.getJSONObject(it).texteOuNull("identifiant")) }.toSet()
        val planningDistant = lireUuidsPlanningDistant() // null si les tables du planning n'existent pas en ligne

        try {
            db.withTransaction {
                ecraser = false
                tirerUtilisateurs(rapport)

                ecraser = true
                tirerPilotes(rapport)
                tirerEpreuves(rapport)
                if (planningDistant != null) tirerPlanning(rapport) else rapport.ajouterAvertissement(messagePlanningAbsent)
                tirerInscriptions(rapport)
                tirerResultats(rapport)

                // Épreuves et pilotes absents en ligne : leurs inscriptions/résultats/variables/barèmes
                // partent avec eux (ON DELETE CASCADE de Room).
                for (e in dao.toutesEpreuves()) {
                    if (cle(e.uuid) !in uuids) { dao.supprimerEpreuve(e.id); rapport.epreuvesSupprimees++ }
                }
                for (p in dao.tousPilotes()) {
                    if (cle(p.numeroLicence) !in licences) { dao.supprimerPilote(p.id); rapport.pilotesSupprimes++ }
                }

                // Journées et ateliers supprimés en ligne : supprimés ici aussi.
                if (planningDistant != null) supprimerPlanningLocalAbsentDuDistant(rapport, planningDistant.first, planningDistant.second)

                // Comptes supprimés en ligne : supprimés ici aussi (jamais « admin »).
                for (u in dao.tousUtilisateurs()) {
                    if (u.identifiant != "admin" && cle(u.identifiant) !in identifiantsDistants) {
                        dao.supprimerUtilisateur(u.id)
                        rapport.utilisateursSupprimes++
                    }
                }

                // Inscriptions / résultats isolés absents en ligne (pilote et épreuve existent encore).
                val pilotesParId = dao.tousPilotes().associateBy { it.id }
                val epreuvesParId = dao.toutesEpreuves().associateBy { it.id }
                for (ins in dao.toutesInscriptions()) {
                    val k = cleLiaison(pilotesParId[ins.piloteId]?.numeroLicence, epreuvesParId[ins.epreuveId]?.uuid)
                    if (k !in inscriptionsDistantes) { dao.supprimerInscription(ins.id); rapport.inscriptionsSupprimees++ }
                }
                for (res in dao.tousResultats()) {
                    val k = cleLiaison(pilotesParId[res.piloteId]?.numeroLicence, epreuvesParId[res.epreuveId]?.uuid)
                    if (k !in resultatsDistants) { dao.supprimerResultat(res.id); rapport.resultatsSupprimes++ }
                }
            }
        } finally {
            ecraser = false
        }
        return rapport
    }

    /**
     * Application -> base en ligne (équivalent d'un "git push").
     * Après l'opération, la base en ligne est IDENTIQUE à l'application : ce qui n'existe pas
     * en local est supprimé en ligne (donc pour tous les appareils).
     */
    suspend fun envoyerVersSupabase(): RapportSync {
        val rapport = RapportSync()
        client.ping()

        // Garde-fou : base locale vide + base en ligne remplie = on refuse de tout effacer en ligne.
        if (dao.tousPilotes().isEmpty() && dao.toutesEpreuves().isEmpty() &&
            (client.recuperer("pilotes", "id").length() > 0 || client.recuperer("epreuves", "id").length() > 0)
        ) {
            throw ErreurMetier("La base de cet appareil est vide : envoi annulé pour ne pas effacer la base en ligne.")
        }

        db.withTransaction {
            pousserUtilisateurs(rapport)
            pousserPilotes(rapport)
            pousserEpreuves(rapport)
            val planningEnvoye = pousserPlanningSecurise(rapport)
            pousserInscriptions(rapport)
            pousserResultats(rapport)
            supprimerDistantAbsentsDuLocal(rapport)
            if (planningEnvoye) supprimerPlanningDistantAbsentDuLocal(rapport)
        }
        return rapport
    }

    private suspend fun supprimerDistantAbsentsDuLocal(rapport: RapportSync) {
        val pilotesLocaux = dao.tousPilotes()
        val epreuvesLocales = dao.toutesEpreuves()
        val licences = pilotesLocaux.map { cle(it.numeroLicence) }.toSet()
        val uuids = epreuvesLocales.map { cle(it.uuid) }.toSet()
        val pilotesParId = pilotesLocaux.associateBy { it.id }
        val epreuvesParId = epreuvesLocales.associateBy { it.id }
        val inscriptionsLocales = dao.toutesInscriptions()
            .map { cleLiaison(pilotesParId[it.piloteId]?.numeroLicence, epreuvesParId[it.epreuveId]?.uuid) }.toSet()
        val resultatsLocaux = dao.tousResultats()
            .map { cleLiaison(pilotesParId[it.piloteId]?.numeroLicence, epreuvesParId[it.epreuveId]?.uuid) }.toSet()

        // Épreuves absentes en local : on efface explicitement leurs enfants côté serveur
        // (on ne suppose pas que Supabase a des ON DELETE CASCADE), puis l'épreuve elle-même.
        val epreuves = client.recuperer("epreuves", "id,uuid")
        for (i in 0 until epreuves.length()) {
            val r = epreuves.getJSONObject(i)
            if (cle(r.texteOuNull("uuid")) in uuids) continue
            val id = r.getInt("id")
            for (enfant in listOf("resultats", "inscriptions_epreuve", "epreuve_parametres", "bareme_points")) {
                client.supprimerOu(enfant, "epreuve_id=eq.$id")
            }
            supprimerLiensPlanningDistants("epreuve_id=eq.$id")
            client.supprimerOu("epreuves", "id=eq.$id")
            rapport.epreuvesSupprimees++
        }

        val pilotes = client.recuperer("pilotes", "id,numero_licence")
        for (i in 0 until pilotes.length()) {
            val r = pilotes.getJSONObject(i)
            if (cle(r.texteOuNull("numero_licence")) in licences) continue
            val id = r.getInt("id")
            client.supprimerOu("resultats", "pilote_id=eq.$id")
            client.supprimerOu("inscriptions_epreuve", "pilote_id=eq.$id")
            client.supprimerOu("pilotes", "id=eq.$id")
            rapport.pilotesSupprimes++
        }

        // Comptes absents en local : supprimés en ligne (jamais « admin »).
        val identifiantsLocaux = dao.tousUtilisateurs().map { cle(it.identifiant) }.toSet()
        val comptes = client.recuperer("utilisateurs", "identifiant")
        for (i in 0 until comptes.length()) {
            val identifiant = comptes.getJSONObject(i).optString("identifiant", "")
            if (identifiant.isEmpty() || identifiant == "admin" || cle(identifiant) in identifiantsLocaux) continue
            val valeur = java.net.URLEncoder.encode(identifiant, "UTF-8").replace("+", "%20")
            client.supprimerOu("utilisateurs", "identifiant=eq.$valeur")
            rapport.utilisateursSupprimes++
        }

        rapport.inscriptionsSupprimees += supprimerLiaisonsDistantesAbsentes("inscriptions_epreuve", inscriptionsLocales)
        rapport.resultatsSupprimes += supprimerLiaisonsDistantesAbsentes("resultats", resultatsLocaux)
    }

    private suspend fun supprimerLiaisonsDistantesAbsentes(table: String, clesLocales: Set<String>): Int {
        var n = 0
        val lignes = client.recuperer(table, "pilote_id,epreuve_id,pilotes(numero_licence),epreuves(uuid)")
        for (i in 0 until lignes.length()) {
            val r = lignes.getJSONObject(i)
            val p = r.optJSONObject("pilotes") ?: continue
            val e = r.optJSONObject("epreuves") ?: continue
            val k = cleLiaison(p.texteOuNull("numero_licence"), e.texteOuNull("uuid"))
            if (k in clesLocales) continue
            client.supprimerOu(table, "pilote_id=eq.${r.getInt("pilote_id")}&epreuve_id=eq.${r.getInt("epreuve_id")}")
            n++
        }
        return n
    }

    /** Clés métier "licence|uuid_epreuve" des lignes distantes d'une table de liaison (inscriptions, résultats). */
    private suspend fun clesLiaisonsDistantes(table: String): Set<String> {
        val cles = mutableSetOf<String>()
        val lignes = client.recuperer(table, "pilote_id,epreuve_id,pilotes(numero_licence),epreuves(uuid)")
        for (i in 0 until lignes.length()) {
            val r = lignes.getJSONObject(i)
            val p = r.optJSONObject("pilotes") ?: continue
            val e = r.optJSONObject("epreuves") ?: continue
            cles.add(cleLiaison(p.texteOuNull("numero_licence"), e.texteOuNull("uuid")))
        }
        return cles
    }

    /** Normalise une clé métier (licence ou uuid) pour la comparer sans souci de majuscules/espaces. */
    private fun cle(valeur: String?): String = valeur?.trim()?.lowercase() ?: ""

    private fun cleLiaison(licence: String?, uuid: String?): String = cle(licence) + "|" + cle(uuid)

    // ============================================================ UTILITAIRES

    private fun lireInstant(o: JSONObject, champ: String): Long? {
        val s = o.texteOuNull(champ) ?: return null
        if (s.isBlank()) return null
        return try {
            OffsetDateTime.parse(s).toInstant().toEpochMilli()
        } catch (e: Exception) {
            try { Instant.parse(s).toEpochMilli() } catch (e2: Exception) { null }
        }
    }

    private fun estPlusRecent(distante: Long?, locale: Long?): Boolean {
        if (ecraser) return true
        if (distante == null) return false
        return locale == null || distante > locale
    }

    /**
     * Lit un texte JSON en renvoyant un VRAI null quand la valeur est null.
     * Sur Android, optString(champ, null) renvoie la chaîne "null" pour un null JSON (ex : departement ou genre
     * vides côté Supabase), ce qui polluait la base locale et faisait disparaître des pilotes des classements
     * Hommes/Femmes. isNull() se comporte pareil partout.
     */
    private fun JSONObject.texteOuNull(cle: String): String? = if (isNull(cle)) null else optString(cle, null)

    private fun versIso(epochMillis: Long): String = Instant.ofEpochMilli(epochMillis).toString()

    private fun mettreOuNull(o: JSONObject, champ: String, valeur: Any?) {
        o.put(champ, valeur ?: JSONObject.NULL)
    }

    // ============================================================ COMPTES UTILISATEURS

    private suspend fun tirerUtilisateurs(rapport: RapportSync) {
        val distantes = client.recuperer("utilisateurs", "*")
        val locaux = dao.tousUtilisateurs().associateBy { it.identifiant }.toMutableMap()
        for (i in 0 until distantes.length()) {
            val r = distantes.getJSONObject(i)
            val identifiant = r.getString("identifiant")
            val majDistante = lireInstant(r, "updated_at")
            val local = locaux[identifiant]
            if (local != null) {
                if (estPlusRecent(majDistante, local.updatedAt)) {
                    dao.majUtilisateur(
                        local.copy(
                            motDePasse = r.getString("mot_de_passe"),
                            role = r.getString("role"),
                            actif = r.getBoolean("actif"),
                            updatedAt = majDistante ?: System.currentTimeMillis()
                        )
                    )
                    rapport.utilisateursRecus++
                }
            } else {
                dao.insererUtilisateur(
                    UtilisateurEntity(
                        identifiant = identifiant, motDePasse = r.getString("mot_de_passe"),
                        role = r.getString("role"), actif = r.getBoolean("actif"),
                        updatedAt = majDistante ?: System.currentTimeMillis()
                    )
                )
                rapport.utilisateursRecus++
            }
        }
    }

    private suspend fun pousserUtilisateurs(rapport: RapportSync) {
        for (u in dao.tousUtilisateurs()) {
            val payload = JSONObject().apply {
                put("identifiant", u.identifiant)
                put("mot_de_passe", u.motDePasse)
                put("role", u.role)
                put("actif", u.actif)
                put("updated_at", versIso(u.updatedAt))
            }
            client.envoyer("utilisateurs", payload, "identifiant")
            rapport.utilisateursEnvoyes++
        }
    }

    // ============================================================ PILOTES

    private suspend fun tirerPilotes(rapport: RapportSync) {
        val distantes = client.recuperer("pilotes", "*")
        val locaux = dao.tousPilotes().associateBy { it.numeroLicence }
        for (i in 0 until distantes.length()) {
            val r = distantes.getJSONObject(i)
            val licence = r.getString("numero_licence")
            val majDistante = lireInstant(r, "updated_at")
            val local = locaux[licence]
            if (local != null) {
                if (estPlusRecent(majDistante, local.updatedAt)) {
                    dao.majPilote(
                        local.copy(
                            nom = r.getString("nom"), prenom = r.getString("prenom"),
                            departement = r.texteOuNull("departement"),
                            poids = if (r.isNull("poids")) null else r.getDouble("poids"),
                            email = r.texteOuNull("email"),
                            anneeNaissance = if (r.isNull("annee_naissance")) null else r.getInt("annee_naissance"),
                            genre = r.texteOuNull("genre"),
                            updatedAt = majDistante ?: System.currentTimeMillis()
                        )
                    )
                    rapport.pilotesRecus++
                }
            } else {
                dao.insererPilote(
                    PiloteEntity(
                        numeroLicence = licence, nom = r.getString("nom"), prenom = r.getString("prenom"),
                        departement = r.texteOuNull("departement"),
                        poids = if (r.isNull("poids")) null else r.getDouble("poids"),
                        email = r.texteOuNull("email"),
                        anneeNaissance = if (r.isNull("annee_naissance")) null else r.getInt("annee_naissance"),
                        genre = r.texteOuNull("genre"),
                        updatedAt = majDistante ?: System.currentTimeMillis()
                    )
                )
                rapport.pilotesRecus++
            }
        }
    }

    private suspend fun pousserPilotes(rapport: RapportSync) {
        for (p in dao.tousPilotes()) {
            val payload = JSONObject().apply {
                put("numero_licence", p.numeroLicence)
                put("nom", p.nom)
                put("prenom", p.prenom)
                mettreOuNull(this, "departement", p.departement)
                mettreOuNull(this, "poids", p.poids)
                mettreOuNull(this, "email", p.email)
                mettreOuNull(this, "annee_naissance", p.anneeNaissance)
                mettreOuNull(this, "genre", p.genre)
                put("updated_at", versIso(p.updatedAt))
            }
            // Fusion par numéro de licence : évite les doublons si deux appareils ont inscrit
            // le même pilote indépendamment avant leur première synchronisation.
            val retour = client.envoyer("pilotes", payload, "numero_licence")
            dao.definirSupabaseIdPilote(p.id, retour.getInt("id"))
            rapport.pilotesEnvoyes++
        }
    }

    // ============================================================ ÉPREUVES (+ variables + barème)

    private suspend fun tirerEpreuves(rapport: RapportSync) {
        val distantes = client.recuperer("epreuves", "*,epreuve_parametres(*),bareme_points(*)")
        val locaux = dao.toutesEpreuves().associateBy { it.uuid }
        for (i in 0 until distantes.length()) {
            val r = distantes.getJSONObject(i)
            val uuid = r.getString("uuid")
            val majDistante = lireInstant(r, "updated_at")
            val local = locaux[uuid]
            var localId: Int? = null
            var remplacerEnfants = false

            if (local != null) {
                if (estPlusRecent(majDistante, local.updatedAt)) {
                    val maj = local.copy(
                        nom = r.getString("nom"), description = r.texteOuNull("description"),
                        modeCalcul = r.getString("mode_calcul"), formule = r.texteOuNull("formule"),
                        valeurCle = r.texteOuNull("valeur_cle"), sensClassement = r.getString("sens_classement"),
                        afficherClassement = r.getBoolean("afficher_classement"),
                        compteDansGeneral = r.getBoolean("compte_dans_general"),
                        actif = r.getBoolean("actif"), ordre = r.optInt("ordre", 0),
                        updatedAt = majDistante ?: System.currentTimeMillis()
                    )
                    dao.majEpreuve(maj)
                    localId = local.id
                    remplacerEnfants = true
                    rapport.epreuvesRecues++
                }
            } else {
                val nouvelle = EpreuveEntity(
                    uuid = uuid, nom = r.getString("nom"), description = r.texteOuNull("description"),
                    modeCalcul = r.getString("mode_calcul"), formule = r.texteOuNull("formule"),
                    valeurCle = r.texteOuNull("valeur_cle"), sensClassement = r.getString("sens_classement"),
                    afficherClassement = r.getBoolean("afficher_classement"),
                    compteDansGeneral = r.getBoolean("compte_dans_general"),
                    actif = r.getBoolean("actif"), ordre = r.optInt("ordre", 0),
                    updatedAt = majDistante ?: System.currentTimeMillis()
                )
                localId = dao.insererEpreuve(nouvelle).toInt()
                remplacerEnfants = true
                rapport.epreuvesRecues++
            }

            if (remplacerEnfants && localId != null) {
                val parametres = r.optJSONArray("epreuve_parametres")
                val bareme = r.optJSONArray("bareme_points")
                val distantSansVariables = parametres == null || parametres.length() == 0
                val distantSansBareme = bareme == null || bareme.length() == 0

                // GARDE-FOU : une épreuve sans aucune variable (ou, en mode Barème, sans barème) en ligne alors que
                // l'appareil en possède est presque toujours le signe d'un envoi interrompu par une coupure réseau
                // (les variables sont supprimées puis réécrites en deux requêtes). On garde alors les données locales
                // au lieu de les vider : la prochaine synchronisation réparera la base en ligne.
                val garderVariables = distantSansVariables && dao.parametres(localId).isNotEmpty()
                val garderBareme = r.getString("mode_calcul") == Mode.BAREME && distantSansBareme && dao.bareme(localId).isNotEmpty()

                if (!garderVariables) {
                    dao.supprimerParametres(localId)
                    if (parametres != null && parametres.length() > 0) {
                        val liste = (0 until parametres.length()).map { j ->
                            val p = parametres.getJSONObject(j)
                            ParametreEntity(
                                epreuveId = localId, nomVariable = p.getString("nom_variable"),
                                label = p.getString("label"), unite = p.texteOuNull("unite"),
                                obligatoire = p.optBoolean("obligatoire", true), ordre = p.optInt("ordre", 0)
                            )
                        }
                        dao.insererParametres(liste)
                    }
                }
                if (!garderBareme) {
                    dao.supprimerBareme(localId)
                    if (bareme != null && bareme.length() > 0) {
                        val liste = (0 until bareme.length()).map { j ->
                            val b = bareme.getJSONObject(j)
                            BaremeEntity(epreuveId = localId, rang = b.getInt("rang"), points = b.getDouble("points"))
                        }
                        dao.insererBareme(liste)
                    }
                }
            }
        }
    }

    private suspend fun pousserEpreuves(rapport: RapportSync) {
        for (e in dao.toutesEpreuves()) {
            val payload = JSONObject().apply {
                put("uuid", e.uuid)
                put("nom", e.nom)
                mettreOuNull(this, "description", e.description)
                put("mode_calcul", e.modeCalcul)
                mettreOuNull(this, "formule", e.formule)
                mettreOuNull(this, "valeur_cle", e.valeurCle)
                put("sens_classement", e.sensClassement)
                put("afficher_classement", e.afficherClassement)
                put("compte_dans_general", e.compteDansGeneral)
                put("actif", e.actif)
                put("ordre", e.ordre)
                put("updated_at", versIso(e.updatedAt))
            }
            val retour = client.envoyer("epreuves", payload, "uuid")
            val supabaseId = retour.getInt("id")
            dao.definirSupabaseIdEpreuve(e.id, supabaseId)
            rapport.epreuvesEnvoyees++

            // Variables et barème : on remplace tout côté serveur (même principe que localement).
            val parametresPayload = JSONArray()
            dao.parametres(e.id).forEach { p ->
                parametresPayload.put(JSONObject().apply {
                    put("epreuve_id", supabaseId)
                    put("nom_variable", p.nomVariable)
                    put("label", p.label)
                    mettreOuNull(this, "unite", p.unite)
                    put("obligatoire", p.obligatoire)
                    put("ordre", p.ordre)
                })
            }
            // Garde-fou : jamais de suppression en ligne si cet appareil n'a aucune variable pour cette épreuve.
            if (parametresPayload.length() > 0) {
                client.supprimerOu("epreuve_parametres", "epreuve_id=eq.$supabaseId")
                client.inserer("epreuve_parametres", parametresPayload)
            }

            val baremePayload = JSONArray()
            dao.bareme(e.id).forEach { b ->
                baremePayload.put(JSONObject().apply {
                    put("epreuve_id", supabaseId)
                    put("rang", b.rang)
                    put("points", b.points)
                })
            }
            if (baremePayload.length() > 0) {
                client.supprimerOu("bareme_points", "epreuve_id=eq.$supabaseId")
                client.inserer("bareme_points", baremePayload)
            }
        }
    }

    // ============================================================ PLANNING (journées > ateliers > épreuves)

    private val messagePlanningAbsent =
        "Le planning (journées et ateliers) n'a pas été synchronisé : les tables correspondantes n'existent pas encore en ligne. " +
            "Exécutez une fois le script supabase_migration_v5_planning.sql dans l'éditeur SQL de Supabase, puis recommencez."

    /** Vrai si l'erreur signifie « table inexistante » ; une simple coupure réseau, elle, doit faire échouer la synchronisation. */
    private fun tablePlanningAbsente(e: Exception): Boolean {
        val m = (e.message ?: "").lowercase()
        return "code 404" in m || "pgrst205" in m || "42p01" in m || "does not exist" in m || "could not find the table" in m
    }

    /** Réception du planning sans jamais bloquer le reste : tables absentes en ligne -> avertissement, et on continue. */
    private suspend fun tirerPlanningSecurise(rapport: RapportSync): Boolean {
        return try {
            tirerPlanning(rapport)
            true
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException || !tablePlanningAbsente(e)) throw e
            rapport.ajouterAvertissement(messagePlanningAbsent)
            false
        }
    }

    private suspend fun pousserPlanningSecurise(rapport: RapportSync): Boolean {
        return try {
            pousserPlanning(rapport)
            true
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException || !tablePlanningAbsente(e)) throw e
            rapport.ajouterAvertissement(messagePlanningAbsent)
            false
        }
    }

    /** uuid des journées et des ateliers présents en ligne, ou null si les tables du planning n'existent pas encore. */
    private suspend fun lireUuidsPlanningDistant(): Pair<Set<String>, Set<String>>? {
        return try {
            val journees = client.recuperer("journees", "uuid")
            val ateliers = client.recuperer("ateliers", "uuid")
            Pair(
                (0 until journees.length()).map { cle(journees.getJSONObject(it).texteOuNull("uuid")) }.toSet(),
                (0 until ateliers.length()).map { cle(ateliers.getJSONObject(it).texteOuNull("uuid")) }.toSet()
            )
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException || !tablePlanningAbsente(e)) throw e
            null
        }
    }

    private suspend fun tirerPlanning(rapport: RapportSync) {
        val journees = client.recuperer("journees", "*")
        val ateliers = client.recuperer("ateliers", "*,journees(uuid),atelier_epreuves(ordre,epreuves(uuid))")

        for (i in 0 until journees.length()) {
            val r = journees.getJSONObject(i)
            val uuid = r.getString("uuid")
            val majDistante = lireInstant(r, "updated_at")
            val date = r.texteOuNull("date_journee")?.takeIf { it.isNotBlank() }
            val local = dao.journeeParUuid(uuid)
            if (local != null) {
                if (estPlusRecent(majDistante, local.updatedAt)) {
                    dao.majJournee(
                        local.copy(
                            nom = r.getString("nom"), dateJournee = date, ordre = r.optInt("ordre", 0),
                            updatedAt = majDistante ?: System.currentTimeMillis()
                        )
                    )
                    rapport.planningRecu++
                }
            } else {
                dao.insererJournee(
                    JourneeEntity(
                        uuid = uuid, nom = r.getString("nom"), dateJournee = date, ordre = r.optInt("ordre", 0),
                        updatedAt = majDistante ?: System.currentTimeMillis()
                    )
                )
                rapport.planningRecu++
            }
        }

        for (i in 0 until ateliers.length()) {
            val r = ateliers.getJSONObject(i)
            val journeeEmb = r.optJSONObject("journees") ?: continue
            val journeeLocale = dao.journeeParUuid(journeeEmb.texteOuNull("uuid") ?: continue) ?: continue
            val uuid = r.getString("uuid")
            val majDistante = lireInstant(r, "updated_at")
            val local = dao.atelierParUuid(uuid)
            var atelierId: Int? = null

            if (local != null) {
                if (estPlusRecent(majDistante, local.updatedAt)) {
                    dao.majAtelier(
                        local.copy(
                            journeeId = journeeLocale.id, nom = r.getString("nom"), creneau = r.getString("creneau"),
                            heureDebut = r.texteOuNull("heure_debut"), heureFin = r.texteOuNull("heure_fin"),
                            lieu = r.texteOuNull("lieu"), responsable = r.texteOuNull("responsable"),
                            ordre = r.optInt("ordre", 0), updatedAt = majDistante ?: System.currentTimeMillis()
                        )
                    )
                    atelierId = local.id
                    rapport.planningRecu++
                }
            } else {
                atelierId = dao.insererAtelier(
                    AtelierEntity(
                        uuid = uuid, journeeId = journeeLocale.id, nom = r.getString("nom"), creneau = r.getString("creneau"),
                        heureDebut = r.texteOuNull("heure_debut"), heureFin = r.texteOuNull("heure_fin"),
                        lieu = r.texteOuNull("lieu"), responsable = r.texteOuNull("responsable"),
                        ordre = r.optInt("ordre", 0), updatedAt = majDistante ?: System.currentTimeMillis()
                    )
                ).toInt()
                rapport.planningRecu++
            }

            // Épreuves de l'atelier : remplacées en bloc (seulement si l'atelier a été créé/mis à jour ci-dessus)
            if (atelierId != null) {
                dao.supprimerLiensAtelier(atelierId)
                val liens = r.optJSONArray("atelier_epreuves") ?: continue
                for (k in 0 until liens.length()) {
                    val lien = liens.getJSONObject(k)
                    val epreuveEmb = lien.optJSONObject("epreuves") ?: continue
                    val epreuve = dao.epreuveParUuid(epreuveEmb.texteOuNull("uuid") ?: continue) ?: continue
                    // Une épreuve = un seul atelier : on la retire d'un éventuel autre atelier local avant de la placer ici
                    dao.retirerEpreuvesDesAteliers(listOf(epreuve.id))
                    dao.insererLiensAtelier(listOf(AtelierEpreuveEntity(atelierId = atelierId, epreuveId = epreuve.id, ordre = lien.optInt("ordre", 0))))
                }
            }
        }
    }

    private suspend fun pousserPlanning(rapport: RapportSync) {
        // 1) Journées
        val journeesLocales = dao.toutesJournees()
        val idJourneeDistant = mutableMapOf<Int, Int>() // id local -> id Supabase
        for (j in journeesLocales) {
            val payload = JSONObject().apply {
                put("uuid", j.uuid)
                put("nom", j.nom)
                mettreOuNull(this, "date_journee", j.dateJournee)
                put("ordre", j.ordre)
                put("updated_at", versIso(j.updatedAt))
            }
            idJourneeDistant[j.id] = client.envoyer("journees", payload, "uuid").getInt("id")
            rapport.planningEnvoye++
        }

        // 2) Ateliers (les liens avec les épreuves sont posés ensuite, une fois tous les ateliers envoyés)
        val epreuvesParId = dao.toutesEpreuves().associateBy { it.id } // relues APRÈS l'envoi des épreuves : supabaseId à jour
        val liensLocaux = dao.tousLiensAtelier().groupBy { it.atelierId }
        val liensParAtelierDistant = linkedMapOf<Int, List<IntArray>>() // id Supabase de l'atelier -> [(id Supabase épreuve, ordre)]
        for (a in dao.tousAteliers()) {
            val journeeDistantId = idJourneeDistant[a.journeeId] ?: continue
            val payload = JSONObject().apply {
                put("uuid", a.uuid)
                put("journee_id", journeeDistantId)
                put("nom", a.nom)
                put("creneau", a.creneau)
                mettreOuNull(this, "heure_debut", a.heureDebut)
                mettreOuNull(this, "heure_fin", a.heureFin)
                mettreOuNull(this, "lieu", a.lieu)
                mettreOuNull(this, "responsable", a.responsable)
                put("ordre", a.ordre)
                put("updated_at", versIso(a.updatedAt))
            }
            val atelierDistantId = client.envoyer("ateliers", payload, "uuid").getInt("id")
            rapport.planningEnvoye++
            liensParAtelierDistant[atelierDistantId] = (liensLocaux[a.id] ?: emptyList())
                .sortedBy { it.ordre }
                .mapNotNull { l -> epreuvesParId[l.epreuveId]?.supabaseId?.let { sid -> intArrayOf(sid, l.ordre) } }
        }

        // 3) Épreuves des ateliers : on efface d'abord TOUS les anciens liens concernés (côté atelier ET côté épreuve,
        //    pour respecter « une épreuve = un seul atelier » quand une épreuve a changé d'atelier), puis on réécrit.
        for ((atelierDistantId, liens) in liensParAtelierDistant) {
            client.supprimerOu("atelier_epreuves", "atelier_id=eq.$atelierDistantId")
            for (lien in liens) client.supprimerOu("atelier_epreuves", "epreuve_id=eq.${lien[0]}")
        }
        for ((atelierDistantId, liens) in liensParAtelierDistant) {
            val lignes = JSONArray()
            for (lien in liens) {
                lignes.put(
                    JSONObject().apply {
                        put("atelier_id", atelierDistantId)
                        put("epreuve_id", lien[0])
                        put("ordre", lien[1])
                    }
                )
            }
            client.inserer("atelier_epreuves", lignes)
        }
    }

    /** Réception miroir : supprime en local les journées et ateliers absents en ligne. */
    private suspend fun supprimerPlanningLocalAbsentDuDistant(rapport: RapportSync, uuidsJournees: Set<String>, uuidsAteliers: Set<String>) {
        // Même garde-fou que pour les épreuves : un planning en ligne totalement vide n'efface pas le planning local
        if (uuidsJournees.isEmpty() && uuidsAteliers.isEmpty()) return
        for (a in dao.tousAteliers()) {
            if (cle(a.uuid) !in uuidsAteliers) { dao.supprimerAtelier(a.id); rapport.planningSupprime++ }
        }
        for (j in dao.toutesJournees()) {
            if (cle(j.uuid) !in uuidsJournees) { dao.supprimerJournee(j.id); rapport.planningSupprime++ }
        }
    }

    /** Envoi miroir : supprime en ligne les journées et ateliers absents de cet appareil. */
    private suspend fun supprimerPlanningDistantAbsentDuLocal(rapport: RapportSync) {
        val ateliersLocaux = dao.tousAteliers().map { cle(it.uuid) }.toSet()
        val journeesLocales = dao.toutesJournees().map { cle(it.uuid) }.toSet()
        val ateliers = client.recuperer("ateliers", "id,uuid,journee_id")
        val journees = client.recuperer("journees", "id,uuid")

        // Garde-fou : cet appareil n'a AUCUN planning (ex. il vient d'être mis à jour) alors qu'il en existe un en ligne :
        // on ne l'efface surtout pas, on prévient.
        if (ateliersLocaux.isEmpty() && journeesLocales.isEmpty() && (ateliers.length() > 0 || journees.length() > 0)) {
            rapport.ajouterAvertissement(
                "Le planning en ligne n'a pas été touché : cet appareil n'a pas de planning. " +
                    "Utilisez d'abord « base en ligne → application » pour le récupérer."
            )
            return
        }

        for (i in 0 until ateliers.length()) {
            val r = ateliers.getJSONObject(i)
            if (cle(r.texteOuNull("uuid")) in ateliersLocaux) continue
            val id = r.getInt("id")
            supprimerLiensPlanningDistants("atelier_id=eq.$id")
            client.supprimerOu("ateliers", "id=eq.$id")
            rapport.planningSupprime++
        }
        // Ateliers restants rattachés à une journée à supprimer : on les retire d'abord
        val ateliersRestants = client.recuperer("ateliers", "id,journee_id")
        for (i in 0 until journees.length()) {
            val r = journees.getJSONObject(i)
            if (cle(r.texteOuNull("uuid")) in journeesLocales) continue
            val id = r.getInt("id")
            for (k in 0 until ateliersRestants.length()) {
                val enfant = ateliersRestants.getJSONObject(k)
                if (enfant.optInt("journee_id", -1) != id) continue
                val atelierId = enfant.getInt("id")
                supprimerLiensPlanningDistants("atelier_id=eq.$atelierId")
                client.supprimerOu("ateliers", "id=eq.$atelierId")
            }
            client.supprimerOu("journees", "id=eq.$id")
            rapport.planningSupprime++
        }
    }

    /** Supprime des lignes de atelier_epreuves en ligne ; ignore le cas où la table n'existe pas encore (script v5 non exécuté). */
    private suspend fun supprimerLiensPlanningDistants(filtre: String) {
        try {
            client.supprimerOu("atelier_epreuves", filtre)
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException || !tablePlanningAbsente(e)) throw e
        }
    }

    // ============================================================ INSCRIPTIONS

    private suspend fun tirerInscriptions(rapport: RapportSync) {
        val distantes = client.recuperer("inscriptions_epreuve", "*,pilotes(numero_licence),epreuves(uuid)")
        val pilotesParLicence = dao.tousPilotes().associateBy { it.numeroLicence }
        val epreuvesParUuid = dao.toutesEpreuves().associateBy { it.uuid }
        for (i in 0 until distantes.length()) {
            val r = distantes.getJSONObject(i)
            val piloteEmb = r.optJSONObject("pilotes") ?: continue
            val epreuveEmb = r.optJSONObject("epreuves") ?: continue
            val pilote = pilotesParLicence[piloteEmb.texteOuNull("numero_licence")] ?: continue
            val epreuve = epreuvesParUuid[epreuveEmb.texteOuNull("uuid")] ?: continue

            if (dao.inscriptionExiste(pilote.id, epreuve.id) == 0) {
                dao.insererInscription(InscriptionEntity(piloteId = pilote.id, epreuveId = epreuve.id))
                rapport.inscriptionsRecues++
            }
        }
    }

    private suspend fun pousserInscriptions(rapport: RapportSync) {
        val pilotesParId = dao.tousPilotes().associateBy { it.id }
        val epreuvesParId = dao.toutesEpreuves().associateBy { it.id }
        for (ins in dao.toutesInscriptions()) {
            val piloteSid = pilotesParId[ins.piloteId]?.supabaseId ?: continue
            val epreuveSid = epreuvesParId[ins.epreuveId]?.supabaseId ?: continue
            val payload = JSONObject().apply {
                put("pilote_id", piloteSid)
                put("epreuve_id", epreuveSid)
            }
            client.envoyer("inscriptions_epreuve", payload, "pilote_id,epreuve_id")
            rapport.inscriptionsEnvoyees++
        }
    }

    // ============================================================ RÉSULTATS

    private suspend fun tirerResultats(rapport: RapportSync) {
        val distantes = client.recuperer("resultats", "*,pilotes(numero_licence),epreuves(uuid)")
        val pilotesParLicence = dao.tousPilotes().associateBy { it.numeroLicence }
        val epreuvesParUuid = dao.toutesEpreuves().associateBy { it.uuid }
        for (i in 0 until distantes.length()) {
            val r = distantes.getJSONObject(i)
            val piloteEmb = r.optJSONObject("pilotes") ?: continue
            val epreuveEmb = r.optJSONObject("epreuves") ?: continue
            val pilote = pilotesParLicence[piloteEmb.texteOuNull("numero_licence")] ?: continue
            val epreuve = epreuvesParUuid[epreuveEmb.texteOuNull("uuid")] ?: continue

            val majDistante = lireInstant(r, "updated_at")
            val existant = dao.resultat(epreuve.id, pilote.id)
            if (existant != null) {
                if (estPlusRecent(majDistante, existant.updatedAt)) {
                    dao.enregistrerResultat(
                        existant.copy(
                            valeursJson = r.optString("valeurs_json", "{}"),
                            points = r.optDouble("points", 0.0),
                            disqualifie = r.optBoolean("disqualifie", false),
                            saisiPar = r.texteOuNull("saisi_par"),
                            updatedAt = majDistante ?: System.currentTimeMillis()
                        )
                    )
                    rapport.resultatsRecus++
                }
            } else {
                dao.enregistrerResultat(
                    ResultatEntity(
                        epreuveId = epreuve.id, piloteId = pilote.id,
                        valeursJson = r.optString("valeurs_json", "{}"),
                        points = r.optDouble("points", 0.0),
                        disqualifie = r.optBoolean("disqualifie", false),
                        saisiPar = r.texteOuNull("saisi_par"),
                        updatedAt = majDistante ?: System.currentTimeMillis()
                    )
                )
                rapport.resultatsRecus++
            }
        }
    }

    private suspend fun pousserResultats(rapport: RapportSync) {
        val pilotesParId = dao.tousPilotes().associateBy { it.id }
        val epreuvesParId = dao.toutesEpreuves().associateBy { it.id }
        for (res in dao.tousResultats()) {
            val piloteSid = pilotesParId[res.piloteId]?.supabaseId ?: continue
            val epreuveSid = epreuvesParId[res.epreuveId]?.supabaseId ?: continue
            val payload = JSONObject().apply {
                put("pilote_id", piloteSid)
                put("epreuve_id", epreuveSid)
                put("valeurs_json", res.valeursJson)
                put("points", res.points)
                put("disqualifie", res.disqualifie)
                mettreOuNull(this, "saisi_par", res.saisiPar)
                put("updated_at", versIso(res.updatedAt))
            }
            client.envoyer("resultats", payload, "epreuve_id,pilote_id")
            rapport.resultatsEnvoyes++
        }
    }
}
