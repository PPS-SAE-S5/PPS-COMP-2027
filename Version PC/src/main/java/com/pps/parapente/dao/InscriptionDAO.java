package com.pps.parapente.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Gère l'inscription des pilotes aux épreuves : "toute personne inscrite doit passer
 * les épreuves (sinon 0 pt)" -> on doit donc savoir qui est inscrit à quoi.
 */
public class InscriptionDAO {

    public void inscrire(int piloteId, int epreuveId) throws SQLException {
        String sql = "MERGE INTO inscriptions_epreuve (pilote_id, epreuve_id) KEY(pilote_id, epreuve_id) VALUES (?, ?)";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, piloteId);
            ps.setInt(2, epreuveId);
            ps.executeUpdate();
        }
    }

    public void desinscrire(int piloteId, int epreuveId) throws SQLException {
        String sql = "DELETE FROM inscriptions_epreuve WHERE pilote_id=? AND epreuve_id=?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, piloteId);
            ps.setInt(2, epreuveId);
            ps.executeUpdate();
        }
    }

    /** Inscrit automatiquement tous les pilotes existants à une épreuve (pratique par défaut). */
    public void inscrireTousLesPilotes(int epreuveId) throws SQLException {
        String sql = "INSERT INTO inscriptions_epreuve (pilote_id, epreuve_id) " +
                "SELECT id, ? FROM pilotes WHERE id NOT IN " +
                "(SELECT pilote_id FROM inscriptions_epreuve WHERE epreuve_id = ?)";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, epreuveId);
            ps.setInt(2, epreuveId);
            ps.executeUpdate();
        }
    }

    /** Inscrit un pilote donné à toutes les épreuves actives existantes (pratique par défaut à l'inscription). */
    public void inscrirePiloteAToutesLesEpreuvesActives(int piloteId) throws SQLException {
        String sql = "INSERT INTO inscriptions_epreuve (pilote_id, epreuve_id) " +
                "SELECT ?, id FROM epreuves WHERE actif = TRUE AND id NOT IN " +
                "(SELECT epreuve_id FROM inscriptions_epreuve WHERE pilote_id = ?)";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, piloteId);
            ps.setInt(2, piloteId);
            ps.executeUpdate();
        }
    }

    public List<Integer> listerPilotesInscrits(int epreuveId) throws SQLException {
        List<Integer> liste = new ArrayList<>();
        String sql = "SELECT pilote_id FROM inscriptions_epreuve WHERE epreuve_id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, epreuveId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) liste.add(rs.getInt("pilote_id"));
            }
        }
        return liste;
    }

    public List<Integer> listerEpreuvesDuPilote(int piloteId) throws SQLException {
        List<Integer> liste = new ArrayList<>();
        String sql = "SELECT epreuve_id FROM inscriptions_epreuve WHERE pilote_id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, piloteId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) liste.add(rs.getInt("epreuve_id"));
            }
        }
        return liste;
    }
}
