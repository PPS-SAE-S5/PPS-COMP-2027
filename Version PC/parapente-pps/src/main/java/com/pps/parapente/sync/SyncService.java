package com.pps.parapente.sync;

import com.pps.parapente.dao.DatabaseManager;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.*;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Synchronise la base locale (H2) avec la base Supabase (PostgreSQL), dans les deux sens.
 *
 * Principe : l'id local (auto-incrémenté) recommence à 1 sur chaque appareil et ne peut donc
 * jamais servir à faire correspondre les lignes entre appareils. On utilise à la place :
 *  - pour les pilotes : le numéro de licence (déjà unique dans l'application)
 *  - pour les épreuves : un identifiant "uuid" généré automatiquement par la base
 *  - pour les inscriptions et les résultats : le couple (pilote, épreuve), déjà unique
 * Les variables et barèmes d'une épreuve sont remplacés en bloc à chaque synchronisation
 * (même logique que la modification d'une épreuve dans l'application : on efface puis on réécrit).
 *
 * Les comptes de connexion (utilisateurs) sont aussi synchronisés, par identifiant (déjà unique) :
 * mot de passe (haché), rôle et statut actif/inactif. Choix assumé pour un outil interne au club
 * (pas de données sensibles côté public) ; à revoir si l'application devait un jour être exposée
 * plus largement.
 */
public class SyncService {

    private final SupabaseClient client;

    /** Vrai pendant une réception "miroir" : les données distantes écrasent les locales,
     *  sans comparer les dates de modification. */
    private boolean ecraser = false;

    public SyncService(SyncConfig config) {
        if (!config.estConfigure()) {
            throw new IllegalStateException("La synchronisation Supabase n'est pas configurée (URL et clé nécessaires).");
        }
        this.client = new SupabaseClient(config.getUrl(), config.getCleAnon());
    }

    /** Vérifie que l'URL et la clé permettent bien d'accéder à la base distante. */
    public void testerConnexion() throws Exception {
        client.recuperer("pilotes", "id");
    }

    /** Lance une synchronisation complète : d'abord les nouveautés reçues, puis l'envoi des nôtres. */
    public RapportSync synchroniser() throws Exception {
        RapportSync rapport = new RapportSync();
        Connection conn = DatabaseManager.getConnection();
        boolean autoCommitInitial = conn.getAutoCommit();
        conn.setAutoCommit(false);
        try {
            tirerUtilisateurs(conn, rapport);
            tirerPilotes(conn, rapport);
            tirerEpreuves(conn, rapport);
            tirerInscriptions(conn, rapport);
            tirerResultats(conn, rapport);

            pousserUtilisateurs(conn, rapport);
            pousserPilotes(conn, rapport);
            pousserEpreuves(conn, rapport);
            pousserInscriptions(conn, rapport);
            pousserResultats(conn, rapport);

            conn.commit();
        } catch (Exception e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(autoCommitInitial);
        }
        return rapport;
    }

    // ============================================================ RECEVOIR / ENVOYER (mode "miroir")

    /**
     * Base en ligne -> application (équivalent d'un "git pull").
     * Après l'opération, pilotes, épreuves, inscriptions et résultats de l'application sont
     * IDENTIQUES à ceux de la base en ligne : ce qui n'existe pas en ligne est supprimé en local.
     * Les comptes utilisateurs sont seulement fusionnés (jamais supprimés), pour ne pas risquer
     * de se retrouver sans compte administrateur.
     */
    public RapportSync recevoirDepuisSupabase() throws Exception {
        RapportSync rapport = new RapportSync();
        // Lecture préalable : sert aussi de garde-fou (voir plus bas) et aux suppressions.
        JSONArray distPilotes = client.recuperer("pilotes", "numero_licence");
        JSONArray distEpreuves = client.recuperer("epreuves", "uuid");

        Connection conn = DatabaseManager.getConnection();
        boolean autoCommitInitial = conn.getAutoCommit();
        conn.setAutoCommit(false);
        try {
            // Garde-fou : base en ligne vide + base locale remplie = probablement une erreur
            // (mauvaise URL, tables vidées...). On refuse plutôt que d'effacer tout l'appareil.
            if (distPilotes.isEmpty() && distEpreuves.isEmpty()
                    && (compter(conn, "pilotes") > 0 || compter(conn, "epreuves") > 0)) {
                throw new IllegalStateException(
                        "La base en ligne est vide : réception annulée pour ne pas effacer vos données locales.");
            }

            ecraser = false;
            tirerUtilisateurs(conn, rapport);

            ecraser = true;
            tirerPilotes(conn, rapport);
            tirerEpreuves(conn, rapport);
            tirerInscriptions(conn, rapport);
            tirerResultats(conn, rapport);

            supprimerLocalAbsentsDuDistant(conn, rapport, distPilotes, distEpreuves);

            conn.commit();
        } catch (Exception e) {
            conn.rollback();
            throw e;
        } finally {
            ecraser = false;
            conn.setAutoCommit(autoCommitInitial);
        }
        return rapport;
    }

    /**
     * Application -> base en ligne (équivalent d'un "git push").
     * Après l'opération, la base en ligne est IDENTIQUE à l'application : ce qui n'existe pas
     * en local est supprimé en ligne (donc pour tous les ordinateurs).
     */
    public RapportSync envoyerVersSupabase() throws Exception {
        RapportSync rapport = new RapportSync();
        Connection conn = DatabaseManager.getConnection();
        boolean autoCommitInitial = conn.getAutoCommit();
        conn.setAutoCommit(false);
        try {
            // Garde-fou : base locale vide + base en ligne remplie = on refuse de tout effacer en ligne.
            if (compter(conn, "pilotes") == 0 && compter(conn, "epreuves") == 0
                    && (!client.recuperer("pilotes", "id").isEmpty() || !client.recuperer("epreuves", "id").isEmpty())) {
                throw new IllegalStateException(
                        "La base locale est vide : envoi annulé pour ne pas effacer la base en ligne.");
            }

            pousserUtilisateurs(conn, rapport);
            pousserPilotes(conn, rapport);
            pousserEpreuves(conn, rapport);
            pousserInscriptions(conn, rapport);
            pousserResultats(conn, rapport);

            supprimerDistantAbsentsDuLocal(conn, rapport);

            conn.commit();
        } catch (Exception e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(autoCommitInitial);
        }
        return rapport;
    }

    private void supprimerLocalAbsentsDuDistant(Connection conn, RapportSync rapport,
                                                JSONArray distPilotes, JSONArray distEpreuves) throws Exception {
        Set<String> licences = new HashSet<>();
        for (int i = 0; i < distPilotes.length(); i++) {
            licences.add(cle(distPilotes.getJSONObject(i).optString("numero_licence", null)));
        }
        Set<String> uuids = new HashSet<>();
        for (int i = 0; i < distEpreuves.length(); i++) {
            uuids.add(cle(distEpreuves.getJSONObject(i).optString("uuid", null)));
        }

        // Épreuves et pilotes : leurs inscriptions/résultats/variables/barèmes partent avec (ON DELETE CASCADE).
        rapport.epreuvesSupprimees += supprimerLocalSiAbsent(conn,
                "SELECT id, uuid FROM epreuves", "DELETE FROM epreuves WHERE id = ?", uuids);
        rapport.pilotesSupprimes += supprimerLocalSiAbsent(conn,
                "SELECT id, numero_licence FROM pilotes", "DELETE FROM pilotes WHERE id = ?", licences);

        Set<String> inscriptions = clesDistantes("inscriptions_epreuve");
        Set<String> resultats = clesDistantes("resultats");
        rapport.inscriptionsSupprimees += supprimerLocalSiAbsent(conn,
                "SELECT i.id, p.numero_licence, e.uuid FROM inscriptions_epreuve i " +
                        "JOIN pilotes p ON p.id = i.pilote_id JOIN epreuves e ON e.id = i.epreuve_id",
                "DELETE FROM inscriptions_epreuve WHERE id = ?", inscriptions);
        rapport.resultatsSupprimes += supprimerLocalSiAbsent(conn,
                "SELECT r.id, p.numero_licence, e.uuid FROM resultats r " +
                        "JOIN pilotes p ON p.id = r.pilote_id JOIN epreuves e ON e.id = r.epreuve_id",
                "DELETE FROM resultats WHERE id = ?", resultats);
    }

    /** Parcourt le SELECT (1re colonne = id, les suivantes = clé métier) et supprime les lignes dont la clé est absente. */
    private int supprimerLocalSiAbsent(Connection conn, String select, String delete, Set<String> clesDistantes) throws SQLException {
        List<Integer> aSupprimer = new ArrayList<>();
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(select)) {
            int nbCols = rs.getMetaData().getColumnCount();
            while (rs.next()) {
                StringBuilder k = new StringBuilder();
                for (int c = 2; c <= nbCols; c++) {
                    if (c > 2) k.append('|');
                    k.append(cle(rs.getString(c)));
                }
                if (!clesDistantes.contains(k.toString())) aSupprimer.add(rs.getInt(1));
            }
        }
        int n = 0;
        try (PreparedStatement del = conn.prepareStatement(delete)) {
            for (int id : aSupprimer) {
                del.setInt(1, id);
                n += del.executeUpdate();
            }
        }
        return n;
    }

    /** Clés métier "licence|uuid_epreuve" des lignes distantes d'une table de liaison (inscriptions, résultats). */
    private Set<String> clesDistantes(String table) throws Exception {
        Set<String> cles = new HashSet<>();
        JSONArray lignes = client.recuperer(table, "pilote_id,epreuve_id,pilotes(numero_licence),epreuves(uuid)");
        for (int i = 0; i < lignes.length(); i++) {
            JSONObject r = lignes.getJSONObject(i);
            JSONObject p = r.optJSONObject("pilotes");
            JSONObject e = r.optJSONObject("epreuves");
            if (p == null || e == null) continue;
            cles.add(cle(p.optString("numero_licence", null)) + "|" + cle(e.optString("uuid", null)));
        }
        return cles;
    }

    private void supprimerDistantAbsentsDuLocal(Connection conn, RapportSync rapport) throws Exception {
        Set<String> licencesLocales = lireClesLocales(conn, "SELECT numero_licence FROM pilotes");
        Set<String> uuidsLocaux = lireClesLocales(conn, "SELECT uuid FROM epreuves");
        Set<String> inscriptionsLocales = lireClesLocales(conn,
                "SELECT p.numero_licence, e.uuid FROM inscriptions_epreuve i " +
                        "JOIN pilotes p ON p.id = i.pilote_id JOIN epreuves e ON e.id = i.epreuve_id");
        Set<String> resultatsLocaux = lireClesLocales(conn,
                "SELECT p.numero_licence, e.uuid FROM resultats r " +
                        "JOIN pilotes p ON p.id = r.pilote_id JOIN epreuves e ON e.id = r.epreuve_id");

        // Épreuves absentes en local : on efface explicitement leurs enfants côté serveur
        // (on ne suppose pas que Supabase a des ON DELETE CASCADE), puis l'épreuve elle-même.
        JSONArray epreuves = client.recuperer("epreuves", "id,uuid");
        for (int i = 0; i < epreuves.length(); i++) {
            JSONObject r = epreuves.getJSONObject(i);
            if (uuidsLocaux.contains(cle(r.optString("uuid", null)))) continue;
            int id = r.getInt("id");
            for (String enfant : new String[]{"resultats", "inscriptions_epreuve", "epreuve_parametres", "bareme_points"}) {
                client.supprimerOu(enfant, "epreuve_id=eq." + id);
            }
            client.supprimerOu("epreuves", "id=eq." + id);
            rapport.epreuvesSupprimees++;
        }

        JSONArray pilotes = client.recuperer("pilotes", "id,numero_licence");
        for (int i = 0; i < pilotes.length(); i++) {
            JSONObject r = pilotes.getJSONObject(i);
            if (licencesLocales.contains(cle(r.optString("numero_licence", null)))) continue;
            int id = r.getInt("id");
            client.supprimerOu("resultats", "pilote_id=eq." + id);
            client.supprimerOu("inscriptions_epreuve", "pilote_id=eq." + id);
            client.supprimerOu("pilotes", "id=eq." + id);
            rapport.pilotesSupprimes++;
        }

        rapport.inscriptionsSupprimees += supprimerDistantLiaisonsAbsentes("inscriptions_epreuve", inscriptionsLocales);
        rapport.resultatsSupprimes += supprimerDistantLiaisonsAbsentes("resultats", resultatsLocaux);
    }

    private int supprimerDistantLiaisonsAbsentes(String table, Set<String> clesLocales) throws Exception {
        int n = 0;
        JSONArray lignes = client.recuperer(table, "pilote_id,epreuve_id,pilotes(numero_licence),epreuves(uuid)");
        for (int i = 0; i < lignes.length(); i++) {
            JSONObject r = lignes.getJSONObject(i);
            JSONObject p = r.optJSONObject("pilotes");
            JSONObject e = r.optJSONObject("epreuves");
            if (p == null || e == null) continue;
            String k = cle(p.optString("numero_licence", null)) + "|" + cle(e.optString("uuid", null));
            if (clesLocales.contains(k)) continue;
            client.supprimerOu(table, "pilote_id=eq." + r.getInt("pilote_id") + "&epreuve_id=eq." + r.getInt("epreuve_id"));
            n++;
        }
        return n;
    }

    private Set<String> lireClesLocales(Connection conn, String sql) throws SQLException {
        Set<String> cles = new HashSet<>();
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            int nbCols = rs.getMetaData().getColumnCount();
            while (rs.next()) {
                StringBuilder k = new StringBuilder();
                for (int c = 1; c <= nbCols; c++) {
                    if (c > 1) k.append('|');
                    k.append(cle(rs.getString(c)));
                }
                cles.add(k.toString());
            }
        }
        return cles;
    }

    private int compter(Connection conn, String table) throws SQLException {
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + table)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /** Normalise une clé métier (licence ou uuid) pour la comparer sans souci de majuscules/espaces. */
    private String cle(String valeur) {
        return valeur == null ? "" : valeur.trim().toLowerCase();
    }

    // ============================================================ COMPTES UTILISATEURS

    private void tirerUtilisateurs(Connection conn, RapportSync rapport) throws Exception {
        JSONArray distantes = client.recuperer("utilisateurs", "*");
        for (int i = 0; i < distantes.length(); i++) {
            JSONObject r = distantes.getJSONObject(i);
            String identifiant = r.getString("identifiant");
            Instant majDistante = lireInstant(r, "updated_at");

            Integer localId = trouverId(conn, "SELECT id FROM utilisateurs WHERE identifiant = ?", identifiant);
            if (localId != null) {
                Instant majLocale = lireInstantLocal(conn, "utilisateurs", localId);
                if (estPlusRecent(majDistante, majLocale)) {
                    try (PreparedStatement maj = conn.prepareStatement(
                            "UPDATE utilisateurs SET mot_de_passe=?, role=?, actif=?, updated_at=? WHERE id=?")) {
                        maj.setString(1, r.getString("mot_de_passe"));
                        maj.setString(2, r.getString("role"));
                        maj.setBoolean(3, r.getBoolean("actif"));
                        maj.setTimestamp(4, Timestamp.from(majDistante != null ? majDistante : Instant.now()));
                        maj.setInt(5, localId);
                        maj.executeUpdate();
                    }
                    rapport.utilisateursRecus++;
                }
            } else {
                try (PreparedStatement ins = conn.prepareStatement(
                        "INSERT INTO utilisateurs (identifiant, mot_de_passe, role, actif, updated_at) VALUES (?,?,?,?,?)")) {
                    ins.setString(1, identifiant);
                    ins.setString(2, r.getString("mot_de_passe"));
                    ins.setString(3, r.getString("role"));
                    ins.setBoolean(4, r.getBoolean("actif"));
                    ins.setTimestamp(5, Timestamp.from(majDistante != null ? majDistante : Instant.now()));
                    ins.executeUpdate();
                }
                rapport.utilisateursRecus++;
            }
        }
    }

    private void pousserUtilisateurs(Connection conn, RapportSync rapport) throws Exception {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT identifiant, mot_de_passe, role, actif, updated_at FROM utilisateurs")) {
            while (rs.next()) {
                JSONObject payload = new JSONObject();
                payload.put("identifiant", rs.getString("identifiant"));
                payload.put("mot_de_passe", rs.getString("mot_de_passe"));
                payload.put("role", rs.getString("role"));
                payload.put("actif", rs.getBoolean("actif"));
                payload.put("updated_at", versIso(rs.getTimestamp("updated_at")));

                // Fusion par identifiant : évite de créer un doublon si le même identifiant
                // (ex: "admin") existe déjà côté serveur ou sur un autre appareil.
                client.envoyer("utilisateurs", payload, "identifiant");
                rapport.utilisateursEnvoyes++;
            }
        }
    }

    // ============================================================ PILOTES

    private void tirerPilotes(Connection conn, RapportSync rapport) throws Exception {
        JSONArray distantes = client.recuperer("pilotes", "*");
        for (int i = 0; i < distantes.length(); i++) {
            JSONObject r = distantes.getJSONObject(i);
            String licence = r.getString("numero_licence");
            Instant majDistante = lireInstant(r, "updated_at");

            Integer localId = trouverId(conn, "SELECT id FROM pilotes WHERE numero_licence = ?", licence);
            if (localId != null) {
                Instant majLocale = lireInstantLocal(conn, "pilotes", localId);
                if (estPlusRecent(majDistante, majLocale)) {
                    try (PreparedStatement maj = conn.prepareStatement(
                            "UPDATE pilotes SET uuid=?, nom=?, prenom=?, caserne=?, poids=?, email=?, " +
                                    "annee_naissance=?, genre=?, updated_at=? WHERE id=?")) {
                        maj.setString(1, r.getString("uuid"));
                        maj.setString(2, r.getString("nom"));
                        maj.setString(3, r.getString("prenom"));
                        maj.setString(4, r.optString("caserne", null));
                        setNullableDouble(maj, 5, r, "poids");
                        maj.setString(6, r.optString("email", null));
                        setNullableInt(maj, 7, r, "annee_naissance");
                        maj.setString(8, r.optString("genre", null));
                        maj.setTimestamp(9, Timestamp.from(majDistante != null ? majDistante : Instant.now()));
                        maj.setInt(10, localId);
                        maj.executeUpdate();
                    }
                    rapport.pilotesRecus++;
                } else {
                    try (PreparedStatement maj = conn.prepareStatement("UPDATE pilotes SET uuid=? WHERE id=? AND uuid IS NULL")) {
                        maj.setString(1, r.getString("uuid"));
                        maj.setInt(2, localId);
                        maj.executeUpdate();
                    }
                }
            } else {
                try (PreparedStatement ins = conn.prepareStatement(
                        "INSERT INTO pilotes (uuid, numero_licence, nom, prenom, caserne, poids, email, annee_naissance, genre, updated_at) " +
                                "VALUES (?,?,?,?,?,?,?,?,?,?)")) {
                    ins.setString(1, r.getString("uuid"));
                    ins.setString(2, licence);
                    ins.setString(3, r.getString("nom"));
                    ins.setString(4, r.getString("prenom"));
                    ins.setString(5, r.optString("caserne", null));
                    setNullableDouble(ins, 6, r, "poids");
                    ins.setString(7, r.optString("email", null));
                    setNullableInt(ins, 8, r, "annee_naissance");
                    ins.setString(9, r.optString("genre", null));
                    ins.setTimestamp(10, Timestamp.from(majDistante != null ? majDistante : Instant.now()));
                    ins.executeUpdate();
                }
                rapport.pilotesRecus++;
            }
        }
    }

    private void pousserPilotes(Connection conn, RapportSync rapport) throws Exception {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT id, uuid, numero_licence, nom, prenom, caserne, poids, email, annee_naissance, genre, updated_at FROM pilotes")) {
            while (rs.next()) {
                JSONObject payload = new JSONObject();
                payload.put("uuid", rs.getString("uuid"));
                payload.put("numero_licence", rs.getString("numero_licence"));
                payload.put("nom", rs.getString("nom"));
                payload.put("prenom", rs.getString("prenom"));
                mettreOuNull(payload, "caserne", rs.getString("caserne"));
                mettreDoubleOuNull(payload, "poids", rs, "poids");
                mettreOuNull(payload, "email", rs.getString("email"));
                mettreIntOuNull(payload, "annee_naissance", rs, "annee_naissance");
                mettreOuNull(payload, "genre", rs.getString("genre"));
                payload.put("updated_at", versIso(rs.getTimestamp("updated_at")));

                // Fusion par numéro de licence : évite de créer un doublon si deux appareils
                // ont inscrit le même pilote indépendamment avant leur première synchronisation.
                JSONObject retour = client.envoyer("pilotes", payload, "numero_licence");
                try (PreparedStatement maj = conn.prepareStatement("UPDATE pilotes SET uuid=?, supabase_id=? WHERE id=?")) {
                    maj.setString(1, retour.getString("uuid"));
                    maj.setInt(2, retour.getInt("id"));
                    maj.setInt(3, rs.getInt("id"));
                    maj.executeUpdate();
                }
                rapport.pilotesEnvoyes++;
            }
        }
    }

    // ============================================================ ÉPREUVES (+ variables + barème)

    private void tirerEpreuves(Connection conn, RapportSync rapport) throws Exception {
        JSONArray distantes = client.recuperer("epreuves", "*,epreuve_parametres(*),bareme_points(*)");
        for (int i = 0; i < distantes.length(); i++) {
            JSONObject r = distantes.getJSONObject(i);
            String uuid = r.getString("uuid");
            Instant majDistante = lireInstant(r, "updated_at");

            Integer localId = trouverId(conn, "SELECT id FROM epreuves WHERE uuid = ?", uuid);
            boolean doitRemplacerEnfants = false;

            if (localId != null) {
                Instant majLocale = lireInstantLocal(conn, "epreuves", localId);
                if (estPlusRecent(majDistante, majLocale)) {
                    try (PreparedStatement maj = conn.prepareStatement(
                            "UPDATE epreuves SET nom=?, description=?, mode_calcul=?, formule=?, valeur_cle=?, sens_classement=?, " +
                                    "afficher_classement=?, compte_dans_general=?, actif=?, ordre=?, updated_at=? WHERE id=?")) {
                        maj.setString(1, r.getString("nom"));
                        maj.setString(2, r.optString("description", null));
                        maj.setString(3, r.getString("mode_calcul"));
                        maj.setString(4, r.optString("formule", null));
                        maj.setString(5, r.optString("valeur_cle", null));
                        maj.setString(6, r.getString("sens_classement"));
                        maj.setBoolean(7, r.getBoolean("afficher_classement"));
                        maj.setBoolean(8, r.getBoolean("compte_dans_general"));
                        maj.setBoolean(9, r.getBoolean("actif"));
                        maj.setInt(10, r.optInt("ordre", 0));
                        maj.setTimestamp(11, Timestamp.from(majDistante != null ? majDistante : Instant.now()));
                        maj.setInt(12, localId);
                        maj.executeUpdate();
                    }
                    doitRemplacerEnfants = true;
                    rapport.epreuvesRecues++;
                }
            } else {
                try (PreparedStatement ins = conn.prepareStatement(
                        "INSERT INTO epreuves (uuid, nom, description, mode_calcul, formule, valeur_cle, sens_classement, " +
                                "afficher_classement, compte_dans_general, actif, ordre, updated_at) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    ins.setString(1, uuid);
                    ins.setString(2, r.getString("nom"));
                    ins.setString(3, r.optString("description", null));
                    ins.setString(4, r.getString("mode_calcul"));
                    ins.setString(5, r.optString("formule", null));
                    ins.setString(6, r.optString("valeur_cle", null));
                    ins.setString(7, r.getString("sens_classement"));
                    ins.setBoolean(8, r.getBoolean("afficher_classement"));
                    ins.setBoolean(9, r.getBoolean("compte_dans_general"));
                    ins.setBoolean(10, r.getBoolean("actif"));
                    ins.setInt(11, r.optInt("ordre", 0));
                    ins.setTimestamp(12, Timestamp.from(majDistante != null ? majDistante : Instant.now()));
                    ins.executeUpdate();
                    try (ResultSet keys = ins.getGeneratedKeys()) {
                        if (keys.next()) localId = keys.getInt(1);
                    }
                }
                doitRemplacerEnfants = true;
                rapport.epreuvesRecues++;
            }

            if (doitRemplacerEnfants && localId != null) {
                try (PreparedStatement del1 = conn.prepareStatement("DELETE FROM epreuve_parametres WHERE epreuve_id = ?")) {
                    del1.setInt(1, localId);
                    del1.executeUpdate();
                }
                try (PreparedStatement del2 = conn.prepareStatement("DELETE FROM bareme_points WHERE epreuve_id = ?")) {
                    del2.setInt(1, localId);
                    del2.executeUpdate();
                }
                JSONArray parametres = r.optJSONArray("epreuve_parametres");
                if (parametres != null && parametres.length() > 0) {
                    try (PreparedStatement ins = conn.prepareStatement(
                            "INSERT INTO epreuve_parametres (epreuve_id, nom_variable, label, unite, obligatoire, ordre) VALUES (?,?,?,?,?,?)")) {
                        for (int j = 0; j < parametres.length(); j++) {
                            JSONObject p = parametres.getJSONObject(j);
                            ins.setInt(1, localId);
                            ins.setString(2, p.getString("nom_variable"));
                            ins.setString(3, p.getString("label"));
                            ins.setString(4, p.optString("unite", null));
                            ins.setBoolean(5, p.optBoolean("obligatoire", true));
                            ins.setInt(6, p.optInt("ordre", 0));
                            ins.addBatch();
                        }
                        ins.executeBatch();
                    }
                }
                JSONArray bareme = r.optJSONArray("bareme_points");
                if (bareme != null && bareme.length() > 0) {
                    try (PreparedStatement ins = conn.prepareStatement(
                            "INSERT INTO bareme_points (epreuve_id, rang, points) VALUES (?,?,?)")) {
                        for (int j = 0; j < bareme.length(); j++) {
                            JSONObject b = bareme.getJSONObject(j);
                            ins.setInt(1, localId);
                            ins.setInt(2, b.getInt("rang"));
                            ins.setDouble(3, b.getDouble("points"));
                            ins.addBatch();
                        }
                        ins.executeBatch();
                    }
                }
            }
        }
    }

    private void pousserEpreuves(Connection conn, RapportSync rapport) throws Exception {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT id, uuid, nom, description, mode_calcul, formule, valeur_cle, sens_classement, " +
                             "afficher_classement, compte_dans_general, actif, ordre, updated_at FROM epreuves")) {
            while (rs.next()) {
                int localId = rs.getInt("id");
                JSONObject payload = new JSONObject();
                payload.put("uuid", rs.getString("uuid"));
                payload.put("nom", rs.getString("nom"));
                mettreOuNull(payload, "description", rs.getString("description"));
                payload.put("mode_calcul", rs.getString("mode_calcul"));
                mettreOuNull(payload, "formule", rs.getString("formule"));
                mettreOuNull(payload, "valeur_cle", rs.getString("valeur_cle"));
                payload.put("sens_classement", rs.getString("sens_classement"));
                payload.put("afficher_classement", rs.getBoolean("afficher_classement"));
                payload.put("compte_dans_general", rs.getBoolean("compte_dans_general"));
                payload.put("actif", rs.getBoolean("actif"));
                payload.put("ordre", rs.getInt("ordre"));
                payload.put("updated_at", versIso(rs.getTimestamp("updated_at")));

                JSONObject retour = client.envoyer("epreuves", payload, "uuid");
                int supabaseId = retour.getInt("id");
                try (PreparedStatement maj = conn.prepareStatement("UPDATE epreuves SET supabase_id=? WHERE id=?")) {
                    maj.setInt(1, supabaseId);
                    maj.setInt(2, localId);
                    maj.executeUpdate();
                }
                rapport.epreuvesEnvoyees++;

                // Variables et barème : on remplace tout côté serveur (même principe que la
                // modification d'une épreuve dans l'application : effacer puis réécrire).
                List<JSONObject> parametresPayload = new ArrayList<>();
                try (PreparedStatement sel = conn.prepareStatement(
                        "SELECT nom_variable, label, unite, obligatoire, ordre FROM epreuve_parametres WHERE epreuve_id = ?")) {
                    sel.setInt(1, localId);
                    try (ResultSet rp = sel.executeQuery()) {
                        while (rp.next()) {
                            JSONObject p = new JSONObject();
                            p.put("epreuve_id", supabaseId);
                            p.put("nom_variable", rp.getString("nom_variable"));
                            p.put("label", rp.getString("label"));
                            mettreOuNull(p, "unite", rp.getString("unite"));
                            p.put("obligatoire", rp.getBoolean("obligatoire"));
                            p.put("ordre", rp.getInt("ordre"));
                            parametresPayload.add(p);
                        }
                    }
                }
                client.supprimerOu("epreuve_parametres", "epreuve_id=eq." + supabaseId);
                client.inserer("epreuve_parametres", new JSONArray(parametresPayload));

                List<JSONObject> baremePayload = new ArrayList<>();
                try (PreparedStatement sel = conn.prepareStatement(
                        "SELECT rang, points FROM bareme_points WHERE epreuve_id = ?")) {
                    sel.setInt(1, localId);
                    try (ResultSet rb = sel.executeQuery()) {
                        while (rb.next()) {
                            JSONObject b = new JSONObject();
                            b.put("epreuve_id", supabaseId);
                            b.put("rang", rb.getInt("rang"));
                            b.put("points", rb.getDouble("points"));
                            baremePayload.add(b);
                        }
                    }
                }
                client.supprimerOu("bareme_points", "epreuve_id=eq." + supabaseId);
                client.inserer("bareme_points", new JSONArray(baremePayload));
            }
        }
    }

    // ============================================================ INSCRIPTIONS

    private void tirerInscriptions(Connection conn, RapportSync rapport) throws Exception {
        JSONArray distantes = client.recuperer("inscriptions_epreuve", "*,pilotes(numero_licence),epreuves(uuid)");
        for (int i = 0; i < distantes.length(); i++) {
            JSONObject r = distantes.getJSONObject(i);
            JSONObject piloteEmb = r.optJSONObject("pilotes");
            JSONObject epreuveEmb = r.optJSONObject("epreuves");
            if (piloteEmb == null || epreuveEmb == null) continue;

            Integer piloteId = trouverId(conn, "SELECT id FROM pilotes WHERE numero_licence = ?", piloteEmb.optString("numero_licence", null));
            Integer epreuveId = trouverId(conn, "SELECT id FROM epreuves WHERE uuid = ?", epreuveEmb.optString("uuid", null));
            if (piloteId == null || epreuveId == null) continue; // sera traité à la prochaine synchronisation

            Integer existant = trouverId(conn, "SELECT id FROM inscriptions_epreuve WHERE pilote_id = ? AND epreuve_id = ?", piloteId, epreuveId);
            if (existant == null) {
                try (PreparedStatement ins = conn.prepareStatement("INSERT INTO inscriptions_epreuve (pilote_id, epreuve_id) VALUES (?,?)")) {
                    ins.setInt(1, piloteId);
                    ins.setInt(2, epreuveId);
                    ins.executeUpdate();
                }
                rapport.inscriptionsRecues++;
            }
        }
    }

    private void pousserInscriptions(Connection conn, RapportSync rapport) throws Exception {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT p.supabase_id AS pilote_sid, e.supabase_id AS epreuve_sid " +
                             "FROM inscriptions_epreuve i " +
                             "JOIN pilotes p ON p.id = i.pilote_id " +
                             "JOIN epreuves e ON e.id = i.epreuve_id")) {
            while (rs.next()) {
                int piloteSid = rs.getInt("pilote_sid");
                boolean piloteSidNull = rs.wasNull();
                int epreuveSid = rs.getInt("epreuve_sid");
                boolean epreuveSidNull = rs.wasNull();
                if (piloteSidNull || epreuveSidNull) continue; // pas encore poussés : traité au prochain tour

                JSONObject payload = new JSONObject();
                payload.put("pilote_id", piloteSid);
                payload.put("epreuve_id", epreuveSid);
                client.envoyer("inscriptions_epreuve", payload, "pilote_id,epreuve_id");
                rapport.inscriptionsEnvoyees++;
            }
        }
    }

    // ============================================================ RÉSULTATS

    private void tirerResultats(Connection conn, RapportSync rapport) throws Exception {
        JSONArray distantes = client.recuperer("resultats", "*,pilotes(numero_licence),epreuves(uuid)");
        for (int i = 0; i < distantes.length(); i++) {
            JSONObject r = distantes.getJSONObject(i);
            JSONObject piloteEmb = r.optJSONObject("pilotes");
            JSONObject epreuveEmb = r.optJSONObject("epreuves");
            if (piloteEmb == null || epreuveEmb == null) continue;

            Integer piloteId = trouverId(conn, "SELECT id FROM pilotes WHERE numero_licence = ?", piloteEmb.optString("numero_licence", null));
            Integer epreuveId = trouverId(conn, "SELECT id FROM epreuves WHERE uuid = ?", epreuveEmb.optString("uuid", null));
            if (piloteId == null || epreuveId == null) continue;

            Instant majDistante = lireInstant(r, "updated_at");
            String valeursJson = r.optString("valeurs_json", "{}");
            double points = r.optDouble("points", 0);
            boolean disqualifie = r.optBoolean("disqualifie", false);
            String saisiPar = r.optString("saisi_par", null);

            Integer existant = trouverId(conn, "SELECT id FROM resultats WHERE epreuve_id = ? AND pilote_id = ?", epreuveId, piloteId);
            if (existant != null) {
                Instant majLocale = lireInstantLocal(conn, "resultats", existant);
                if (estPlusRecent(majDistante, majLocale)) {
                    try (PreparedStatement maj = conn.prepareStatement(
                            "UPDATE resultats SET valeurs_json=?, points=?, disqualifie=?, saisi_par=?, updated_at=? WHERE id=?")) {
                        maj.setString(1, valeursJson);
                        maj.setDouble(2, points);
                        maj.setBoolean(3, disqualifie);
                        maj.setString(4, saisiPar);
                        maj.setTimestamp(5, Timestamp.from(majDistante != null ? majDistante : Instant.now()));
                        maj.setInt(6, existant);
                        maj.executeUpdate();
                    }
                    rapport.resultatsRecus++;
                }
            } else {
                try (PreparedStatement ins = conn.prepareStatement(
                        "INSERT INTO resultats (epreuve_id, pilote_id, valeurs_json, points, disqualifie, saisi_par, updated_at) VALUES (?,?,?,?,?,?,?)")) {
                    ins.setInt(1, epreuveId);
                    ins.setInt(2, piloteId);
                    ins.setString(3, valeursJson);
                    ins.setDouble(4, points);
                    ins.setBoolean(5, disqualifie);
                    ins.setString(6, saisiPar);
                    ins.setTimestamp(7, Timestamp.from(majDistante != null ? majDistante : Instant.now()));
                    ins.executeUpdate();
                }
                rapport.resultatsRecus++;
            }
        }
    }

    private void pousserResultats(Connection conn, RapportSync rapport) throws Exception {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT r.valeurs_json, r.points, r.disqualifie, r.saisi_par, r.updated_at, " +
                             "p.supabase_id AS pilote_sid, e.supabase_id AS epreuve_sid " +
                             "FROM resultats r " +
                             "JOIN pilotes p ON p.id = r.pilote_id " +
                             "JOIN epreuves e ON e.id = r.epreuve_id")) {
            while (rs.next()) {
                int piloteSid = rs.getInt("pilote_sid");
                boolean piloteSidNull = rs.wasNull();
                int epreuveSid = rs.getInt("epreuve_sid");
                boolean epreuveSidNull = rs.wasNull();
                if (piloteSidNull || epreuveSidNull) continue;

                JSONObject payload = new JSONObject();
                payload.put("pilote_id", piloteSid);
                payload.put("epreuve_id", epreuveSid);
                payload.put("valeurs_json", rs.getString("valeurs_json"));
                payload.put("points", rs.getDouble("points"));
                payload.put("disqualifie", rs.getBoolean("disqualifie"));
                mettreOuNull(payload, "saisi_par", rs.getString("saisi_par"));
                payload.put("updated_at", versIso(rs.getTimestamp("updated_at")));

                client.envoyer("resultats", payload, "epreuve_id,pilote_id");
                rapport.resultatsEnvoyes++;
            }
        }
    }

    // ============================================================ Utilitaires internes

    private Integer trouverId(Connection conn, String sql, Object... params) throws SQLException {
        if (params.length > 0 && params[0] == null) return null; // évite de chercher sur une valeur inconnue
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : null;
            }
        }
    }

    private Instant lireInstantLocal(Connection conn, String table, int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT updated_at FROM " + table + " WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Timestamp ts = rs.getTimestamp(1);
                    return ts != null ? ts.toInstant() : null;
                }
            }
        }
        return null;
    }

    private Instant lireInstant(JSONObject o, String champ) {
        String s = o.optString(champ, null);
        if (s == null || s.isBlank()) return null;
        try {
            return OffsetDateTime.parse(s).toInstant();
        } catch (Exception e) {
            try {
                return Instant.parse(s);
            } catch (Exception e2) {
                return null;
            }
        }
    }

    private boolean estPlusRecent(Instant distante, Instant locale) {
        if (ecraser) return true;
        if (distante == null) return false;
        return locale == null || distante.isAfter(locale);
    }

    private String versIso(Timestamp ts) {
        return (ts != null ? ts.toInstant() : Instant.now()).toString();
    }

    private void setNullableDouble(PreparedStatement ps, int index, JSONObject o, String champ) throws SQLException {
        if (!o.has(champ) || o.isNull(champ)) ps.setNull(index, Types.DOUBLE);
        else ps.setDouble(index, o.getDouble(champ));
    }

    private void setNullableInt(PreparedStatement ps, int index, JSONObject o, String champ) throws SQLException {
        if (!o.has(champ) || o.isNull(champ)) ps.setNull(index, Types.INTEGER);
        else ps.setInt(index, o.getInt(champ));
    }

    private void mettreOuNull(JSONObject o, String champ, String valeur) {
        o.put(champ, valeur == null ? JSONObject.NULL : valeur);
    }

    private void mettreDoubleOuNull(JSONObject o, String champ, ResultSet rs, String colonne) throws SQLException {
        double v = rs.getDouble(colonne);
        o.put(champ, rs.wasNull() ? JSONObject.NULL : v);
    }

    private void mettreIntOuNull(JSONObject o, String champ, ResultSet rs, String colonne) throws SQLException {
        int v = rs.getInt(colonne);
        o.put(champ, rs.wasNull() ? JSONObject.NULL : v);
    }
}
