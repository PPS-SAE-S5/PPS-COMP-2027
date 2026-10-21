package com.pps.parapente.dao;

import com.pps.parapente.model.Atelier;
import com.pps.parapente.model.Creneau;
import com.pps.parapente.model.Epreuve;
import com.pps.parapente.model.Journee;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** DAO du planning : journées, ateliers et affectation des épreuves aux ateliers. */
public class PlanningDAO {

    //   JOURNÉES

    public List<Journee> listerJournees() throws SQLException {
        List<Journee> liste = new ArrayList<>();
        try (Statement st = DatabaseManager.getConnection().createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM journees ORDER BY ordre, id")) {
            while (rs.next()) {
                Journee j = new Journee();
                j.setId(rs.getInt("id"));
                j.setUuid(rs.getString("uuid"));
                j.setNom(rs.getString("nom"));
                Date d = rs.getDate("date_journee");
                j.setDateJournee(d == null ? null : d.toLocalDate());
                j.setOrdre(rs.getInt("ordre"));
                liste.add(j);
            }
        }
        return liste;
    }

    public void creerJournee(Journee j) throws SQLException {
        String sql = "INSERT INTO journees (nom, date_journee, ordre) VALUES (?,?,?)";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, j.getNom());
            ps.setDate(2, j.getDateJournee() == null ? null : Date.valueOf(j.getDateJournee()));
            ps.setInt(3, prochainOrdre("journees", null, 0));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) j.setId(keys.getInt(1));
            }
        }
    }

    public void mettreAJourJournee(Journee j) throws SQLException {
        String sql = "UPDATE journees SET nom=?, date_journee=?, updated_at=CURRENT_TIMESTAMP WHERE id=?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, j.getNom());
            ps.setDate(2, j.getDateJournee() == null ? null : Date.valueOf(j.getDateJournee()));
            ps.setInt(3, j.getId());
            ps.executeUpdate();
        }
    }

    /** Supprime la journée ET ses ateliers (cascade). Les épreuves, elles, ne sont pas supprimées : elles redeviennent « à placer ». */
    public void supprimerJournee(int id) throws SQLException {
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement("DELETE FROM journees WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    //   ATELIERS

    /** Ateliers d'une journée, chacun avec ses épreuves dans l'ordre de passage. */
    public List<Atelier> listerAteliers(int journeeId) throws SQLException {
        List<Atelier> liste = new ArrayList<>();
        String sql = "SELECT * FROM ateliers WHERE journee_id = ? ORDER BY heure_debut, ordre, id";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, journeeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    liste.add(mapperAtelier(rs));
                }
            }
        }
        for (Atelier a : liste) {
            a.setEpreuves(listerEpreuvesDeAtelier(a.getId()));
        }
        return liste;
    }

    public int compterAteliers(int journeeId) throws SQLException {
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement("SELECT COUNT(*) FROM ateliers WHERE journee_id = ?")) {
            ps.setInt(1, journeeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /** Crée l'atelier et affecte ses épreuves (dans l'ordre de la liste) en une seule transaction. */
    public void creerAtelier(Atelier a) throws SQLException {
        Connection conn = DatabaseManager.getConnection();
        boolean ancienAutoCommit = conn.getAutoCommit();
        try {
            conn.setAutoCommit(false);
            String sql = "INSERT INTO ateliers (journee_id, nom, creneau, heure_debut, heure_fin, lieu, responsable, ordre) VALUES (?,?,?,?,?,?,?,?)";
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, a.getJourneeId());
                ps.setString(2, a.getNom());
                ps.setString(3, a.getCreneau().name());
                ps.setString(4, vide(a.getHeureDebut()));
                ps.setString(5, vide(a.getHeureFin()));
                ps.setString(6, vide(a.getLieu()));
                ps.setString(7, vide(a.getResponsable()));
                ps.setInt(8, prochainOrdre("ateliers", "journee_id", a.getJourneeId()));
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) a.setId(keys.getInt(1));
                }
            }
            affecterEpreuves(conn, a);
            conn.commit();
        } catch (SQLException ex) {
            conn.rollback();
            throw ex;
        } finally {
            conn.setAutoCommit(ancienAutoCommit);
        }
    }

    public void mettreAJourAtelier(Atelier a) throws SQLException {
        Connection conn = DatabaseManager.getConnection();
        boolean ancienAutoCommit = conn.getAutoCommit();
        try {
            conn.setAutoCommit(false);
            String sql = "UPDATE ateliers SET journee_id=?, nom=?, creneau=?, heure_debut=?, heure_fin=?, lieu=?, responsable=?, " +
                    "updated_at=CURRENT_TIMESTAMP WHERE id=?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, a.getJourneeId());
                ps.setString(2, a.getNom());
                ps.setString(3, a.getCreneau().name());
                ps.setString(4, vide(a.getHeureDebut()));
                ps.setString(5, vide(a.getHeureFin()));
                ps.setString(6, vide(a.getLieu()));
                ps.setString(7, vide(a.getResponsable()));
                ps.setInt(8, a.getId());
                ps.executeUpdate();
            }
            try (PreparedStatement del = conn.prepareStatement("DELETE FROM atelier_epreuves WHERE atelier_id = ?")) {
                del.setInt(1, a.getId());
                del.executeUpdate();
            }
            affecterEpreuves(conn, a);
            conn.commit();
        } catch (SQLException ex) {
            conn.rollback();
            throw ex;
        } finally {
            conn.setAutoCommit(ancienAutoCommit);
        }
    }

    /** Supprime l'atelier : ses épreuves ne sont pas supprimées, elles redeviennent « à placer ». */
    public void supprimerAtelier(int id) throws SQLException {
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement("DELETE FROM ateliers WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    //   ÉPREUVES <-> ATELIERS

    /** Épreuves actives qui ne sont encore dans aucun atelier. */
    public List<Epreuve> listerEpreuvesNonAffectees() throws SQLException {
        return lireEpreuves("SELECT id, nom FROM epreuves WHERE actif = TRUE " +
                "AND id NOT IN (SELECT epreuve_id FROM atelier_epreuves) ORDER BY ordre, id", null);
    }

    /** Épreuves qu'on peut ajouter à cet atelier : celles non affectées + celles déjà dans cet atelier. */
    public List<Epreuve> listerEpreuvesDisponibles(int atelierId) throws SQLException {
        return lireEpreuves("SELECT id, nom FROM epreuves WHERE actif = TRUE " +
                "AND (id NOT IN (SELECT epreuve_id FROM atelier_epreuves) " +
                "OR id IN (SELECT epreuve_id FROM atelier_epreuves WHERE atelier_id = ?)) ORDER BY ordre, id", atelierId);
    }

    public int compterEpreuvesActives() throws SQLException {
        try (Statement st = DatabaseManager.getConnection().createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM epreuves WHERE actif = TRUE")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    //   internes

    private List<Epreuve> listerEpreuvesDeAtelier(int atelierId) throws SQLException {
        return lireEpreuves("SELECT e.id, e.nom FROM atelier_epreuves ae JOIN epreuves e ON e.id = ae.epreuve_id " +
                "WHERE ae.atelier_id = ? ORDER BY ae.ordre, ae.id", atelierId);
    }

    private List<Epreuve> lireEpreuves(String sql, Integer parametre) throws SQLException {
        List<Epreuve> liste = new ArrayList<>();
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            if (parametre != null) ps.setInt(1, parametre);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Epreuve e = new Epreuve();
                    e.setId(rs.getInt("id"));
                    e.setNom(rs.getString("nom"));
                    liste.add(e);
                }
            }
        }
        return liste;
    }

    /** Affecte les épreuves à l'atelier ; une épreuve qui était dans un autre atelier en est retirée (une épreuve = un seul atelier). */
    private void affecterEpreuves(Connection conn, Atelier a) throws SQLException {
        int rang = 0;
        try (PreparedStatement retirer = conn.prepareStatement("DELETE FROM atelier_epreuves WHERE epreuve_id = ?");
             PreparedStatement ajouter = conn.prepareStatement("INSERT INTO atelier_epreuves (atelier_id, epreuve_id, ordre) VALUES (?,?,?)")) {
            for (Epreuve e : a.getEpreuves()) {
                retirer.setInt(1, e.getId());
                retirer.executeUpdate();
                ajouter.setInt(1, a.getId());
                ajouter.setInt(2, e.getId());
                ajouter.setInt(3, rang++);
                ajouter.executeUpdate();
            }
        }
    }

    private int prochainOrdre(String table, String colonneFiltre, int valeurFiltre) throws SQLException {
        String sql = "SELECT COALESCE(MAX(ordre), -1) + 1 FROM " + table + (colonneFiltre == null ? "" : " WHERE " + colonneFiltre + " = ?");
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            if (colonneFiltre != null) ps.setInt(1, valeurFiltre);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private Atelier mapperAtelier(ResultSet rs) throws SQLException {
        Atelier a = new Atelier();
        a.setId(rs.getInt("id"));
        a.setUuid(rs.getString("uuid"));
        a.setJourneeId(rs.getInt("journee_id"));
        a.setNom(rs.getString("nom"));
        a.setCreneau(Creneau.valueOf(rs.getString("creneau")));
        a.setHeureDebut(rs.getString("heure_debut"));
        a.setHeureFin(rs.getString("heure_fin"));
        a.setLieu(rs.getString("lieu"));
        a.setResponsable(rs.getString("responsable"));
        a.setOrdre(rs.getInt("ordre"));
        return a;
    }

    private String vide(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
