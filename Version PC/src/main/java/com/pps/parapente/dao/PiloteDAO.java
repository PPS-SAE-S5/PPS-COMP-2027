package com.pps.parapente.dao;

import com.pps.parapente.model.Pilote;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PiloteDAO {

    public List<Pilote> listerTous() throws SQLException {
        List<Pilote> liste = new ArrayList<>();
        String sql = "SELECT * FROM pilotes ORDER BY nom, prenom";
        try (Statement st = DatabaseManager.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) liste.add(mapper(rs));
        }
        return liste;
    }

    public Optional<Pilote> trouverParId(int id) throws SQLException {
        String sql = "SELECT * FROM pilotes WHERE id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapper(rs));
            }
        }
        return Optional.empty();
    }

    public void creer(Pilote p) throws SQLException {
        String sql = "INSERT INTO pilotes (numero_licence, nom, prenom, caserne, poids, email, annee_naissance, categorie) " +
                "VALUES (?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            remplirParametres(ps, p);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) p.setId(keys.getInt(1));
            }
        }
    }

    public void mettreAJour(Pilote p) throws SQLException {
        String sql = "UPDATE pilotes SET numero_licence=?, nom=?, prenom=?, caserne=?, poids=?, email=?, annee_naissance=?, categorie=? " +
                "WHERE id=?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            remplirParametres(ps, p);
            ps.setInt(9, p.getId());
            ps.executeUpdate();
        }
    }

    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM pilotes WHERE id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private void remplirParametres(PreparedStatement ps, Pilote p) throws SQLException {
        ps.setString(1, p.getNumeroLicence());
        ps.setString(2, p.getNom());
        ps.setString(3, p.getPrenom());
        ps.setString(4, p.getCaserne());
        if (p.getPoids() != null) ps.setDouble(5, p.getPoids()); else ps.setNull(5, Types.DOUBLE);
        ps.setString(6, p.getEmail());
        if (p.getAnneeNaissance() != null) ps.setInt(7, p.getAnneeNaissance()); else ps.setNull(7, Types.INTEGER);
        ps.setString(8, p.getCategorie());
    }

    private Pilote mapper(ResultSet rs) throws SQLException {
        Pilote p = new Pilote();
        p.setId(rs.getInt("id"));
        p.setNumeroLicence(rs.getString("numero_licence"));
        p.setNom(rs.getString("nom"));
        p.setPrenom(rs.getString("prenom"));
        p.setCaserne(rs.getString("caserne"));
        double poids = rs.getDouble("poids");
        p.setPoids(rs.wasNull() ? null : poids);
        p.setEmail(rs.getString("email"));
        int annee = rs.getInt("annee_naissance");
        p.setAnneeNaissance(rs.wasNull() ? null : annee);
        p.setCategorie(rs.getString("categorie"));
        Timestamp ts = rs.getTimestamp("date_inscription");
        if (ts != null) p.setDateInscription(ts.toLocalDateTime());
        return p;
    }
}
