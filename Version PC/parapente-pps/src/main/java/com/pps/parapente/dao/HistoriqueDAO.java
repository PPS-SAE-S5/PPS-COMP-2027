package com.pps.parapente.dao;

import com.pps.parapente.model.Categorie;
import com.pps.parapente.model.HistoriquePoint;
import com.pps.parapente.model.Resultat;
import com.pps.parapente.model.TypeActionPoints;
import com.pps.parapente.model.Utilisateur;
import com.pps.parapente.util.SessionManager;
import org.json.JSONObject;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Accès à l'historique des modifications de points.
 * Journal en LECTURE SEULE : cette classe ne propose volontairement que l'ajout et la lecture,
 * aucune méthode de modification ni de suppression.
 */
public class HistoriqueDAO {

    /**
     * Ajoute une ligne d'historique pour le passage de "avant" à "apres", si quelque chose a changé
     * (nouveau résultat, points différents ou statut de disqualification différent).
     *
     * @param avant état précédent du résultat (null = première saisie pour ce pilote sur cette épreuve)
     * @param apres nouvel état du résultat (son type d'action et son motif éventuels sont lus dessus)
     */
    public void journaliser(Resultat avant, Resultat apres) throws SQLException {
        boolean change = avant == null
                || Double.compare(avant.getPoints(), apres.getPoints()) != 0
                || avant.isDisqualifie() != apres.isDisqualifie();
        if (!change) return;

        TypeActionPoints type = apres.getTypeActionHistorique();
        if (type == null) {
            if (avant == null) type = TypeActionPoints.SAISIE;
            else if (!avant.isDisqualifie() && apres.isDisqualifie()) type = TypeActionPoints.DISQUALIFICATION;
            else type = TypeActionPoints.MODIFICATION;
        }
        if (apres.isDisqualifie() && (avant == null || !avant.isDisqualifie()) && type != TypeActionPoints.RECALCUL_BAREME) {
            type = TypeActionPoints.DISQUALIFICATION;
        }

        ajouter(apres.getEpreuveId(), apres.getPiloteId(), type,
                avant != null ? avant.getPoints() : null, apres.getPoints(),
                apres.getMotifHistorique(), new JSONObject(apres.getValeurs()).toString(), apres.getSaisiPar());
    }

    /** Trace la suppression d'un résultat. */
    public void journaliserSuppression(Resultat supprime) throws SQLException {
        ajouter(supprime.getEpreuveId(), supprime.getPiloteId(), TypeActionPoints.SUPPRESSION,
                supprime.getPoints(), 0.0, supprime.getMotifHistorique(), null, supprime.getSaisiPar());
    }

    private void ajouter(int epreuveId, int piloteId, TypeActionPoints type, Double pointsAvant, Double pointsApres,
                         String motif, String valeursJson, String saisiParParDefaut) throws SQLException {
        Connection conn = DatabaseManager.getConnection();

        // Recopie des informations lisibles (pilote, épreuve, catégorie) au moment de l'action
        String piloteNom = null, licence = null, categorie = null, epreuveNom = null, epreuveUuid = null;
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT prenom, nom, numero_licence, categorie, genre FROM pilotes WHERE id = ?")) {
            ps.setInt(1, piloteId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    piloteNom = ((rs.getString("prenom") != null ? rs.getString("prenom") : "") + " "
                            + (rs.getString("nom") != null ? rs.getString("nom") : "")).trim();
                    licence = rs.getString("numero_licence");
                    categorie = construireCategorie(rs.getString("categorie"), rs.getString("genre"));
                }
            }
        }
        try (PreparedStatement ps = conn.prepareStatement("SELECT nom, uuid FROM epreuves WHERE id = ?")) {
            ps.setInt(1, epreuveId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    epreuveNom = rs.getString("nom");
                    epreuveUuid = rs.getString("uuid");
                }
            }
        }

        Utilisateur connecte = SessionManager.getUtilisateurConnecte();
        String auteur = connecte != null ? connecte.getIdentifiant() : saisiParParDefaut;
        String role = connecte != null && connecte.getRole() != null ? connecte.getRole().name() : null;

        String sql = "INSERT INTO historique_points (auteur, auteur_role, pilote_licence, pilote_nom, epreuve_uuid, " +
                "epreuve_nom, categorie, type_action, points_avant, points_apres, motif, valeurs_json) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, auteur);
            ps.setString(2, role);
            ps.setString(3, licence);
            ps.setString(4, piloteNom);
            ps.setString(5, epreuveUuid);
            ps.setString(6, epreuveNom);
            ps.setString(7, categorie);
            ps.setString(8, type.name());
            if (pointsAvant != null) ps.setDouble(9, pointsAvant); else ps.setNull(9, Types.DOUBLE);
            if (pointsApres != null) ps.setDouble(10, pointsApres); else ps.setNull(10, Types.DOUBLE);
            String motifNettoye = motif != null && !motif.isBlank() ? motif.trim() : null;
            if (motifNettoye != null && motifNettoye.length() > 500) motifNettoye = motifNettoye.substring(0, 500);
            ps.setString(11, motifNettoye);
            ps.setString(12, valeursJson);
            ps.executeUpdate();
        }
    }

    /** "POMPIER" + "Homme" -> "Pompier-H" ; sans genre renseigné -> "Pompier". */
    static String construireCategorie(String codeCategorie, String genre) {
        String libelle = Categorie.depuisCode(codeCategorie).getLibelle();
        if (genre != null && !genre.isBlank()) {
            libelle += "-" + genre.trim().substring(0, 1).toUpperCase();
        }
        return libelle;
    }

    /** Tout l'historique, du plus récent au plus ancien. */
    public List<HistoriquePoint> listerTous() throws SQLException {
        List<HistoriquePoint> liste = new ArrayList<>();
        String sql = "SELECT * FROM historique_points ORDER BY date_action DESC, id DESC";
        try (Statement st = DatabaseManager.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) liste.add(mapper(rs));
        }
        return liste;
    }

    private HistoriquePoint mapper(ResultSet rs) throws SQLException {
        HistoriquePoint h = new HistoriquePoint();
        h.setId(rs.getInt("id"));
        h.setUuid(rs.getString("uuid"));
        Timestamp ts = rs.getTimestamp("date_action");
        if (ts != null) h.setDateAction(ts.toLocalDateTime());
        h.setAuteur(rs.getString("auteur"));
        h.setAuteurRole(rs.getString("auteur_role"));
        h.setPiloteLicence(rs.getString("pilote_licence"));
        h.setPiloteNom(rs.getString("pilote_nom"));
        h.setEpreuveUuid(rs.getString("epreuve_uuid"));
        h.setEpreuveNom(rs.getString("epreuve_nom"));
        h.setCategorie(rs.getString("categorie"));
        h.setTypeAction(TypeActionPoints.depuisCode(rs.getString("type_action")));
        double avant = rs.getDouble("points_avant");
        h.setPointsAvant(rs.wasNull() ? null : avant);
        double apres = rs.getDouble("points_apres");
        h.setPointsApres(rs.wasNull() ? null : apres);
        h.setMotif(rs.getString("motif"));
        h.setValeursJson(rs.getString("valeurs_json"));
        return h;
    }
}
