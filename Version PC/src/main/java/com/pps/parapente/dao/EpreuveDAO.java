package com.pps.parapente.dao;

import com.pps.parapente.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO pour les épreuves. Une épreuve est composée de :
 *  - ses infos générales (nom, mode de calcul, formule ou barème...)
 *  - la liste de ses paramètres/variables saisissables (créés dynamiquement par le responsable)
 *  - éventuellement une table de barème (rang -> points)
 */
public class EpreuveDAO {

    public List<Epreuve> listerToutes() throws SQLException {
        List<Epreuve> liste = new ArrayList<>();
        String sql = "SELECT * FROM epreuves ORDER BY ordre, id";
        try (Statement st = DatabaseManager.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Epreuve e = mapper(rs);
                chargerParametres(e);
                chargerBareme(e);
                liste.add(e);
            }
        }
        return liste;
    }

    public Optional<Epreuve> trouverParId(int id) throws SQLException {
        String sql = "SELECT * FROM epreuves WHERE id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Epreuve e = mapper(rs);
                    chargerParametres(e);
                    chargerBareme(e);
                    return Optional.of(e);
                }
            }
        }
        return Optional.empty();
    }

    public void creer(Epreuve e) throws SQLException {
        Connection conn = DatabaseManager.getConnection();
        boolean ancienAutoCommit = conn.getAutoCommit();
        try {
            conn.setAutoCommit(false);
            String sql = "INSERT INTO epreuves (nom, description, mode_calcul, formule, valeur_cle, sens_classement, " +
                    "afficher_classement, compte_dans_general, actif, ordre) VALUES (?,?,?,?,?,?,?,?,?,?)";
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                remplirParametresEpreuve(ps, e);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) e.setId(keys.getInt(1));
                }
            }
            enregistrerParametres(conn, e);
            enregistrerBareme(conn, e);
            conn.commit();
        } catch (SQLException ex) {
            conn.rollback();
            throw ex;
        } finally {
            conn.setAutoCommit(ancienAutoCommit);
        }
    }

    public void mettreAJour(Epreuve e) throws SQLException {
        Connection conn = DatabaseManager.getConnection();
        boolean ancienAutoCommit = conn.getAutoCommit();
        try {
            conn.setAutoCommit(false);
            String sql = "UPDATE epreuves SET nom=?, description=?, mode_calcul=?, formule=?, valeur_cle=?, sens_classement=?, " +
                    "afficher_classement=?, compte_dans_general=?, actif=?, ordre=? WHERE id=?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                remplirParametresEpreuve(ps, e);
                ps.setInt(11, e.getId());
                ps.executeUpdate();
            }
            // On recrée les paramètres et le barème (plus simple et sûr que du diff fin)
            try (PreparedStatement del1 = conn.prepareStatement("DELETE FROM epreuve_parametres WHERE epreuve_id = ?")) {
                del1.setInt(1, e.getId());
                del1.executeUpdate();
            }
            try (PreparedStatement del2 = conn.prepareStatement("DELETE FROM bareme_points WHERE epreuve_id = ?")) {
                del2.setInt(1, e.getId());
                del2.executeUpdate();
            }
            enregistrerParametres(conn, e);
            enregistrerBareme(conn, e);
            conn.commit();
        } catch (SQLException ex) {
            conn.rollback();
            throw ex;
        } finally {
            conn.setAutoCommit(ancienAutoCommit);
        }
    }

    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM epreuves WHERE id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // -------------------- helpers internes --------------------

    private void enregistrerParametres(Connection conn, Epreuve e) throws SQLException {
        String sql = "INSERT INTO epreuve_parametres (epreuve_id, nom_variable, label, unite, obligatoire, ordre) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (EpreuveParametre param : e.getParametres()) {
                ps.setInt(1, e.getId());
                ps.setString(2, param.getNomVariable());
                ps.setString(3, param.getLabel());
                ps.setString(4, param.getUnite());
                ps.setBoolean(5, param.isObligatoire());
                ps.setInt(6, param.getOrdre());
                ps.addBatch();
            }
            if (!e.getParametres().isEmpty()) ps.executeBatch();
        }
    }

    private void enregistrerBareme(Connection conn, Epreuve e) throws SQLException {
        String sql = "INSERT INTO bareme_points (epreuve_id, rang, points) VALUES (?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (BaremePoint bp : e.getBareme()) {
                ps.setInt(1, e.getId());
                ps.setInt(2, bp.getRang());
                ps.setDouble(3, bp.getPoints());
                ps.addBatch();
            }
            if (!e.getBareme().isEmpty()) ps.executeBatch();
        }
    }

    private void chargerParametres(Epreuve e) throws SQLException {
        String sql = "SELECT * FROM epreuve_parametres WHERE epreuve_id = ? ORDER BY ordre, id";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, e.getId());
            try (ResultSet rs = ps.executeQuery()) {
                List<EpreuveParametre> params = new ArrayList<>();
                while (rs.next()) {
                    EpreuveParametre param = new EpreuveParametre();
                    param.setId(rs.getInt("id"));
                    param.setEpreuveId(rs.getInt("epreuve_id"));
                    param.setNomVariable(rs.getString("nom_variable"));
                    param.setLabel(rs.getString("label"));
                    param.setUnite(rs.getString("unite"));
                    param.setObligatoire(rs.getBoolean("obligatoire"));
                    param.setOrdre(rs.getInt("ordre"));
                    params.add(param);
                }
                e.setParametres(params);
            }
        }
    }

    private void chargerBareme(Epreuve e) throws SQLException {
        String sql = "SELECT * FROM bareme_points WHERE epreuve_id = ? ORDER BY rang";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, e.getId());
            try (ResultSet rs = ps.executeQuery()) {
                List<BaremePoint> bareme = new ArrayList<>();
                while (rs.next()) {
                    BaremePoint bp = new BaremePoint();
                    bp.setId(rs.getInt("id"));
                    bp.setEpreuveId(rs.getInt("epreuve_id"));
                    bp.setRang(rs.getInt("rang"));
                    bp.setPoints(rs.getDouble("points"));
                    bareme.add(bp);
                }
                e.setBareme(bareme);
            }
        }
    }

    private void remplirParametresEpreuve(PreparedStatement ps, Epreuve e) throws SQLException {
        ps.setString(1, e.getNom());
        ps.setString(2, e.getDescription());
        ps.setString(3, e.getModeCalcul().name());
        ps.setString(4, e.getFormule());
        ps.setString(5, e.getValeurCle());
        ps.setString(6, e.getSensClassement().name());
        ps.setBoolean(7, e.isAfficherClassement());
        ps.setBoolean(8, e.isCompteDansGeneral());
        ps.setBoolean(9, e.isActif());
        ps.setInt(10, e.getOrdre());
    }

    private Epreuve mapper(ResultSet rs) throws SQLException {
        Epreuve e = new Epreuve();
        e.setId(rs.getInt("id"));
        e.setNom(rs.getString("nom"));
        e.setDescription(rs.getString("description"));
        e.setModeCalcul(ModeCalcul.valueOf(rs.getString("mode_calcul")));
        e.setFormule(rs.getString("formule"));
        e.setValeurCle(rs.getString("valeur_cle"));
        e.setSensClassement(SensClassement.valueOf(rs.getString("sens_classement")));
        e.setAfficherClassement(rs.getBoolean("afficher_classement"));
        e.setCompteDansGeneral(rs.getBoolean("compte_dans_general"));
        e.setActif(rs.getBoolean("actif"));
        e.setOrdre(rs.getInt("ordre"));
        return e;
    }
}
