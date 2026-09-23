package com.pps.parapente.dao;

import com.pps.parapente.model.Role;
import com.pps.parapente.model.Utilisateur;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UtilisateurDAO {

    public Optional<Utilisateur> trouverParIdentifiant(String identifiant) throws SQLException {
        String sql = "SELECT * FROM utilisateurs WHERE identifiant = ? AND actif = TRUE";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, identifiant);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapper(rs));
            }
        }
        return Optional.empty();
    }

    public List<Utilisateur> listerTous() throws SQLException {
        List<Utilisateur> liste = new ArrayList<>();
        String sql = "SELECT * FROM utilisateurs ORDER BY identifiant";
        try (Statement st = DatabaseManager.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) liste.add(mapper(rs));
        }
        return liste;
    }

    public void creer(Utilisateur u) throws SQLException {
        String sql = "INSERT INTO utilisateurs (identifiant, mot_de_passe, role, pilote_id, actif) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getIdentifiant());
            ps.setString(2, u.getMotDePasseHache());
            ps.setString(3, u.getRole().name());
            if (u.getPiloteId() != null) ps.setInt(4, u.getPiloteId()); else ps.setNull(4, Types.INTEGER);
            ps.setBoolean(5, u.isActif());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) u.setId(keys.getInt(1));
            }
        }
    }

    public void mettreAJourMotDePasse(int id, String nouveauHache) throws SQLException {
        String sql = "UPDATE utilisateurs SET mot_de_passe = ? WHERE id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, nouveauHache);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM utilisateurs WHERE id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Utilisateur mapper(ResultSet rs) throws SQLException {
        Utilisateur u = new Utilisateur();
        u.setId(rs.getInt("id"));
        u.setIdentifiant(rs.getString("identifiant"));
        u.setMotDePasseHache(rs.getString("mot_de_passe"));
        u.setRole(Role.valueOf(rs.getString("role")));
        int piloteId = rs.getInt("pilote_id");
        u.setPiloteId(rs.wasNull() ? null : piloteId);
        u.setActif(rs.getBoolean("actif"));
        return u;
    }
}
